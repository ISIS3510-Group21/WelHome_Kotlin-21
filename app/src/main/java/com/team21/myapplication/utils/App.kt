package com.team21.myapplication.utils

import android.app.Application
import com.cloudinary.android.MediaManager

class App : Application() {

    // NetworkMonitor como propiedad de la aplicación
    lateinit var networkMonitor: NetworkMonitor
        private set

    override fun onCreate() {
        super.onCreate()

        // Inicializar cloudinary
        val config = mapOf("cloud_name" to "dzglyzgv3")
        MediaManager.init(this, config)

        // Inicializar NetworkMonitor
        networkMonitor = NetworkMonitor.get(this)
    }
}