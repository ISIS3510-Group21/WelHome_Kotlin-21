package com.team21.myapplication.cache

import android.util.SparseArray

/**
 * SparseCache
 *
 * Cache optimizado para mapear IDs numéricos (Int → objeto),
 * por ejemplo, HousingPost.id → HousingPost.
 *
 * Más eficiente en memoria que HashMap<Integer, Object>
 * porque evita autoboxing de claves.
 */
class SparseCache<V> private constructor() : CacheProvider<Int, V> {

    companion object {
        @Volatile
        private var INSTANCE: SparseCache<Any>? = null

        fun getInstance(): SparseCache<Any> {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SparseCache<Any>().also { INSTANCE = it }
            }
        }
    }

    private val cache = SparseArray<V>()

    override fun put(key: Int, value: V) {
        cache.put(key, value)
    }

    override fun get(key: Int): V? {
        return cache[key]
    }

    override fun clear() {
        cache.clear()
    }

    /**
     * Devuelve el tamaño del cache (número de elementos).
     */
    fun size(): Int = cache.size()
}
