package com.team21.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.team21.myapplication.ui.mapsearch.MapLocation

@Entity(tableName = "map_cache")
data class MapCacheEntry(
    @PrimaryKey val id: Int = 1, // Singleton cache
    val userLatitude: Double,
    val userLongitude: Double,
    val mapSnapshotPath: String,
    val locations: List<MapLocation>
)