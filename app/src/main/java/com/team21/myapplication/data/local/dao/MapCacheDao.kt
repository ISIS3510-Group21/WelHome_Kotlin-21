package com.team21.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team21.myapplication.data.model.MapCacheEntry

@Dao
interface MapCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entry: MapCacheEntry)

    @Query("SELECT * FROM map_cache WHERE id = 1")
    suspend fun getCache(): MapCacheEntry?
}
