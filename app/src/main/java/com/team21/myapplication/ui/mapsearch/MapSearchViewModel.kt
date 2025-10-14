package com.team21.myapplication.ui.mapsearch

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.team21.myapplication.data.repository.HousingPostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapSearchViewModel: ViewModel() {
    private val repository: HousingPostRepository = HousingPostRepository()
    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state

    init {
        loadLocations()
    }

    private fun loadLocations() {
        viewModelScope.launch {
            val posts = repository.getHousingPosts()

            val locations = posts.mapNotNull { post ->
                post.location?.let { loc ->
                    MapLocation(
                        id = post.id,
                        title = post.title,
                        position = LatLng(loc.lat, loc.lng),
                        rating = post.rating,
                        price = "$${post.price}/month",
                        imageUrl = post.thumbnail
                    )
                }
            }
            _state.value = _state.value.copy(locations = locations, isLoading = false)
            reorderPostsByDistance(_state.value.userLocation)
        }
    }

    fun onUserLocationReceived(location: LatLng) {
        _state.value = _state.value.copy(userLocation = location)
        reorderPostsByDistance(location)

    }

    private fun reorderPostsByDistance(userLocation: LatLng) {
        val sorted = _state.value.locations.sortedBy { location ->
            distanceBetween(
                userLocation.latitude,
                userLocation.longitude,
                location.position.latitude,
                location.position.longitude
            )
        }
        _state.update { it.copy(locations = sorted) }
    }

    private fun distanceBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val result = FloatArray(1)
        Location.distanceBetween(
            lat1,
            lng1,
            lat2,
            lng2,
            result
        )
        return result[0]
    }
}