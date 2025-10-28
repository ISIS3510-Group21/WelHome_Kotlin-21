package com.team21.myapplication.cache

import android.util.ArrayMap

/**
 * FiltersCache
 *
 * Cache ligero en memoria para almacenar resultados de filtros
 * (por ejemplo, búsquedas por tags o ubicación).
 *
 * Basado en ArrayMap, optimizado para colecciones pequeñas y
 * eficiente en memoria (usa menos objetos que un HashMap).
 */
class FiltersCache private constructor() : CacheProvider<String, Any> {

    companion object {
        @Volatile
        private var INSTANCE: FiltersCache? = null

        fun getInstance(): FiltersCache {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FiltersCache().also { INSTANCE = it }
            }
        }
    }

    private val cache = ArrayMap<String, Any>()

    override fun put(key: String, value: Any) {
        cache[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(key: String): Any? {
        return cache[key]
    }

    override fun clear() {
        cache.clear()
    }

    /**
     * Tamaño actual del cache (para debug o métricas)
     */
    fun size(): Int = cache.size
}
