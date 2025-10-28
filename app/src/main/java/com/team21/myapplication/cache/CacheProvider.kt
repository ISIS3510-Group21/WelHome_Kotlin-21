package com.team21.myapplication.cache

/**
 * Interfaz genérica para definir un proveedor de cache en memoria.
 */
interface CacheProvider<K, V> {
    fun put(key: K, value: V)
    fun get(key: K): V?
    fun clear()
}
