package com.team21.myapplication.ui.postBookingSchedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.data.repository.BookingScheduleRepository
import com.team21.myapplication.data.repository.HousingPostRepository
import com.team21.myapplication.data.repository.OwnerUserRepository
import com.team21.myapplication.ui.components.cards.PropertyOptionUi
import com.team21.myapplication.ui.postBookingSchedule.state.PostBookingScheduleUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PostBookingScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val ownerRepository = OwnerUserRepository()
    private val housingRepository = HousingPostRepository()
    private val bookingScheduleRepository = BookingScheduleRepository()

    private val _state = MutableStateFlow(PostBookingScheduleUiState())
    val state: StateFlow<PostBookingScheduleUiState> = _state.asStateFlow()

    private val zone: ZoneId = ZoneId.of("America/Bogota")   // UTC-5
    private val hourFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    // Horas base del día
    private val baseHours: List<LocalTime> = (7..20).map { h -> LocalTime.of(h, 0) }

    init {
        loadOwnerProperties()
    }

    private fun loadOwnerProperties() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val ownerId = authRepository.getCurrentUserId()
                if (ownerId == null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "User not logged in"
                        )
                    }
                    return@launch
                }

                // 1) Obtener housingIds del owner
                val housingIdsResult = ownerRepository.getOwnerHousingIds(ownerId)
                if (housingIdsResult.isFailure) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load properties"
                        )
                    }
                    return@launch
                }

                val housingIds = housingIdsResult.getOrNull().orEmpty()
                if (housingIds.isEmpty()) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            properties = emptyList()
                        )
                    }
                    return@launch
                }

                // 2) Obtener título + thumbnail de cada propiedad
                val basicDetails = housingRepository.getHousingBasicDetailsWithAddress(housingIds)

                val options = housingIds.mapNotNull { id ->
                    val pair = basicDetails[id] ?: return@mapNotNull null
                    val (title, thumbnail, address) = pair
                    PropertyOptionUi(
                        id = id,
                        title = title,
                        subtitle = address,            // si luego quieres dirección, se puede extender
                        thumbnailUrl = thumbnail
                    )
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        properties = options
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun onPropertySelected(housingId: String) {
        viewModelScope.launch {
            val option = _state.value.properties.firstOrNull { it.id == housingId }
            _state.update {
                it.copy(
                    selectedPropertyId = housingId,
                    selectedPropertyTitle = option?.title.orEmpty(),
                    selectedPropertySubtitle = option?.subtitle.orEmpty(),
                    selectedPropertyThumbnail = option?.thumbnailUrl.orEmpty(),
                    selectedHours = emptySet()        // resetear selección al cambiar propiedad
                )
            }

            refreshAvailableHoursForSelection(
                housingId = housingId,
                dateMillis = _state.value.selectedDateMillis
            )
        }
    }

    fun onDateSelected(dateMillis: Long?) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    selectedDateMillis = dateMillis,
                    selectedHours = emptySet()        // resetear selección al cambiar fecha
                )
            }

            refreshAvailableHoursForSelection(
                housingId = _state.value.selectedPropertyId,
                dateMillis = dateMillis
            )
        }
    }

    fun onToggleHour(hour: String) {
        _state.update { current ->
            val newSet = current.selectedHours.toMutableSet().apply {
                if (contains(hour)) remove(hour) else add(hour)
            }
            current.copy(selectedHours = newSet)
        }
    }

    fun saveSchedule() {
        viewModelScope.launch {
            val current = _state.value
            val housingId = current.selectedPropertyId
            val dateMillis = current.selectedDateMillis
            val selectedHours = current.selectedHours

            if (housingId == null || dateMillis == null || selectedHours.isEmpty()) {
                _state.update {
                    it.copy(
                        snackbarMessage = "Please select a property, date and at least one time slot.",
                        snackbarError = true
                    )
                }
                return@launch
            }

            try {
                _state.update { it.copy(isSaving = true, snackbarMessage = null) }

                val selectedDate = Instant.ofEpochMilli(dateMillis)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()

                // convertir "HH:mm" -> LocalTime
                val times = selectedHours.mapNotNull { h ->
                    try {
                        LocalTime.parse(h, hourFormatter)
                    } catch (_: Exception) {
                        null
                    }
                }

                val result = bookingScheduleRepository.addAvailableSlots(
                    housingId = housingId,
                    date = selectedDate,
                    times = times
                )

                if (result.isSuccess) {
                    val formattedDate = selectedDate.toString() // yyyy-MM-dd;

                    _state.update {
                        it.copy(
                            isSaving = false,
                            selectedHours = emptySet(),
                            snackbarMessage = "Schedule saved for $formattedDate",
                            snackbarError = false
                        )
                    }

                    // actualizar lista de horas disponibles (ya no deberían salir las recien creadas)
                    refreshAvailableHoursForSelection(
                        housingId = housingId,
                        dateMillis = dateMillis
                    )
                } else {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            snackbarMessage = result.exceptionOrNull()?.message
                                ?: "Error saving schedule",
                            snackbarError = true
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        snackbarMessage = e.message ?: "Error saving schedule",
                        snackbarError = true
                    )
                }
            }
        }
    }

    fun consumeSnackbarMessage() {
        _state.update {
            it.copy(snackbarMessage = null)
        }
    }

    /**
     * Calcula las horas disponibles para mostrar en UI, excluyendo los slots
     * que ya existen en BookingSchedule para ese housingId y fecha.
     */
    private suspend fun refreshAvailableHoursForSelection(
        housingId: String?,
        dateMillis: Long?
    ) {
        if (housingId == null || dateMillis == null) {
            _state.update { it.copy(availableHours = emptyList()) }
            return
        }

        try {
            val selectedDate = Instant.ofEpochMilli(dateMillis)
                .atZone(ZoneOffset.UTC)   // el millis viene como 00:00 UTC
                .toLocalDate()

            val availabilityByDay = bookingScheduleRepository.getAvailabilityByDay(housingId)
            val alreadyPostedTimes: List<LocalTime> = availabilityByDay[selectedDate] ?: emptyList()

            val remainingTimes = baseHours.filter { t -> !alreadyPostedTimes.contains(t) }

            val formatted = remainingTimes.map { t -> t.format(hourFormatter) }

            _state.update {
                it.copy(
                    availableHours = formatted
                )
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    availableHours = emptyList(),
                    error = e.message
                )
            }
        }
    }
}

class PostBookingScheduleViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostBookingScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PostBookingScheduleViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
    }
}


