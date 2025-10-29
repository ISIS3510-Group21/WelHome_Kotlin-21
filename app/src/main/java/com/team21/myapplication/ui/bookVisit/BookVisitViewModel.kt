package com.team21.myapplication.ui.bookVisit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.ui.bookVisit.state.BookVisitUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.team21.myapplication.data.repository.BookingScheduleRepository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset


class BookVisitViewModel : ViewModel() {

    private val _state = MutableStateFlow(BookVisitUiState())
    val state: StateFlow<BookVisitUiState> = _state
    private val repo = BookingScheduleRepository()

    fun load(housingId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null, housingId = housingId)

        viewModelScope.launch {
            try {
                val availability = repo.getAvailabilityByDay(housingId)

                // Si no hay fecha seleccionada aún, seleccionamos HOY (UTC midnight)
                val initialSelectedMillis = _state.value.selectedDateMillis
                    ?: java.time.LocalDate.now(ZoneOffset.UTC)
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant()
                        .toEpochMilli()

                val newHours = buildHoursForSelectedDate(
                    availabilityByDay = availability,
                    selectedDateMillis = initialSelectedMillis
                )

                _state.value = _state.value.copy(
                    isLoading = false,
                    availabilityByDay = availability,
                    selectedDateMillis = initialSelectedMillis, // hoy por defecto
                    availableHours = newHours,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error loading availability"
                )
            }
        }
    }


    fun onDateSelected(millis: Long?) {
        val hours = buildHoursForSelectedDate(
            availabilityByDay = _state.value.availabilityByDay,
            selectedDateMillis = millis
        )
        _state.value = _state.value.copy(
            selectedDateMillis = millis,
            selectedHour = null,
            availableHours = hours
        )
    }


    fun onHourSelected(hour: String) {
        _state.value = _state.value.copy(selectedHour = hour)
    }

    fun onConfirm() {
        // En la siguiente fase conectaremos con Firebase/Cloud Functions
        // Por ahora no hace nada.
    }

    private fun buildHoursForSelectedDate(
        availabilityByDay: Map<LocalDate, List<LocalTime>>,
        selectedDateMillis: Long?
    ): List<String> {
        if (selectedDateMillis == null) return emptyList()
        val selectedDate = Instant.ofEpochMilli(selectedDateMillis).atZone(ZoneOffset.UTC).toLocalDate()
        val times = availabilityByDay[selectedDate].orEmpty()
        return times.map { t -> repo.formatHour(t) }
    }

}
