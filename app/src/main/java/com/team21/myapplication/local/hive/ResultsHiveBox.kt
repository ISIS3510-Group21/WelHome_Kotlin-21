package com.team21.myapplication.local.hive

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * [LOCAL STORAGE]
 * "Hive" file-backed store para persistir combinaciones -> previews (DTO).
 * - Archivo JSON único: filesDir/results_hive.json
 * - Mantiene un índice MRU (order) para limitar a 10 combinaciones.
 * - Guarda photoUrl; si no hay, el caller puede usar un fallback drawable.
 */
class ResultsHiveBox(private val appContext: Context) {

    private val file: File by lazy { File(appContext.filesDir, "results_hive.json") }
    private val maxCombos = 10

    data class PreviewDto(
        val housingId: String,
        val title: String,
        val rating: Double,
        val reviewsCount: Int,
        val pricePerMonthLabel: String,
        val photoUrl: String? // puede ser null; el repo aplicará fallback
    )

    // -------------- API --------------

    suspend fun readAll(): Map<String, List<PreviewDto>> = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext emptyMap()
        runCatching {
            val text = file.readText()
            val root = JSONObject(text)
            val order = root.optJSONArray("order") ?: JSONArray()
            val combos = root.optJSONObject("combos") ?: JSONObject()

            val result = LinkedHashMap<String, List<PreviewDto>>()
            for (i in 0 until order.length()) {
                val token = order.getString(i)
                val arr = combos.optJSONArray(token) ?: JSONArray()
                result[token] = arr.toDtoList()
            }
            result
        }.getOrElse { emptyMap() }
    }

    suspend fun writeCombo(token: String, items: List<PreviewDto>) = withContext(Dispatchers.IO) {
        val current = readAll().toMutableMap()
        // refrescar MRU (quitar y reinsertar al inicio)
        current.remove(token)
        current[token] = items

        // Evicción MRU
        while (current.size > maxCombos) {
            val eldest = current.keys.lastOrNull() ?: break
            current.remove(eldest)
        }

        persist(current)
    }

    suspend fun readCombo(token: String): List<PreviewDto>? = withContext(Dispatchers.IO) {
        val all = readAll().toMutableMap()
        val list = all.remove(token) ?: return@withContext null
        // reinsertar como más reciente
        all[token] = list
        persist(all)
        list
    }

    // -------------- Helpers --------------

    private fun persist(current: Map<String, List<PreviewDto>>) {
        val root = JSONObject()
        val combos = JSONObject()
        val order = JSONArray()
        current.forEach { (k, v) ->
            combos.put(k, v.toJsonArray())
            order.put(k)
        }
        root.put("order", order)
        root.put("combos", combos)
        file.writeText(root.toString())
    }

    private fun JSONArray.toDtoList(): List<PreviewDto> {
        return buildList(length()) {
            for (i in 0 until length()) {
                val o = getJSONObject(i)
                add(
                    PreviewDto(
                        housingId = o.optString("housingId"),
                        title = o.optString("title"),
                        rating = o.optDouble("rating", 0.0),
                        reviewsCount = o.optInt("reviewsCount", 0),
                        pricePerMonthLabel = o.optString("pricePerMonthLabel"),
                        photoUrl = o.optString("photoUrl", null)
                            ?.takeIf { it.isNotBlank() }
                    )
                )
            }
        }
    }

    private fun List<PreviewDto>.toJsonArray(): JSONArray {
        val arr = JSONArray()
        this.forEach { p ->
            arr.put(
                JSONObject().apply {
                    put("housingId", p.housingId)
                    put("title", p.title)
                    put("rating", p.rating.toDouble())
                    put("reviewsCount", p.reviewsCount)
                    put("pricePerMonthLabel", p.pricePerMonthLabel)
                    put("photoUrl", p.photoUrl ?: JSONObject.NULL)
                }
            )
        }
        return arr
    }
}
