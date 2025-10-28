package com.team21.myapplication.cache

import android.util.LruCache

/**
 * LruDataCache
 *
 * Cache genérico basado en LRU (Least Recently Used) para almacenar
 * objetos de dominio (HousingPost, Review, filtros serializados, etc.).
 *
 * Implementa la interfaz CacheProvider<K, V> para mantener consistencia
 * con las demás estructuras de cache (FiltersCache y SparseCache).
 */
class LruDataCache<K, V> private constructor(maxSize: Int) : CacheProvider<K, V> {

    companion object {
        @Volatile
        private var INSTANCE: LruDataCache<String, Any>? = null

        /**
         * Devuelve una instancia única del cache.
         * Tamaño por defecto = 1/8 de la memoria disponible.
         */
        fun getInstance(): LruDataCache<String, Any> {
            return INSTANCE ?: synchronized(this) {
                val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
                val cacheSize = maxMemory / 8
                INSTANCE ?: LruDataCache<String, Any>(cacheSize).also { INSTANCE = it }
            }
        }
    }

    // Cache interna
    private val memoryCache = object : LruCache<K, V>(maxSize) {
        override fun sizeOf(key: K, value: V): Int {
            // Estimación genérica: 1 unidad por objeto
            // (si necesitas precisión, puedes sobreescribir con byteCount)
            return 1
        }
    }

    // Implementación de la interfaz
    override fun put(key: K, value: V) {
        if (memoryCache.get(key) == null) {
            memoryCache.put(key, value)
        }
    }

    override fun get(key: K): V? = memoryCache.get(key)

    override fun clear() {
        memoryCache.evictAll()
    }

    /**
     * Tamaño actual del cache (en número de elementos)
     */
    fun size(): Int = memoryCache.size()
}
