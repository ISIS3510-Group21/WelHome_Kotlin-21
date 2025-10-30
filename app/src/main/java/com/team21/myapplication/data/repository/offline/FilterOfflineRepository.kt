package com.team21.myapplication.data.repository.offline

import android.content.Context
import com.team21.myapplication.data.local.prefs.CachedTagDto
import com.team21.myapplication.data.local.prefs.FilterPrefs
import com.team21.myapplication.ui.filterView.state.TagChipUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [EVENTUAL CONNECTIVITY] [CACHE-FIRST]
 * Offline repository for Filters:
 * - Reads cached tags and last selection from preferences (cache-first).
 * - Persists both in the background (coroutines IO).
 */
class FilterOfflineRepository(ctx: Context) {

    private val prefs = FilterPrefs.get(ctx)

    // ---- Selection ----
    suspend fun saveLastSelection(tagIdsOrdered: List<String>) = withContext(Dispatchers.IO) {
        // [MULTITHREADING] Persist selection off the main thread.
        prefs.saveLastSelection(tagIdsOrdered)
    }

    suspend fun readLastSelection(): List<String> = withContext(Dispatchers.IO) {
        // [MULTITHREADING] Read from prefs off the main thread.
        prefs.readLastSelection()
    }

    // ---- Cached tags ----
    suspend fun saveCachedTagsFromUi(chips: List<TagChipUi>) = withContext(Dispatchers.IO) {
        // [MULTITHREADING] Persist tags snapshot off the main thread.
        val dto = chips.map { CachedTagDto(id = it.id, name = it.label, iconPath = "") }
        prefs.saveCachedTags(dto)
    }

    suspend fun readCachedTagsAsUi(): List<TagChipUi> = withContext(Dispatchers.IO) {
        // [MULTITHREADING] Read from prefs off the main thread.
        prefs.readCachedTags().map { TagChipUi(id = it.id, label = it.name, selected = false) }
    }
}
