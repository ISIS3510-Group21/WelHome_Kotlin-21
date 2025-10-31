package com.team21.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team21.myapplication.data.local.entity.ThreadForumEntity

@Dao
interface ThreadForumDao {
    @Query("SELECT * FROM thread_forum")
    suspend fun getAllThreads(): List<ThreadForumEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(threads: List<ThreadForumEntity>)

    @Query("DELETE FROM thread_forum")
    suspend fun clearAll()
}
