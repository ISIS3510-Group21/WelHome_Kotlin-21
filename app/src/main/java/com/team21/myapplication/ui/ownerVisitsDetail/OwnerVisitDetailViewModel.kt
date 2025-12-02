package com.team21.myapplication.ui.ownerVisitsDetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.BookingRepository
import com.team21.myapplication.data.repository.StudentUserRepository
import com.team21.myapplication.ui.ownerVisitsDetail.state.OwnerVisitDetailState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.ListenerRegistration
import com.team21.myapplication.data.model.Booking

class OwnerVisitDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val bookingRepository = BookingRepository()
    private val studentRepository = StudentUserRepository()

    private var bookingListener: ListenerRegistration? = null

    private val _state = MutableStateFlow(OwnerVisitDetailState())
    val state: StateFlow<OwnerVisitDetailState> = _state.asStateFlow()

    /**
     * Carga el detalle de una visita
     * @param bookingId ID del booking o ID compuesto para slots disponibles
     * @param isAvailable Si es un slot disponible (true) o un booking real (false)
     * @param propertyImageUrl URL de la imagen de la propiedad
     * @param propertyName Nombre de la propiedad
     * @param visitDate Fecha de la visita (ya formateada)
     * @param visitTime Hora de la visita (ya formateada)
     */
    fun loadVisitDetail(
        bookingId: String,
        isAvailable: Boolean,
        propertyImageUrl: String,
        propertyName: String,
        visitDate: String,
        visitTime: String
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                if (isAvailable) {
                    // Es un slot disponible - no hay booking real
                    _state.value = _state.value.copy(
                        isLoading = false,
                        propertyImageUrl = propertyImageUrl,
                        visitStatus = VisitStatus.AVAILABLE,
                        visitDate = visitDate,
                        visitTime = visitTime,
                        visitorName = "",
                        visitorNationality = "",
                        visitorPhotoUrl = null,
                        visitorFeedback = null,
                        visitorRating = null,
                        ownerComment = "",
                        ownerCommentDraft = "",
                        bookingId = bookingId,
                        isAvailable = true
                    )
                } else {
                    // Es un booking real - obtener detalles de Firestore
                    loadBookingDetails(bookingId, propertyImageUrl, visitDate, visitTime)
                    if (bookingListener == null) {
                        bookingListener = bookingRepository.addBookingChangeListener(bookingId) { updatedBooking ->
                            if (updatedBooking != null) {
                                // Usamos la misma función que en la carga inicial
                                applyBookingToState(updatedBooking, propertyImageUrl, visitDate, visitTime)
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error loading visit details"
                )
            }
        }
    }

    private suspend fun loadBookingDetails(
        bookingId: String,
        propertyImageUrl: String,
        visitDate: String,
        visitTime: String
    ) {
        try {
            // Obtener el booking directamente por ID
            val booking = bookingRepository.getBookingById(bookingId)

            if (booking == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Booking not found"
                )
                return
            }
            // Reutilizamos la misma lógica que usa el listener
            applyBookingToState(booking, propertyImageUrl, visitDate, visitTime)

        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = e.message ?: "Error loading booking details"
            )
        }
    }

    private fun applyBookingToState(
        booking: Booking,
        propertyImageUrl: String,
        visitDate: String,
        visitTime: String
    ) {
        // Obtener información del visitante
        viewModelScope.launch {
            try {
                val (visitorName, visitorPhotoUrl) =
                    studentRepository.getStudentBasicInfo(booking.user)

                android.util.Log.d("OwnerVisitDetailVM", "visitorPhotoUrl = $visitorPhotoUrl")

                val studentUser = studentRepository.getStudentUser(booking.user)
                val visitorNationality = studentUser?.nationality ?: "Unknown"

                val visitStatus = when {
                    booking.state.equals("Completed", ignoreCase = true) -> VisitStatus.COMPLETED
                    booking.state.equals("Missed", ignoreCase = true) -> VisitStatus.MISSED
                    booking.state.equals("Scheduled", ignoreCase = true) && booking.confirmedVisit -> VisitStatus.SCHEDULED
                    else -> VisitStatus.CONFIRMED
                }

                val rating = if (booking.rating > 0) booking.rating.toInt() else null

                _state.value = _state.value.copy(
                    isLoading = false,
                    propertyImageUrl = propertyImageUrl.ifBlank { booking.thumbnail },
                    visitStatus = visitStatus,
                    visitDate = visitDate,
                    visitTime = visitTime,
                    visitorName = visitorName,
                    visitorNationality = visitorNationality,
                    visitorPhotoUrl = visitorPhotoUrl,
                    visitorFeedback = booking.userComment.ifBlank { null },
                    visitorRating = rating,
                    ownerComment = booking.ownerComment,
                    ownerCommentDraft = booking.ownerComment,
                    bookingId = booking.id,  // asegúrate de tener id en Booking
                    isAvailable = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error loading booking details"
                )
            }
        }
    }


    fun updateOwnerComment(comment: String) {
        _state.value = _state.value.copy(ownerCommentDraft = comment)
    }

    fun setEditingOwnerComment(isEditing: Boolean) {
        val current = _state.value
        _state.value = current.copy(
            isEditingOwnerComment = isEditing,
            // Si salimos del modo edición (Cancel), el borrador vuelve al valor guardado
            ownerCommentDraft = if (!isEditing) current.ownerComment else current.ownerCommentDraft
        )
    }



    /**
     * Guarda el comentario del owner en la colección Booking.
     * Devuelve true si todo salió bien, false si hubo error.
     */
    suspend fun saveOwnerComment(): Boolean {
        val currentState = _state.value
        val bookingId = currentState.bookingId
        val comment = currentState.ownerCommentDraft   // usar borrador

        // Si por alguna razón no tenemos bookingId, no podemos guardar
        if (bookingId.isBlank()) {
            _state.value = currentState.copy(
                commentSaveMessage = "Could not identify this visit to save the comment.",
                commentSaveError = true
            )
            return false
        }

        return try {
            // Marcamos que estamos guardando
            _state.value = currentState.copy(
                isSavingComment = true,
                commentSaveMessage = null
            )

            // Llamada al repositorio: crea/actualiza el campo ownerComment
            bookingRepository.updateOwnerComment(bookingId, comment)

            // Actualizamos el estado con éxito
            _state.value = _state.value.copy(
                isSavingComment = false,
                commentSaveMessage = "Comment saved successfully.",
                commentSaveError = false,
                isEditingOwnerComment = false,   // salir del modo edición
                ownerComment = comment,           // lo guardado en DB
                ownerCommentDraft = comment       // sincroniza borrador
            )
            true

        } catch (e: Exception) {
            // En caso de error, informamos a la UI
            _state.value = _state.value.copy(
                isSavingComment = false,
                commentSaveMessage = "Could not save your comment. Please try again.",
                commentSaveError = true
            )
            false
        }
    }

    /**
     * Se llama desde la UI cuando ya se mostró el snackbar,
     * para limpiar el mensaje del estado.
     */
    fun onCommentSaveMessageConsumed() {
        _state.value = _state.value.copy(commentSaveMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        bookingListener?.remove()
    }

}