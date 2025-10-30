package com.team21.myapplication.ui.filterView.state

/**
 * STATE (patrón State):
 * - Única fuente de verdad para que la View pinte.
 * - Inmutable para la View; el ViewModel emite nuevas instancias.
 */

data class TagChipUi(
    val id: String,
    val label: String,
    val selected: Boolean = false
)

data class PreviewCardUi(
    val housingId: String,
    val title: String,
    val pricePerMonthLabel: String,
    val rating: Double,
    val reviewsCount: Int,
    val photoUrl: String
)

data class FilterUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSearching: Boolean = false,

    // Tags principales (botones grandes): House, Apartment, Cabins, Room
    val featuredTags: List<TagChipUi> = emptyList(),

    // Resto de tags (chips del carrusel)
    val otherTags: List<TagChipUi> = emptyList(),

    // Control de selección
    val selectedCount: Int = 0,
    val canSearch: Boolean = false,

    // Resultados de la última búsqueda (se usarán en la pantalla de resultados)
    val lastResults: List<PreviewCardUi> = emptyList()
)