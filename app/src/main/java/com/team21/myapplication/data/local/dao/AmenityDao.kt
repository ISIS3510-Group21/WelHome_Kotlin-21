package com.team21.myapplication.data.local.dao

import androidx.room.*
import com.team21.myapplication.data.local.entity.AmenityEntity

@Dao
interface AmenityDao {

    @Query("SELECT * FROM amenities ORDER BY name ASC")
    suspend fun getAll(): List<AmenityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AmenityEntity>)

    @Query("DELETE FROM amenities")
    suspend fun clear()
}
