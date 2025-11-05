package com.team21.myapplication.ui.detailView.state

/**
 * STATE:
 * - Representa exactamente lo que la View necesita (no expone entidades de Firestore).
 * - Inmutable desde la View: la VM emite nuevas instancias.
 */
data class DetailHousingUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val title: String = "",
    val rating: Double = 0.0,
    val pricePerMonthLabel: String = "",
    val address: String = "",
    val ownerName: String = "",

    val imagesFromServer: List<String> = emptyList(),
    val amenityLabels: List<String> = emptyList(),
    val roommateNames: List<String> = emptyList(),

    val roommateCount: Int = 10,
    val reviewsCount: Int = 2,

    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: String = "",
    val isSaved: Boolean = false,           // <- si este post está guardado por el usuario
    val isFavoriteInFlight: Boolean = false // <- bloquea taps mientras se procesa
)