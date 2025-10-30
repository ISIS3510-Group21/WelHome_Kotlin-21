package com.team21.myapplication.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team21.myapplication.local.db.entity.DetailSnapshotEntity

@Dao
interface DetailSnapshotDao {

    @Query("SELECT * FROM detail_snapshots WHERE housingId = :id LIMIT 1")
    suspend fun getById(id: String): DetailSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DetailSnapshotEntity)

    @Query("DELETE FROM detail_snapshots WHERE housingId = :id")
    suspend fun deleteById(id: String)
}
