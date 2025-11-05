package com.team21.myapplication.ui.saved

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team21.myapplication.ui.components.banners.BannerPosition
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import com.team21.myapplication.ui.components.inputs.SearchBar
import com.team21.myapplication.ui.saved.state.SavedPostsUiState
import com.team21.myapplication.ui.theme.AppTextStyles
import com.team21.myapplication.utils.NetworkMonitor
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner


/**
 * SavedPostsRoute
 * - Muestra el ConnectivityBanner según NetworkMonitor
 * - Usa collectAsStateWithLifecycle(initialValue = ...) para evitar errores de delegado
 * - SearchBar -> onOpenFilters (igual patrón que MainActivity)
 */
@Composable
fun SavedPostsRoute(
    onOpenDetail: (String) -> Unit,
    onOpenFilters: () -> Unit
) {
    val ctx = LocalContext.current

    // Conectividad (con valor inicial para evitar errores de delegado)
    val isOnline by remember { NetworkMonitor.get(ctx).isOnline }
        .collectAsStateWithLifecycle(initialValue = true)

    // ViewModel + UI state (con initialValue)
    val vm: SavedPostsViewModel = viewModel()
    val ui: SavedPostsUiState by vm.state.collectAsStateWithLifecycle(
        initialValue = SavedPostsUiState()
    )


    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresco inicial (online-first si hay red)
    LaunchedEffect(Unit) {
        vm.load(isOnline = isOnline)
    }

    // Refresco en onResume (por si hiciste save/unsave en Detail)
    DisposableEffect(lifecycleOwner, isOnline) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.load(isOnline = isOnline)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }


    Scaffold(containerColor = MaterialTheme.colorScheme.background) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            // Banner visible cuando no hay red
            ConnectivityBanner(visible = !isOnline, position = BannerPosition.Top)

            Spacer(Modifier.height(12.dp))
            SearchBar(
                query = "",
                onQueryChange = {},
                placeholder = "Search",
                asButton = true,
                enabled = true,
                onClick = onOpenFilters, // <-- navega a Filters
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Saved Posts",
                style = AppTextStyles.TitleView,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Lista (usa tu HousingInfoCard por dentro)
            SavedPostsView(
                uiState = ui,
                onOpenDetail = onOpenDetail,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
