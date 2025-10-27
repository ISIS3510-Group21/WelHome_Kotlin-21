package com.team21.myapplication.utils

import android.app.Application
import com.cloudinary.android.MediaManager
import com.team21.myapplication.data.local.SecureSessionManager

class App : Application() {

    // NetworkMonitor como propiedad de la aplicación
    lateinit var networkMonitor: NetworkMonitor
        private set

    // secure session manager
    lateinit var sessionManager: SecureSessionManager
        private set

    override fun onCreate() {
        super.onCreate()

        // Inicializar cloudinary
        val config = mapOf("cloud_name" to "dzglyzgv3")
        MediaManager.init(this, config)

        // Inicializar NetworkMonitor
        networkMonitor = NetworkMonitor.get(this)

        // Inicializar SecureSessionManager
        sessionManager = SecureSessionManager(this)
    }
}