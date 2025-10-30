package com.team21.myapplication.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * [LOCAL STORAGE] SharedPreferences for Filters feature.
 * - Stores last selected tag IDs (ordered)
 * - Stores cached tags (minimal snapshot: id, name, iconPath) to enable cache-first offline.
 */
class FilterPrefs private constructor(ctx: Context) {

    private val sp: SharedPreferences = ctx.getSharedPreferences("filter_prefs", Context.MODE_PRIVATE)

    // ---------------- Selection (ids) ----------------

    fun saveLastSelection(tagIdsOrdered: List<String>) {
        // [MULTITHREADING] Call from Dispatchers.IO to avoid blocking main thread.
        sp.edit().putString(KEY_TAGS_CSV, tagIdsOrdered.joinToString(",")).apply()
    }

    fun readLastSelection(): List<String> {
        val csv = sp.getString(KEY_TAGS_CSV, "") ?: ""
        return csv.split(",").filter { it.isNotBlank() }
    }

    // ---------------- Cached tags (JSON) ----------------

    fun saveCachedTags(items: List<CachedTagDto>) {
        // [MULTITHREADING] Call from Dispatchers.IO to avoid blocking main thread.
        val arr = JSONArray()
        items.forEach { t ->
            arr.put(JSONObject().apply {
                put("id", t.id)
                put("name", t.name)
                put("iconPath", t.iconPath)
            })
        }
        sp.edit().putString(KEY_TAGS_JSON, arr.toString()).apply()
    }

    fun readCachedTags(): List<CachedTagDto> {
        val json = sp.getString(KEY_TAGS_JSON, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            buildList(arr.length()) {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        CachedTagDto(
                            id = o.optString("id"),
                            name = o.optString("name"),
                            iconPath = o.optString("iconPath")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val KEY_TAGS_CSV = "last_selected_tags_csv"
        private const val KEY_TAGS_JSON = "cached_tags_json"

        @Volatile private var INSTANCE: FilterPrefs? = null
        fun get(ctx: Context): FilterPrefs {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FilterPrefs(ctx.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

/** Minimal snapshot to persist tags in prefs (cache-first offline). */
data class CachedTagDto(
    val id: String,
    val name: String,
    val iconPath: String
)
