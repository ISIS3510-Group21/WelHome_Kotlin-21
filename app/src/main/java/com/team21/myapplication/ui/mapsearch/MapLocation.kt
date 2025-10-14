package com.team21.myapplication.ui.mapsearch

import com.google.android.gms.maps.model.LatLng

data class MapLocation(
    val id: String,
    val title: String,
    val position: LatLng,
    val rating: Double,
    val price: String,
    val imageUrl: String
)