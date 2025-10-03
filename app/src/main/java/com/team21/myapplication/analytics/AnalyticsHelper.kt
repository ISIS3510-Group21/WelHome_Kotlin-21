package com.team21.myapplication.analytics

import android.content.Context
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

class AnalyticsHelper(context: Context) {

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun logHomeOpen() {
        Log.d("AnalyticsHelper", "Logging home open event")
        firebaseAnalytics.logEvent("home_open") {}
    }

    fun logHomeLoadingTime(timeInMillis: Long,
                           deviceModel: String? = null,
                           network: String? = null
    ) {
        Log.d("AnalyticsHelper", "Logging home loading time event")
        firebaseAnalytics.logEvent("home_loading_time"){
            param("time_ms", timeInMillis)
            deviceModel?.let { param("device_model", it) }
            network?.let { param("network_type", it) }
        }
    }

}