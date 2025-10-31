package com.team21.myapplication.ui.mapsearch

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.team21.myapplication.data.local.AppDatabase
import com.team21.myapplication.data.local.entity.MapCacheEntry
import com.team21.myapplication.data.repository.HousingPostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MapSearchViewModel(application: Application): AndroidViewModel(application) {
    private val repository: HousingPostRepository = HousingPostRepository()
    private val mapCacheDao = AppDatabase.getDatabase(application).mapCacheDao()

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state

    fun loadLocations(isOnline: Boolean) {
        if (isOnline) {
            fetchFromNetwork()
        } else {
            loadFromCache()
        }
    }

    private fun fetchFromNetwork() {
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
                        imageUrl = post.thumbnail.ifBlank {
                            "https://www.howtobogota.com/wp-content/uploads/2014/03/Santa_Barbara_Bogota-1024x768.jpg"
                        }
                    )
                }
            }
            _state.value = _state.value.copy(locations = locations, isLoading = false, isOffline = false)
            reorderPostsByDistance(_state.value.userLocation)
        }
    }

    private fun loadFromCache() {
        viewModelScope.launch {
            val cachedData = mapCacheDao.getCache()
            if (cachedData != null) {
                _state.value = _state.value.copy(
                    userLocation = LatLng(cachedData.userLatitude, cachedData.userLongitude),
                    locations = cachedData.locations,
                    mapSnapshotPath = cachedData.mapSnapshotPath,
                    isLoading = false,
                    isOffline = true
                )
            }
        }
    }

    fun saveMapToCache(snapshot: Bitmap, userLocation: LatLng) {
        viewModelScope.launch {
            val snapshotPath = saveSnapshotToFile(snapshot)
            if (snapshotPath != null) {
                val cacheEntry = MapCacheEntry(
                    userLatitude = userLocation.latitude,
                    userLongitude = userLocation.longitude,
                    mapSnapshotPath = snapshotPath,
                    locations = state.value.locations.take(20)
                )
                mapCacheDao.save(cacheEntry)
            }
        }
    }

    private fun saveSnapshotToFile(bitmap: Bitmap): String? {
        val context = getApplication<Application>().applicationContext
        val file = File(context.cacheDir, "map_snapshot.png")
        return try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            file.absolutePath
        } catch (e: Exception) {
            null
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