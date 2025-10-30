package com.team21.myapplication.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * [LOCAL STORAGE]
 * Snapshot that captures the essential UI data for the Detail screen.
 * Used for cache-first offline reads.
 */
@Entity(tableName = "detail_snapshots")
data class DetailSnapshotEntity(
    @PrimaryKey val housingId: String,

    val title: String,
    val rating: Double,
    val pricePerMonthLabel: String,
    val address: String,
    val ownerName: String,

    val imagesJson: String,
    val amenityLabelsJson: String,
    val roommateNamesJson: String,

    val roommateCount: Int,
    val reviewsCount: Int,
    val latitude: Double?,
    val longitude: Double?,
    val status: String,

    val updatedAtMs: Long            // For staleness/expiration policies if needed
)
