package com.team21.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "draft_posts")
data class DraftPostEntity(
    @PrimaryKey val id: String,            // UUID del borrador
    val title: String,
    val description: String,
    val price: Double,
    val address: String,
    val selectedTagId: String?,
    val amenitiesIdsCsv: String,
    val createdAtMillis: Long,
    val ownerId: String
)
