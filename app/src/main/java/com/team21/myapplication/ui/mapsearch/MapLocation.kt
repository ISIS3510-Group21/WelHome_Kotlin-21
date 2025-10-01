package com.team21.myapplication.ui.mapsearch

import com.google.android.gms.maps.model.LatLng

data class MapLocation(
    val title: String,
    val position: LatLng,
    val rating: Float,
    val price: String,
    val imageUrl: String
)