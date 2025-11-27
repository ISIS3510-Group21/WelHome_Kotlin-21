package com.team21.myapplication.ui.ownerVisits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.model.OwnerScheduledVisit
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.data.repository.BookingRepository
import com.team21.myapplication.data.repository.BookingScheduleRepository
import com.team21.myapplication.data.repository.HousingPostRepository
import com.team21.myapplication.data.repository.OwnerUserRepository
import com.team21.myapplication.data.repository.StudentUserRepository
import com.team21.myapplication.ui.ownerVisits.state.OwnerVisitsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class OwnerVisitsViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val ownerRepository = OwnerUserRepository()
    private val bookingRepository = BookingRepository()
    private val studentRepository = StudentUserRepository()
    private val scheduleRepository = BookingScheduleRepository()
    private val housingRepository = HousingPostRepository()

    private val _state = MutableStateFlow(OwnerVisitsState())
    val state: StateFlow<OwnerVisitsState> = _state.asStateFlow()

    init {
        loadOwnerVisits()
    }

    fun loadOwnerVisits() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                // 1. Obtener el ID del owner actual
                val ownerId = authRepository.getCurrentUserId()
                if (ownerId == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "User not logged in"
                    )
                    return@launch
                }

                // 2. Obtener los IDs de las propiedades del owner
                val housingIdsResult = ownerRepository.getOwnerHousingIds(ownerId)
                if (housingIdsResult.isFailure) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Failed to load properties"
                    )
                    return@launch
                }

                val housingIds = housingIdsResult.getOrNull() ?: emptyList()
                if (housingIds.isEmpty()) {
                    _state.value = _state.value.copy(
                        visits = emptyList(),
                        isLoading = false
                    )
                    return@launch
                }

                // 3. Obtener detalles básicos de las propiedades (título y thumbnail)
                val housingDetailsMap = housingRepository.getHousingBasicDetails(housingIds)

                // 4. Obtener todos los BOOKINGS confirmados
                val bookings = bookingRepository.getBookingsByHousingIds(housingIds)

                // 5. Enriquecer cada booking con el nombre del visitante
                val confirmedVisits = bookings.map { booking ->
                    val (visitorName, visitorPhotoUrl) = studentRepository.getStudentBasicInfo(
                        booking.user
                    )

                    // Convertir Timestamp a formato legible
                    val date = booking.date.toDate()
                    val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.ENGLISH)
                    val formattedDate = dateFormat.format(date)

                    // Extraer SOLO la hora y formatearla con AM/PM
                    val timeRange = if (booking.slot.isNotBlank()) {
                        val raw = booking.slot.trim()

                        // Si viene algo tipo "2025-11-30 09:00" → nos quedamos con "09:00"
                        val timePart = if (raw.contains(" ")) {
                            raw.substringAfterLast(" ")
                        } else {
                            raw
                        }

                        // Intentar parsear "09:00" como 24h para luego formatear a "9:00 AM"
                        val parsed = try {
                            SimpleDateFormat("HH:mm", Locale.ENGLISH).parse(timePart)
                        } catch (e: Exception) {
                            null
                        }

                        if (parsed != null) {
                            SimpleDateFormat("h:mm a", Locale.ENGLISH).format(parsed)
                        } else {
                            // Si por alguna razón no se puede parsear, dejamos tal cual
                            timePart
                        }
                    } else {
                        // Si slot está vacío, tomar la hora desde el Timestamp y formatear "h:mm a"
                        val timeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH)
                        timeFormat.format(date)
                    }


                    OwnerScheduledVisit(
                        bookingId = booking.id,
                        date = booking.date,
                        timeRange = timeRange,
                        propertyName = booking.housingTitle,
                        visitorName = visitorName,
                        visitorPhotoUrl = visitorPhotoUrl,
                        propertyImageUrl = booking.thumbnail,
                        status = booking.state,
                        timestamp = booking.date.toDate().time,
                        isAvailable = false  // Es un booking confirmado
                    )
                }

                // 6. Obtener todos los SLOTS DISPONIBLES (no reservados)
                val availableSlots = scheduleRepository.getAvailableSlotsByHousingIds(
                    housingIds,
                    housingDetailsMap
                )

                // 7. Convertir slots disponibles a OwnerScheduledVisit
                val availableVisits = availableSlots.map { slot ->
                    val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.ENGLISH)
                    val formattedDate = dateFormat.format(slot.timestamp.toDate())

                    val timeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH)
                    val formattedTime = timeFormat.format(slot.timestamp.toDate())

                    OwnerScheduledVisit(
                        bookingId = "available_${slot.housingId}_${slot.timestamp.seconds}",
                        date = slot.timestamp,
                        timeRange = formattedTime,
                        propertyName = slot.housingTitle,
                        visitorName = "",  // No hay visitante aún
                        visitorPhotoUrl = null,
                        propertyImageUrl = slot.thumbnail,
                        status = "Available",
                        timestamp = slot.timestamp.toDate().time,
                        isAvailable = true  // Es un slot disponible
                    )
                }

                // 8. Combinar ambas listas y ordenar por fecha
                val allVisits = (confirmedVisits + availableVisits).sortedBy { it.timestamp }

                _state.value = _state.value.copy(
                    visits = allVisits,
                    isLoading = false
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
}