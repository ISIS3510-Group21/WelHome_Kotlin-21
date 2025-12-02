package com.team21.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team21.myapplication.data.local.entity.ScheduleDraftEntity

@Dao
interface ScheduleDraftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draft: ScheduleDraftEntity)

    @Query("SELECT * FROM schedule_drafts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ScheduleDraftEntity?

    @Query("SELECT * FROM schedule_drafts")
    suspend fun getAll(): List<ScheduleDraftEntity>

    @Query("DELETE FROM schedule_drafts WHERE id = :id")
    suspend fun deleteById(id: String)
}
