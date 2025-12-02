package com.team21.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "owner_visit_cache")
data class OwnerVisitCacheEntity(
    @PrimaryKey val bookingId: String,
    val timestamp: Long,
    val timeRange: String,
    val propertyName: String,
    val visitorName: String,
    val propertyImageUrl: String,
    val status: String,
    val isAvailable: Boolean
)
