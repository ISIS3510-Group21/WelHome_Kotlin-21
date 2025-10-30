package com.team21.myapplication.data.repository.offline

import android.content.Context
import android.net.Uri
import com.team21.myapplication.R
import com.team21.myapplication.cache.CacheProvider
import com.team21.myapplication.cache.LruCacheProvider
import com.team21.myapplication.local.hive.ResultsHiveBox
import com.team21.myapplication.ui.filterView.state.PreviewCardUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [EVENTUAL CONNECTIVITY] [CACHE-FIRST]
 * Repositorio offline para Results:
 *  - Guarda/lee combinaciones -> lista de previews en Hive (archivo JSON).
 *  - Cachea en memoria (LRU) las combinaciones más usadas.
 *  - Fallback de 5 previews si la combinación no existe en offline.
 *
 * Notas:
 *  - Incluimos photoUrl. Si es null, asignamos un URI al drawable sample_house.jpg.
 */
class ResultsOfflineRepository(
    private val appContext: Context,
    private val mem: CacheProvider<String, List<PreviewCardUi>> = LruCacheProvider()
) {
    private val hive = ResultsHiveBox(appContext)

    // URI a drawable de fallback (sample_house.jpg)
    private fun fallbackPhotoUri(): String {
        val pkg = appContext.packageName
        val resId = R.drawable.sample_house
        return Uri.parse("android.resource://$pkg/$resId").toString()
    }

    // Persistir una combinación
    suspend fun saveCombo(token: String, items: List<PreviewCardUi>) {
        // [MULTITHREADING] IO write
        withContext(Dispatchers.IO) {
            hive.writeCombo(
                token,
                items.map {
                    ResultsHiveBox.PreviewDto(
                        housingId = it.housingId,
                        title = it.title,
                        rating = it.rating,
                        reviewsCount = it.reviewsCount,
                        pricePerMonthLabel = it.pricePerMonthLabel,
                        photoUrl = it.photoUrl // puede ser null; Hive lo guarda como null
                    )
                }
            )
        }
        // [CACHING]
        mem.put(token, items)
    }

    // Leer una combinación (cache-first)
    suspend fun loadCombo(token: String): List<PreviewCardUi>? {
        // [CACHING]
        mem.get(token)?.let { return it }

        // [LOCAL STORAGE] [MULTITHREADING]
        val fromHive = withContext(Dispatchers.IO) { hive.readCombo(token) } ?: return null
        val ui = fromHive.map {
            PreviewCardUi(
                housingId = it.housingId,
                title = it.title,
                rating = it.rating,
                reviewsCount = it.reviewsCount,
                pricePerMonthLabel = it.pricePerMonthLabel,
                photoUrl = it.photoUrl ?: fallbackPhotoUri() // fallback si viene null
            )
        }
        mem.put(token, ui)
        return ui
    }

    // Fallback: 5 previews de cualquier combinación persistida (o en memoria)
    suspend fun fallbackFive(): List<PreviewCardUi> {
        // Del Hive (persistido); usamos esto porque LruCache no expone sus llaves
        val all = withContext(Dispatchers.IO) { hive.readAll() }
        val flat = all.values.flatten().map {
            PreviewCardUi(
                housingId = it.housingId,
                title = it.title,
                rating = it.rating,
                reviewsCount = it.reviewsCount,
                pricePerMonthLabel = it.pricePerMonthLabel,
                photoUrl = it.photoUrl ?: fallbackPhotoUri()
            )
        }
        return flat.take(5)
    }
}
