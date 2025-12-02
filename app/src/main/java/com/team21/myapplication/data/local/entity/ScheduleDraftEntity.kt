package com.team21.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_drafts")
data class ScheduleDraftEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val housingId: String,
    val propertyTitle: String,
    val propertyThumbnail: String,
    val timestamp: Long   // epochMillis del slot
)
