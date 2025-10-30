package com.team21.myapplication.ui.mapsearch

import com.google.android.gms.maps.model.LatLng

data class MapState(
    val userLocation: LatLng = LatLng(4.60330, -74.06512),
    val locations: List<MapLocation> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isOffline: Boolean = false,
    val mapSnapshotPath: String? = null
)