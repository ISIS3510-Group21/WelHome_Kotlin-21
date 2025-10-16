package com.team21.myapplication.ui.ownerMainView

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import com.google.firebase.messaging.FirebaseMessaging
import com.team21.myapplication.ui.theme.AppTheme
import androidx.navigation.compose.rememberNavController
import com.team21.myapplication.ui.components.navbar.OwnerNavBar
import com.team21.myapplication.ui.components.navbar.OwnerNavGraph
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier

class OwnerMainActivity : ComponentActivity() {

    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* noop */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Suscripción a topic (igual que Main)
        FirebaseMessaging.getInstance().subscribeToTopic("trending_filters")

        // Pide permiso de notifs en 13+
        if (Build.VERSION.SDK_INT >= 33) {
            requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            AppTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { OwnerNavBar(navController) }
                ) { inner ->
                    OwnerNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(inner)
                    )
                }
            }
        }
    }
}
