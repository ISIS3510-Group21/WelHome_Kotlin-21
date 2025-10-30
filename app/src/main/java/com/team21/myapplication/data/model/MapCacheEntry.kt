package com.team21.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import com.team21.myapplication.ui.mapsearch.MapLocation

@Entity(tableName = "map_cache")
data class MapCacheEntry(
    @PrimaryKey val id: Int = 1, // Singleton cache
    val userLatitude: Double,
    val userLongitude: Double,
    val mapSnapshotPath: String,
    val locations: List<MapLocation>
)
