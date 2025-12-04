package com.team21.myapplication.ui.ownerPostsDetail.state

/**
 * STATE de la pantalla de detalle del owner:
 * - Solo contiene lo que la View necesita.
 * - ViewModel emite nuevas copias inmutables.
 */
data class OwnerPostDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val title: String = "",
    val address: String = "",
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val pricePerMonthLabel: String = "",

    val images: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    val roommatesPhotoUrls: List<String> = emptyList()
)
