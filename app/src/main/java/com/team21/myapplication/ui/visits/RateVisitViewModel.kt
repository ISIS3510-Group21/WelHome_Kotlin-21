package com.team21.myapplication.ui.visits

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.cache.ArrayMapCacheProvider
import com.team21.myapplication.cache.PendingRating
import com.team21.myapplication.data.repository.BookingRepository
import com.team21.myapplication.utils.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface RateVisitUiState {
    object Idle : RateVisitUiState
    object Loading : RateVisitUiState
    object Success : RateVisitUiState
    data class Error(val message: String?) : RateVisitUiState
    object Offline : RateVisitUiState
}

class RateVisitViewModel(application: Application) : AndroidViewModel(application) {

    private val bookingRepository: BookingRepository = BookingRepository()
    private val networkMonitor = NetworkMonitor.get(application)

    private val _uiState = MutableStateFlow<RateVisitUiState>(RateVisitUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun submitRating(visitId: String, rating: Float, comment: String) {
        viewModelScope.launch {
            if (networkMonitor.isOnline.first()) {
                _uiState.value = RateVisitUiState.Loading
                try {
                    // IO y Main combinadas: persistencia en IO, UI en Main
                    withContext(Dispatchers.IO) {
                        bookingRepository.rateVisit(visitId, rating, comment)
                    }
                    _uiState.value = RateVisitUiState.Success
                } catch (e: Exception) {
                    _uiState.value = RateVisitUiState.Error(e.message)
                }
            } else {
                val pendingRating = PendingRating(visitId, rating, comment)
                ArrayMapCacheProvider.pendingRatingsCache.put(visitId, pendingRating)
                Log.d("RateVisitViewModel", "No network. Rating for visit $visitId cached.")
                // Navigate back immediately after caching
                _uiState.value = RateVisitUiState.Offline
            }
        }
    }

    fun resetState() {
        _uiState.value = RateVisitUiState.Idle
    }
}
