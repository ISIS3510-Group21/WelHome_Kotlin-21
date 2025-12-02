package com.team21.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team21.myapplication.data.local.entity.OwnerVisitCacheEntity

@Dao
interface OwnerVisitCacheDao {

    @Query("""
        SELECT * FROM owner_visit_cache 
        WHERE timestamp BETWEEN :fromMillis AND :toMillis
        ORDER BY timestamp ASC
    """)
    suspend fun getVisitsInRange(
        fromMillis: Long,
        toMillis: Long
    ): List<OwnerVisitCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(visits: List<OwnerVisitCacheEntity>)

    @Query("DELETE FROM owner_visit_cache")
    suspend fun clearAll()
}
