package com.team21.myapplication.cache

import android.util.LruCache

/**
 * [CACHING]
 * LRU cache backed by android.util.LruCache.
 *
 * Memory policy:
 * - By default, uses 1/8 of the max memory available to the app process (in KB).
 * - You can provide a custom maxSizeInKb if needed.
 *
 * Thread-safety:
 * - LruCache is synchronized internally for basic operations, but treat it as single-writer in practice.
 * - If you will call from different coroutines, coordinate access at the call site.
 *
 * Size units:
 * - Default sizeOf() returns 1 per entry (count-based).
 * - If you cache large objects (e.g., Bitmaps), consider a specialized subclass that overrides sizeOf(key, value).
 */
open class LruCacheProvider<K, V> @JvmOverloads constructor(
    maxSizeInKb: Int = defaultMaxSizeInKb()
) : CacheProvider<K, V> {

    companion object {
        /** Compute 1/8 of the app max memory in KB. */
        fun defaultMaxSizeInKb(): Int {
            val maxMemoryBytes = Runtime.getRuntime().maxMemory() // bytes
            val maxMemoryKb = (maxMemoryBytes / 1024L).toInt()
            return maxMemoryKb / 8
        }
    }

    // Underlying LRU
    protected val lru: LruCache<K, V> = object : LruCache<K, V>(maxSizeInKb) {
        override fun sizeOf(key: K, value: V): Int {
            // Default: 1 "KB" per entry (count-based). Override if you need precise sizing.
            return 1
        }
    }

    override fun put(key: K, value: V) {
        lru.put(key, value)
    }

    override fun get(key: K): V? = lru.get(key)

    override fun remove(key: K) {
        lru.remove(key)
    }

    override fun clear() {
        lru.evictAll()
    }

    override fun size(): Int = lru.size()
}
