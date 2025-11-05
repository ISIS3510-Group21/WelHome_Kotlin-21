package com.team21.myapplication.local.hive

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * [LOCAL STORAGE]
 * File-backed store for Saved Posts list (previews).
 * - Single file: filesDir/saved_posts.json
 * - Stores a flat array of preview DTOs.
 */
class SavedPostsHive(private val appContext: Context) {

    private val file by lazy { File(appContext.filesDir, "saved_posts.json") }

    data class PreviewDto(
        val housingId: String,
        val title: String,
        val rating: Double,
        val reviewsCount: Int,
        val pricePerMonthLabel: String,
        val photoUrl: String?
    )

    suspend fun readAll(): List<PreviewDto> = withContext(Dispatchers.IO) { // [MULTITHREADING]
        if (!file.exists()) return@withContext emptyList()
        runCatching {
            val arr = JSONArray(file.readText())
            buildList(arr.length()) {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        PreviewDto(
                            housingId = o.optString("housingId"),
                            title = o.optString("title"),
                            rating = o.optDouble("rating", 0.0),
                            reviewsCount = o.optInt("reviewsCount", 0),
                            pricePerMonthLabel = o.optString("pricePerMonthLabel"),
                            photoUrl = o.optString("photoUrl", null)?.takeIf { it.isNotBlank() }
                        )
                    )
                }
            }
        }.getOrElse { emptyList() }
    }

    suspend fun writeAll(items: List<PreviewDto>) = withContext(Dispatchers.IO) { // [MULTITHREADING]
        val arr = JSONArray()
        items.forEach {
            arr.put(
                JSONObject().apply {
                    put("housingId", it.housingId)
                    put("title", it.title)
                    put("rating", it.rating)
                    put("reviewsCount", it.reviewsCount)
                    put("pricePerMonthLabel", it.pricePerMonthLabel)
                    put("photoUrl", it.photoUrl ?: JSONObject.NULL)
                }
            )
        }
        file.writeText(arr.toString())
    }
}
