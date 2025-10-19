package com.team21.myapplication.utils

import android.app.Application
import com.cloudinary.android.MediaManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = mapOf("cloud_name" to "dzglyzgv3")
        MediaManager.init(this, config)
    }
}