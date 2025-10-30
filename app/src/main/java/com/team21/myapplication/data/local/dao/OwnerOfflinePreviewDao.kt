package com.team21.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team21.myapplication.data.local.entity.OwnerOfflinePreviewEntity

@Dao
interface OwnerOfflinePreviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<OwnerOfflinePreviewEntity>)

    @Query("DELETE FROM owner_offline_previews")
    suspend fun clearAll()

    @Query("SELECT * FROM owner_offline_previews ORDER BY savedAt DESC LIMIT :limit")
    suspend fun getTopN(limit: Int): List<OwnerOfflinePreviewEntity>
}
