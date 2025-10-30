package com.team21.myapplication.cache

import com.team21.myapplication.ui.detailView.state.DetailHousingUiState

/**
 * [CACHING]
 * Non-singleton MRU cache for last N detail states (default 20).
 * This is different from LRU memory budgeting — it keeps at most N entries by recency.
 */
class RecentDetailCache(private val maxSize: Int = 20) {
    private val map = object : LinkedHashMap<String, DetailHousingUiState>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, DetailHousingUiState>?): Boolean {
            return size > maxSize
        }
    }

    fun put(id: String, state: DetailHousingUiState) { map[id] = state }
    fun get(id: String): DetailHousingUiState? = map[id]
    fun contains(id: String): Boolean = map.containsKey(id)
    fun size(): Int = map.size
    fun clear() = map.clear()
}
