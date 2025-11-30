package com.team21.myapplication.ui.visits

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.cache.ArrayMapCacheProvider
import com.team21.myapplication.data.model.Booking
import com.team21.myapplication.data.repository.BookingRepository
import com.team21.myapplication.utils.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VisitsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BookingRepository = BookingRepository()
    private val _state = MutableStateFlow(VisitsState())
    val state: StateFlow<VisitsState> = _state.asStateFlow()

    private val networkMonitor = NetworkMonitor.get(application)
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
    private val bookingCache: ArrayMapCacheProvider<String, Booking> = ArrayMapCacheProvider()

    init {
        // On init, check for pending ratings and then decide what to load based on connectivity
        updatePendingRatingsState()
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    sendPendingRatings()
                    loadBookings()
                } else {
                    loadBookingsFromCache()
                }
            }
        }
    }

    fun loadBookings() {
        _state.value = _state.value.copy(isLoading = true)
        updatePendingRatingsState() // Always update pending status when loading
        viewModelScope.launch {
            val bookings = repository.getUserBookings()
            updateCache(bookings)
            _state.value = _state.value.copy(visits = bookings, isLoading = false)
        }
    }

    private fun sendPendingRatings() {
        val pending = ArrayMapCacheProvider.pendingRatingsCache.values().toList()
        if (pending.isNotEmpty()) {
            viewModelScope.launch {
                Log.d("VisitsViewModel", "Network online. Sending ${pending.size} pending ratings.")
                pending.forEach { rating ->
                    try {
                        repository.rateVisit(rating.visitId, rating.rating, rating.comment)
                        ArrayMapCacheProvider.pendingRatingsCache.remove(rating.visitId)
                        Log.d("VisitsViewModel", "Sent pending rating for visit ${rating.visitId}")
                    } catch (e: Exception) {
                        Log.e("VisitsViewModel", "Failed to send pending rating for ${rating.visitId}", e)
                    }
                }
                // After sync, update pending state and reload bookings to get fresh data
                updatePendingRatingsState()
                if (networkMonitor.isOnline.value) {
                    loadBookings()
                }
            }
        }
    }
    
    private fun updatePendingRatingsState() {
        val pendingMap = ArrayMapCacheProvider.pendingRatingsCache.values()
            .associate { it.visitId to it.rating }
        _state.value = _state.value.copy(pendingRatings = pendingMap)
    }

    private fun loadBookingsFromCache() {
        val cachedBookings = bookingCache.values().toList()
        _state.value = _state.value.copy(visits = cachedBookings, isLoading = false)
    }

    private fun updateCache(bookings: List<Booking>) {
        bookingCache.clear()
        for (booking in bookings) {
            bookingCache.put(booking.id, booking)
        }
    }
}
