package com.team21.myapplication.cache

import android.util.ArrayMap

/**
 * [CACHING]
 * ArrayMap-backed cache that respects the generic CacheProvider<K, V> contract.
 * - Optimized for small key sets with minimal overhead.
 * - Not thread-safe; coordinate access at callers (we use it inside ViewModel/Repository scopes).
 */
class ArrayMapCacheProvider<K, V> : CacheProvider<K, V> {
    private val map = ArrayMap<K, V>()

    override fun put(key: K, value: V) { map[key] = value }
    override fun get(key: K): V? = map[key]
    override fun remove(key: K) { map.remove(key) }
    override fun clear() { map.clear() }
    override fun size(): Int = map.size
    fun values(): Collection<V> = map.values

    companion object {
        // Singleton instance for pending ratings
        val pendingRatingsCache = ArrayMapCacheProvider<String, PendingRating>()
    }
}
