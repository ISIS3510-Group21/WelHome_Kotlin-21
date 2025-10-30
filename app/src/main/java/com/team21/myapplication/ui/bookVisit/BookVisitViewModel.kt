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
import com.team21.myapplication.data.repository.BookingRepository
import com.team21.myapplication.data.repository.HousingPostRepository
import java.time.format.DateTimeFormatter

class BookVisitViewModel : ViewModel() {

    private val _state = MutableStateFlow(BookVisitUiState())
    val state: StateFlow<BookVisitUiState> = _state
    private val repo = BookingScheduleRepository()
    private val appZone: ZoneId = ZoneId.of("America/Bogota")
    private val bookingRepo = BookingRepository()
    private val housingRepo = HousingPostRepository()

    fun load(housingId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null, housingId = housingId)

        viewModelScope.launch {
            try {
                val availability = repo.getAvailabilityByDay(housingId)

                // cargar título y thumbnail
                val housing = housingRepo.getHousingPostById(housingId)
                val title = housing?.post?.title.orEmpty()
                val thumb = housing?.post?.thumbnail.orEmpty()

                // Si no hay fecha seleccionada aún se selecciona 'hoy'
                val initialSelectedMillis = _state.value.selectedDateMillis
                    ?: LocalDate.now(appZone)
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
                    selectedDateMillis = initialSelectedMillis,
                    availableHours = newHours,
                    housingTitle = title,
                    housingThumbnail = thumb,
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
        val s = _state.value
        val millis = s.selectedDateMillis ?: return
        val hour = s.selectedHour ?: return
        val housingId = s.housingId

        _state.value = s.copy(isConfirming = true, error = null, successMessage = null)

        viewModelScope.launch {
            try {
                // 1) Crear booking + eliminar slot en Firestore
                val bookingId = bookingRepo.createBookingAndConsumeSlot(
                    housingId = housingId,
                    housingTitle = s.housingTitle,
                    thumbnail = s.housingThumbnail,
                    selectedDateMillis = millis,
                    selectedHourString = hour
                )

                // 2) Actualizar la disponibilidad local: quitar esa hora del día seleccionado
                val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                val updatedMap = s.availabilityByDay.toMutableMap()
                val times = (updatedMap[selectedDate] ?: emptyList()).toMutableList()
                val toRemove = LocalTime.parse(hour, DateTimeFormatter.ofPattern("H:mm"))
                times.remove(toRemove)
                if (times.isEmpty()) {
                    updatedMap.remove(selectedDate)
                } else {
                    updatedMap[selectedDate] = times.sorted()
                }

                // 3) Recalcular la grilla para el día seleccionado
                val newHours = buildHoursForSelectedDate(updatedMap, millis)

                _state.value = _state.value.copy(
                    isConfirming = false,
                    availabilityByDay = updatedMap,
                    availableHours = newHours,
                    selectedHour = null,
                    successMessage = "Visit scheduled! (id: $bookingId)"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isConfirming = false,
                    error = e.message ?: "Error confirming booking"
                )
            }
        }
    }

    fun onSuccessAcknowledged() {
        _state.value = _state.value.copy(successMessage = null)
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
