package com.team21.myapplication.ui.mapsearch

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MapSearchViewModel : ViewModel(){
    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state

    init {
        loadLocations()
    }

    private fun loadLocations(){
        val locations = listOf(
            MapLocation("Location 1", LatLng(4.60330, -74.06512)),
            MapLocation("Location 2", LatLng(4.6047238045939585, -74.06714261304849)),
            MapLocation("Location 3", LatLng(4.605385762083355, -74.0715433871152))
        )

        _state.value = _state.value.copy(locations = locations)


    }
}