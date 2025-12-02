package com.team21.myapplication.ui.ownerVisits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.local.AppDatabase
import com.team21.myapplication.data.local.entity.OwnerVisitCacheEntity
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
import com.google.firebase.firestore.ListenerRegistration
import com.team21.myapplication.sync.ScheduleUpdateBus
import java.util.Date

class OwnerVisitsViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val ownerRepository = OwnerUserRepository()
    private val bookingRepository = BookingRepository()
    private val studentRepository = StudentUserRepository()
    private val scheduleRepository = BookingScheduleRepository()
    private val housingRepository = HousingPostRepository()

    private val db = AppDatabase.getDatabase(application)
    private val ownerVisitCacheDao = db.ownerVisitCacheDao()

    private val scheduleDraftDao = db.scheduleDraftDao()
    private var bookingListeners: List<ListenerRegistration> = emptyList()
    private var listenersRegistered: Boolean = false

    private val _state = MutableStateFlow(OwnerVisitsState())
    val state: StateFlow<OwnerVisitsState> = _state.asStateFlow()

    init {
        loadOwnerVisits()
        observeScheduleUpdates()
    }

    fun loadOwnerVisits(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _state.value = _state.value.copy(
                    isLoading = true,
                    error = null
                )
            } else {
                // refresco silencioso: dejamos isLoading como está y solo limpiamos error
                _state.value = _state.value.copy(
                    error = null
                )
            }

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

                // Registrar listeners de cambios SOLO una vez
                if (!listenersRegistered) {
                    bookingListeners = bookingRepository.addBookingsChangeListenerForHousing(
                        housingIds
                    ) {
                        // Cuando Firestore nos avisa de un cambio, recargamos
                        // sin mostrar spinner (silent refresh)
                        viewModelScope.launch {
                            loadOwnerVisits(showLoading = false)
                        }
                    }
                    listenersRegistered = true
                }


                // 3. Obtener detalles básicos de las propiedades (título y thumbnail)
                val housingDetailsMap = housingRepository.getHousingBasicDetails(housingIds)

                // 4. Obtener todos los BOOKINGS confirmados
                val bookings = bookingRepository.getBookingsByHousingIds(housingIds)

                // filtrar
                val zone = java.time.ZoneId.of("America/Bogota")
                val todayStartMillis = java.time.LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()


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
                }.filter{visit ->
                    visit.timestamp >= todayStartMillis
                }

                // obtener drafts locales de horarios pendientes
                val draftEntities = scheduleDraftDao.getAll()

                val pendingVisits = draftEntities.map { draft ->
                    val date = Date(draft.timestamp)
                    val ts = com.google.firebase.Timestamp(date)

                    val timeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH)
                    val timeRange = timeFormat.format(date)

                    OwnerScheduledVisit(
                        bookingId = "draft_${draft.id}",
                        date = ts,
                        timeRange = timeRange,
                        propertyName = draft.propertyTitle,
                        visitorName = "",
                        visitorPhotoUrl = null,
                        propertyImageUrl = draft.propertyThumbnail,
                        status = "Pending",
                        timestamp = draft.timestamp,
                        isAvailable = true,
                        isPendingDraft = true
                    )
                }

                // 8. Combinar ambas listas y ordenar por fecha
                val allVisits = (confirmedVisits + availableVisits+ pendingVisits).sortedBy { it.timestamp }

                _state.value = _state.value.copy(
                    visits = allVisits,
                    isLoading = false
                )

                // persistir HOY + MAÑANA
                val (fromMillis, toMillis) = todayTomorrowRangeMillis()
                val todayTomorrow = allVisits.filter { it.timestamp in fromMillis..toMillis }

                // Guardar en Room en el mismo scope
                ownerVisitCacheDao.clearAll()
                ownerVisitCacheDao.insertAll(todayTomorrow.map { it.toCacheEntity() })

            } catch (e: Exception) {
                // Intentar recuperar HOY + MAÑANA desde Room
                val (fromMillis, toMillis) = todayTomorrowRangeMillis()
                val cached = ownerVisitCacheDao.getVisitsInRange(fromMillis, toMillis)
                    .map { it.toDomain() }

                // Drafts locales de horarios pendientes (solo acceso a Room, no usa red)
                val draftEntities = scheduleDraftDao.getAll()
                val pendingVisits = draftEntities.map { draft ->
                    val date = Date(draft.timestamp)
                    val ts = com.google.firebase.Timestamp(date)

                    val timeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH)
                    val timeRange = timeFormat.format(date)

                    OwnerScheduledVisit(
                        bookingId = "draft_${draft.id}",
                        date = ts,
                        timeRange = timeRange,
                        propertyName = draft.propertyTitle,
                        visitorName = "",
                        visitorPhotoUrl = null,
                        propertyImageUrl = draft.propertyThumbnail,
                        status = "Pending",
                        timestamp = draft.timestamp,
                        isAvailable = true,
                        isPendingDraft = true
                    )
                }

                if (cached.isNotEmpty() || pendingVisits.isNotEmpty()) {
                    val merged = (cached + pendingVisits).sortedBy { it.timestamp }
                    _state.value = _state.value.copy(
                        visits = merged,
                        isLoading = false,
                        error = null
                    )
                } else {
                    // Sin cache: sí mostramos error
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }

        }
    }

    override fun onCleared() {
        super.onCleared()
        // Remover todos los listeners de Firestore
        bookingListeners.forEach { it.remove() }
        bookingListeners = emptyList()
    }

    private fun observeScheduleUpdates() {
        viewModelScope.launch {
            ScheduleUpdateBus.updates.collect {
                // recarga silenciosa, sin spinner
                loadOwnerVisits(showLoading = false)
            }
        }
    }

}

private fun todayTomorrowRangeMillis(): Pair<Long, Long> {
    val zone = java.time.ZoneId.of("America/Bogota")
    val today = java.time.LocalDate.now(zone)
    val todayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
    val tomorrowEnd = today.plusDays(1)
        .plusDays(1) // fin de mañana = inicio de pasado mañana
        .atStartOfDay(zone)
        .toInstant()
        .toEpochMilli() - 1
    return todayStart to tomorrowEnd
}

private fun OwnerScheduledVisit.toCacheEntity(): OwnerVisitCacheEntity {
    return OwnerVisitCacheEntity(
        bookingId = bookingId,
        timestamp = timestamp,
        timeRange = timeRange,
        propertyName = propertyName,
        visitorName = visitorName,
        propertyImageUrl = propertyImageUrl,
        status = status,
        isAvailable = isAvailable
    )
}

private fun OwnerVisitCacheEntity.toDomain(): OwnerScheduledVisit {
    return OwnerScheduledVisit(
        bookingId = bookingId,
        date = com.google.firebase.Timestamp(java.util.Date(timestamp)),
        timeRange = timeRange,
        propertyName = propertyName,
        visitorName = visitorName,
        propertyImageUrl = propertyImageUrl,
        status = status,
        timestamp = timestamp,
        isAvailable = isAvailable
    )


}
