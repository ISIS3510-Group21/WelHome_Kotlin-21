package com.team21.myapplication.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team21.myapplication.data.local.entity.MyPostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MyPostsDao {
    @Query("SELECT * FROM my_posts WHERE ownerId = :ownerId ORDER BY updatedAt DESC")
    fun observeByOwner(ownerId: String): Flow<List<MyPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MyPostEntity>)

    @Query("DELETE FROM my_posts WHERE ownerId = :ownerId")
    suspend fun deleteByOwner(ownerId: String)
}
