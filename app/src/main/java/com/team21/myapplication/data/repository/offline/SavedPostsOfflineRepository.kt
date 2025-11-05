package com.team21.myapplication.data.repository.offline

import android.content.Context
import android.net.Uri
import com.team21.myapplication.R
import com.team21.myapplication.cache.ArrayMapCacheProvider
import com.team21.myapplication.cache.CacheProvider
import com.team21.myapplication.local.hive.SavedPostsHive
import com.team21.myapplication.ui.filterView.state.PreviewCardUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SavedPostsOfflineRepository(
    private val appContext: Context,
    private val mem: CacheProvider<String, List<PreviewCardUi>> = ArrayMapCacheProvider()
) {
    private val hive = SavedPostsHive(appContext)
    private val KEY = "saved_list"

    private fun fallbackPhotoUri(): String {
        val pkg = appContext.packageName
        val resId = R.drawable.sample_house
        return Uri.parse("android.resource://$pkg/$resId").toString()
    }

    suspend fun load(): List<PreviewCardUi>? {
        mem.get(KEY)?.let { return it }
        val fromDisk = withContext(Dispatchers.IO) { hive.readAll() }
        if (fromDisk.isEmpty()) return null
        val ui = fromDisk.map {
            PreviewCardUi(
                housingId = it.housingId,
                title = it.title,
                rating = it.rating,
                reviewsCount = it.reviewsCount,
                pricePerMonthLabel = it.pricePerMonthLabel,
                photoUrl = it.photoUrl ?: fallbackPhotoUri()
            )
        }
        mem.put(KEY, ui)
        return ui
    }

    suspend fun saveAll(items: List<PreviewCardUi>) {
        mem.put(KEY, items)
        withContext(Dispatchers.IO) {
            hive.writeAll(
                items.map {
                    SavedPostsHive.PreviewDto(
                        housingId = it.housingId,
                        title = it.title,
                        rating = it.rating.toDouble(),
                        reviewsCount = it.reviewsCount,
                        pricePerMonthLabel = it.pricePerMonthLabel,
                        photoUrl = it.photoUrl
                    )
                }
            )
        }
    }

    fun clearMemory() { mem.clear() }
}
