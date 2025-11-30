package com.team21.myapplication.ui.profileView

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team21.myapplication.data.local.SecureSessionManager
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.ui.profileView.state.ProfileUiState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.team21.myapplication.ui.components.banners.BannerPosition
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import androidx.compose.material3.Scaffold
import com.team21.myapplication.ui.updateprofile.UpdateProfileActivity

@Composable
fun ProfileRoute(
    onLogout: () -> Unit
) {

    val context = LocalContext.current
    val session = SecureSessionManager(context.applicationContext)
    val vm: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            auth = AuthRepository(),
            session = session
        )
    )
    val state: ProfileUiState = vm.state.collectAsStateWithLifecycle().value

    val isOnline by com.team21.myapplication.utils.NetworkMonitor
        .get(context).isOnline.collectAsStateWithLifecycle()

    val view = LocalView.current

    val statusBarColor = if (!isOnline) {
        androidx.compose.ui.graphics.Color.Black
    } else {
        MaterialTheme.colorScheme.background
    }

    SideEffect {
        val window = (view.context as android.app.Activity).window
        // Variable de fondo
        window.statusBarColor = statusBarColor.toArgb()

        // La lógica para decidir el color de los íconos
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            if (!isOnline) {
                false // Íconos blancos para fondo negro
            } else {
                statusBarColor.luminance() > 0.5f // Decide según la luminancia del fondo
            }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { inner ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            ConnectivityBanner(
                visible = !isOnline,
                position = BannerPosition.Top
            )

            //Spacer(Modifier.height(12.dp))

            LaunchedEffect(Unit) { vm.load() }
            ProfileView(
                onLogout = onLogout,
                onEditProfile = {
                    val intent = Intent(context, UpdateProfileActivity::class.java)
                    context.startActivity(intent)
                },
                name = state.name,
                email = state.email,
                country = state.country,
                phoneNumber = state.phoneNumber,
                contentTopPadding = 0.dp
            )
        }
    }
}
