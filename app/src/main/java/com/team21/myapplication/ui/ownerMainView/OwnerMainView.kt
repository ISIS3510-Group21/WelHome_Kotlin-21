package com.team21.myapplication.ui.ownerMainView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.team21.myapplication.ui.components.cards.HousingInfoCard
import com.team21.myapplication.ui.components.inputs.SearchBar
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Scaffold
import com.team21.myapplication.data.model.HousingPreview
import com.team21.myapplication.ui.components.banners.BannerPosition
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.main.LoadingScreen
import android.content.Intent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.team21.myapplication.ui.detailView.DetailHousingActivity


@Composable
fun OwnerMainScreenLayout(
    items: List<HousingPreview>,
    onCardClick: (HousingPreview) -> Unit
) {
    // Principal component as Column
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {

        // --- Header / SearchBar fijo arriba ---
        item {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                SearchBar(
                    query = "",
                    onQueryChange = {},
                    placeholder = "Search",
                    asButton = true,
                    onClick = {}
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        items(
            items = items,
            key = { it.id }
        ) { item ->
            // evita recomputar el string del precio en cada recomposición
            val priceLabel = remember(item.id, item.price) {
                "$${"%,.0f".format(item.price)} /month"
            }

            Box(contentAlignment = Alignment.Center) {
                HousingInfoCard(
                    title = item.title,
                    rating = item.rating.toDouble(),
                    reviewsCount = item.reviewsCount.toInt(),
                    pricePerMonthLabel = priceLabel,
                    imageUrl = item.photoPath,
                    onClick = { onCardClick(item) },
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth()
                )
            }
            Spacer(Modifier.height(16.dp))

        }

        if (items.isEmpty()) {
            item {
                BlackText(
                    text = "There are no posts to display. Please connect to the internet to load suggestions."
                )
            }
        }

    }
}

@Composable
fun OwnerMainScreen() {
    val appContext = LocalContext.current.applicationContext
    val viewModel: OwnerMainViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OwnerMainViewModel(appContext) as T
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    LaunchedEffect(Unit) { viewModel.loadOwnerHome() }

    val view = LocalView.current

    val statusBarColor = if (!state.isOnline) {
        androidx.compose.ui.graphics.Color.Black
    } else {
        MaterialTheme.colorScheme.background
    }

    Scaffold(
        topBar = {
            SideEffect {
                val window = (view.context as android.app.Activity).window
                // Variable de fondo
                window.statusBarColor = statusBarColor.toArgb()

                // La lógica para decidir el color de los íconos
                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
                    if (!state.isOnline) {
                        false // Íconos blancos para fondo negro
                    } else {
                        statusBarColor.luminance() > 0.5f // Decide según la luminancia del fondo
                    }
            }
        }

    ) { inner ->
        if (state.isLoading) {
            LoadingScreen(modifier = Modifier.padding(inner))
        } else {

            val listToShow = if (state.isOnline) {
                // online - mostrar todos
                state.defaultTop
            } else {
                // offline → lru + snapshot, sin duplicados, hasta 20
                val recent = state.recentlySeen
                val filler = state.defaultTop.filter { snap ->
                    recent.none { it.id == snap.id }
                }
                (recent + filler).take(20)
            }

            Column(modifier = Modifier.padding(inner)) {
                ConnectivityBanner(
                    visible = !state.isOnline,
                    position = BannerPosition.Top,
                )

                Spacer(Modifier.height(12.dp))

                OwnerMainScreenLayout(
                    items = listToShow,

                    onCardClick = { item ->
                        // 1) registra el click en el LRU (para "recently seen")
                        viewModel.onPostClicked(item)

                        // 2) navega al detalle
                        val intent = Intent(ctx, DetailHousingActivity::class.java)
                            .putExtra(DetailHousingActivity.EXTRA_HOUSING_ID, item.id)
                        ctx.startActivity(intent)
                    }
                )
            }
        }
    }
}
