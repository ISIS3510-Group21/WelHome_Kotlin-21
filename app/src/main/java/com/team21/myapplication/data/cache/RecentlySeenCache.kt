package com.team21.myapplication.data.cache

import android.app.ActivityManager
import android.content.Context
import androidx.collection.LruCache
import com.team21.myapplication.data.model.HousingPreview

/**
 * LRU de HousingPreview para "recently seen" - owner.
 * - Tamaño máx = 1/4 de la memoria disponible (KB).
 * - se guarda una lista de IDs en orden MRU (máx 50).
 */
object RecentlySeenCache {

    private var cache: LruCache<String, HousingPreview>? = null
    private val order: LinkedHashMap<String, Unit> = LinkedHashMap(16, 0.75f, /* accessOrder */ true)

    @Synchronized
    fun init(context: Context) {
        if (cache != null) return
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val maxMemKb = am.memoryClass * 1024
        val quarterKb = maxMemKb / 4

        cache = object : LruCache<String, HousingPreview>(quarterKb) {
            override fun sizeOf(key: String, value: HousingPreview): Int {
                val s = (value.title.length) + (value.photoPath?.length ?: 0) + (value.housing?.length ?: 0)
                val approx = 64 + s * 2 // bytes
                return 1 + approx / 1024 // en KB
            }
        }
    }

    fun get(id: String): HousingPreview? = cache?.get(id)

    fun put(item: HousingPreview) {
        cache?.put(item.id, item)
        // actualiza orden MRU
        order.remove(item.id)
        order[item.id] = Unit
        // cap a 50 ids en MRU
        while (order.size > 50) {
            val eldest = order.entries.iterator().next().key
            order.remove(eldest)
        }
    }

    fun getMRU(limit: Int): List<HousingPreview> {
        val ids = order.keys.toList().asReversed() // más reciente primero
        return ids.take(limit).mapNotNull { id -> cache?.get(id) }
    }

    fun isEmpty(): Boolean = order.isEmpty()
    fun clear() { cache?.evictAll(); order.clear() }
}
