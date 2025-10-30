package com.team21.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_posts")
data class MyPostEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val title: String,
    val thumbnailUrl: String,
    val price: Double,
    val updatedAt: Long    // epoch millis (para last-write-wins)
)