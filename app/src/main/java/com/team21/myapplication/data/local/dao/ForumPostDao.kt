package com.team21.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team21.myapplication.data.local.entity.ForumPostEntity

@Dao
interface ForumPostDao {
    @Query("SELECT * FROM forum_post WHERE threadId = :threadId ORDER BY creationDate DESC")
    suspend fun getPostsForThread(threadId: String): List<ForumPostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<ForumPostEntity>)

    @Query("DELETE FROM forum_post WHERE threadId = :threadId")
    suspend fun clearPostsForThread(threadId: String)

    @Query("""
        DELETE FROM forum_post
        WHERE threadId = :threadId AND id NOT IN (
            SELECT id FROM forum_post
            WHERE threadId = :threadId
            ORDER BY creationDate DESC
            LIMIT :limit
        )
    """)
    suspend fun trimPostsForThread(threadId: String, limit: Int)
}
