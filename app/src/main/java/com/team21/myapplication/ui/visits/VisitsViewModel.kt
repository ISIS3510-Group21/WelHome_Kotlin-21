package com.team21.myapplication.ui.visits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.cache.ArrayMapCacheProvider
import com.team21.myapplication.cache.CacheProvider
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
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    loadBookings()
                } else {
                    loadBookingsFromCache()
                }
            }
        }
    }

    private fun loadBookings() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val bookings = repository.getUserBookings()
            updateCache(bookings)
            _state.value = _state.value.copy(visits = bookings, isLoading = false)
        }
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