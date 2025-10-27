package com.team21.myapplication.ui.visits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VisitsViewModel: ViewModel() {
    private val repository: BookingRepository = BookingRepository()
    private val _state = MutableStateFlow(VisitsState())
    val state: StateFlow<VisitsState> = _state

    init {
        loadBookings()
    }

    private fun loadBookings() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val bookings = repository.getUserBookings()
            _state.value = _state.value.copy(visits = bookings, isLoading = false)
        }
    }
}