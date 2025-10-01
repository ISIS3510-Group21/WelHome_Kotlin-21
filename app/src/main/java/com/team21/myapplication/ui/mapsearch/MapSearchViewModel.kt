package com.team21.myapplication.ui.mapsearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.team21.myapplication.data.repository.HousingPostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

            val locations = posts.map { post ->
                post.location.let { location ->
                    MapLocation(
                        title = post.title,
                        position = LatLng(location.lat, location.lng),
                        rating = post.rating,
                        price = "$" + post.price.toString() + "/month"
                    )
                }
            }
            _state.value = _state.value.copy(locations = locations, isLoading = false)
        }
    }
}