
package com.team21.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_forum_posts")
data class OfflineForumPostEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val threadId: String?,
    val newThreadTitle: String?,
    val newThreadDescription: String?,
    val content: String,
    val user: String,
    val userName: String,
    val userPhoto: String,
    val timestamp: Long = System.currentTimeMillis()
)
