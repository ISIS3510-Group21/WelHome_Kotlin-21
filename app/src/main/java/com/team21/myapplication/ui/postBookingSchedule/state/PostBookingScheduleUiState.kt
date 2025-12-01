package com.team21.myapplication.ui.postBookingSchedule.state
import com.team21.myapplication.ui.components.cards.PropertyOptionUi

data class PostBookingScheduleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    // Propiedades del owner
    val properties: List<PropertyOptionUi> = emptyList(),
    val selectedPropertyId: String? = null,
    val selectedPropertyTitle: String = "",
    val selectedPropertySubtitle: String = "",
    val selectedPropertyThumbnail: String = "",

    // Fecha seleccionada
    val selectedDateMillis: Long? = null,

    // Horas disponibles para publicar en ese día (ej: ["7:00", "9:00", ...])
    val availableHours: List<String> = emptyList(),

    // Horas que el owner ha seleccionado para publicar
    val selectedHours: Set<String> = emptySet(),

    // Guardado + feedback
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null,
    val snackbarError: Boolean = false
)
