package com.team21.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

@Entity(tableName = "thread_forum")
data class ThreadForumEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val creationDate: Timestamp? = null,
    val commentQuantity: Int,
    val photoPath: String
)
