package com.team21.myapplication.utils

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.cloudinary.android.MediaManager
import com.team21.myapplication.data.local.SecureSessionManager
import com.team21.myapplication.workers.UploadDraftWorker

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                UploadDraftWorker.NOTIF_CHANNEL,
                "Uploads",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(ch)
        }
    }

}