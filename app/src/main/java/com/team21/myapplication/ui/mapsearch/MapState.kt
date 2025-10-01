package com.team21.myapplication.ui.mapsearch

data class MapState(
    val locations: List<MapLocation> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)