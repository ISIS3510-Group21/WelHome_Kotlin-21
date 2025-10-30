package com.team21.myapplication.ui.bookVisit.state

data class BookVisitUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    val housingId: String = "",
    val housingTitle: String = "",
    val housingThumbnail: String = "",

    val selectedDateMillis: Long? = null,
    val selectedHour: String? = null,

    // Por ahora estático; luego desde Firestore
    val availableHours: List<String> = emptyList(),
    // disponibilidad real por día
    val availabilityByDay: Map<java.time.LocalDate, List<java.time.LocalTime>> = emptyMap(),

    // flujo de confirmación
    val isConfirming: Boolean = false,
    val successMessage: String? = null,

    )