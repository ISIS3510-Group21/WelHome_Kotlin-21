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

    fun logHousingPostClick(
        postId: String,
        postTitle: String,
        housingCategory: String,
        price: Double,
        userNationality: String
    ) {
        Log.d("AnalyticsHelper", "Logging click: $postTitle | Nat: $userNationality | Cat: $housingCategory")
        firebaseAnalytics.logEvent("housing_post_click") {
            param("post_id", postId)
            param("post_title", postTitle)
            param("housing_category", housingCategory)
            param("price", price)
            param("user_nationality", userNationality)
            param(FirebaseAnalytics.Param.CONTENT_TYPE, "housing_post")
        }
    }

    fun setUserNationality(nationality: String) {
        firebaseAnalytics.setUserProperty("nationality", nationality)
    }

    fun setUserType(isStudent: Boolean) {
        firebaseAnalytics.setUserProperty("user_type", if (isStudent) "student" else "host")
    }

    fun setUserLanguage(language: String) {
        firebaseAnalytics.setUserProperty("language", language)
    }

    fun logHousingDetailViewTime(
        postId: String,
        postTitle: String,
        tags: List<String>,
        durationMs: Long,
        userNationality: String
    ) {
        // Un evento por tag para facilitar agregación en Analytics/BigQuery
        tags.ifEmpty { listOf("Unknown") }.forEach { tag ->
            Log.d("AnalyticsHelper", "logHousingDetailViewTime -> tag=$tag duration=$durationMs post=$postId")
            firebaseAnalytics.logEvent("housing_detail_view_time") {
                param("post_id", postId)
                param("post_title", postTitle)
                param("housing_tag", tag)
                param("duration_ms", durationMs)
                param("user_nationality", userNationality)
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "housing_post")
            }
        }
    }

    fun setUserPreferredTag(tagSlug: String) {
        firebaseAnalytics.setUserProperty("preferred_tag", tagSlug)
    }

    fun logMapSearchOpen() {
        Log.d("AnalyticsHelper", "Logging map search open event")
        firebaseAnalytics.logEvent("map_search_open") {}
    }

    fun logMapSearchLoadingTime(timeInMillis: Long,
                           deviceModel: String? = null,
                           network: String? = null
    ) {
        Log.d("AnalyticsHelper", "Logging map search loading time event")
        firebaseAnalytics.logEvent("map_search_loading_time"){
            param("time_ms", timeInMillis)
            deviceModel?.let { param("device_model", it) }
            network?.let { param("network_type", it) }
        }
    }


}