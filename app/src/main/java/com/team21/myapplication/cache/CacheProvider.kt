package com.team21.myapplication.cache

/**
 * [CACHING]
 * Generic in-memory cache contract. Future providers (SparseArray/ArrayMap) can implement this.
 */
interface CacheProvider<K, V> {
    fun put(key: K, value: V)
    fun get(key: K): V?
    fun remove(key: K)
    fun clear()
    fun size(): Int
}
