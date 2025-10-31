package com.team21.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

@Entity(
    tableName = "forum_post",
    foreignKeys = [ForeignKey(
        entity = ThreadForumEntity::class,
        parentColumns = ["id"],
        childColumns = ["threadId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ForumPostEntity(
    @PrimaryKey
    val id: String,
    val threadId: String, // Foreign key to ThreadForumEntity
    val content: String,
    val positiveVotes: Int,
    val negativeVotes: Int,
    val creationDate: Timestamp? = null,
    val user: String,
    val userName: String,
    val userPhoto: String
)
