package com.team21.myapplication.ui.mapsearch

import android.app.Application
import android.graphics.Bitmap
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.team21.myapplication.data.local.AppDatabase
import com.team21.myapplication.data.local.entity.MapCacheEntry
import com.team21.myapplication.data.repository.HousingPostRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileOutputStream

class MapSearchViewModel(application: Application) : AndroidViewModel(application) {

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

    // Estrategia: Corrutina con Dispatcher.IO → tareas de red y BD
    private fun fetchFromNetwork() {
        viewModelScope.launch(Dispatchers.IO) {
            // Llamada remota (operación de red → I/O)
            val posts = repository.getHousingPosts()

            // Procesamiento de datos en memoria → usa Default (CPU bound)
            val locations = withContext(Dispatchers.Default) {
                posts.mapNotNull { post ->
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
            }

            // 🔹 Actualización de estado → Main thread (UI safe)
            withContext(Dispatchers.Main) {
                _state.value = _state.value.copy(
                    locations = locations,
                    isLoading = false,
                    isOffline = false
                )
                reorderPostsByDistance(_state.value.userLocation)
            }
        }
    }

    // Estrategia: Corrutina con Dispatcher.IO → lectura desde Room
    private fun loadFromCache() {
        viewModelScope.launch(Dispatchers.IO) {
            val cachedData = mapCacheDao.getCache()
            if (cachedData != null) {
                //  Cambio de contexto a Main para actualizar UI
                withContext(Dispatchers.Main) {
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
    }

    // Estrategia: Corrutina anidada (I/O + Default)
    // Guardar snapshot del mapa (Bitmap → archivo)
    fun saveMapToCache(snapshot: Bitmap, userLocation: LatLng) {
        viewModelScope.launch(Dispatchers.IO) {
            // 🔹 Guardar snapshot en archivo (I/O)
            val snapshotPath = saveSnapshotToFile(snapshot)

            // 🔹 Guardar en base de datos (I/O)
            if (snapshotPath != null) {
                val cacheEntry = MapCacheEntry(
                    userLatitude = userLocation.latitude,
                    userLongitude = userLocation.longitude,
                    mapSnapshotPath = snapshotPath,
                    // 🔹 Procesamiento previo en Default (para limitar lista)
                    locations = withContext(Dispatchers.Default) {
                        state.value.locations.take(20)
                    }
                )
                mapCacheDao.save(cacheEntry)
            }
        }
    }

    // Estrategia: Corrutina con Dispatcher.IO → escritura de archivos
    private suspend fun saveSnapshotToFile(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        val context = getApplication<Application>().applicationContext
        val file = File(context.cacheDir, "map_snapshot.png")
        try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    // Reordenar elementos según distancia del usuario
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
        Location.distanceBetween(lat1, lng1, lat2, lng2, result)
        return result[0]
    }
}