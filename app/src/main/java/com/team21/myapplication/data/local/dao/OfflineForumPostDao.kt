
package com.team21.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team21.myapplication.data.local.entity.OfflineForumPostEntity

@Dao
interface OfflineForumPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflinePost(post: OfflineForumPostEntity)

    @Query("SELECT * FROM offline_forum_posts")
    suspend fun getOfflinePosts(): List<OfflineForumPostEntity>

    @Query("DELETE FROM offline_forum_posts WHERE id = :postId")
    suspend fun deleteOfflinePost(postId: String)
}
