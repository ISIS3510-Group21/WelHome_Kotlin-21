package com.team21.myapplication.ui.visits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RateVisitUiState {
    object Idle : RateVisitUiState
    object Loading : RateVisitUiState
    object Success : RateVisitUiState
    data class Error(val message: String?) : RateVisitUiState
}

class RateVisitViewModel : ViewModel() {

    private val bookingRepository: BookingRepository = BookingRepository()

    private val _uiState = MutableStateFlow<RateVisitUiState>(RateVisitUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun rateVisit(visitId: String, rating: Float, comment: String) {
        viewModelScope.launch {
            _uiState.value = RateVisitUiState.Loading
            try {
                bookingRepository.rateVisit(visitId, rating, comment)
                _uiState.value = RateVisitUiState.Success
            } catch (e: Exception) {
                _uiState.value = RateVisitUiState.Error(e.message)
            }
        }
    }
}