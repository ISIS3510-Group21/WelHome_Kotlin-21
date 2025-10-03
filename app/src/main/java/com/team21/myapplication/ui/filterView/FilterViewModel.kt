package com.team21.myapplication.ui.filterView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.FilterMode
import com.team21.myapplication.data.repository.HousingTagRepository
import com.team21.myapplication.domain.usecase.GetAllTagsUseCase
import com.team21.myapplication.domain.usecase.SearchPreviewsByTagsUseCase
import com.team21.myapplication.ui.filterView.state.FilterUiState
import com.team21.myapplication.ui.filterView.state.PreviewCardUi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * MVVM (ViewModel):
 * - Orquesta carga de tags y búsqueda con los filtros seleccionados.
 * STATE:
 * - Expone un StateFlow<FilterUiState> que la View OBSERVA.
 * OBSERVER:
 * - La View/Route recoge este flujo y re-renderiza.
 */
class FilterViewModel(
    private val getAllTags: GetAllTagsUseCase =
        GetAllTagsUseCase(HousingTagRepository()),
    private val searchPreviews: SearchPreviewsByTagsUseCase =
        SearchPreviewsByTagsUseCase(HousingTagRepository())
) : ViewModel() {

    private val _state = MutableStateFlow(FilterUiState())
    val state: StateFlow<FilterUiState> = _state

    // Efectos one-shot (navegación a resultados, snackbars, etc.)
    private val _effects = MutableSharedFlow<FilterEffect>()
    val effects: SharedFlow<FilterEffect> = _effects

    // Conjunto de selección (mantiene orden de clics)
    private val selected = linkedSetOf<String>()

    /** Carga inicial de tags. Llamar desde la Route (LaunchedEffect). */
    fun load() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val tags = getAllTags()
                val (featured, others) = FilterUiMapper.toFeaturedAndOthers(tags, selected)
                _state.value = _state.value.copy(
                    isLoading = false,
                    featuredTags = featured,
                    otherTags = others,
                    selectedCount = selected.size,
                    canSearch = selected.isNotEmpty()
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error al cargar filtros: ${e.message ?: "desconocido"}"
                )
            }
        }
    }

    /** Selección/deselección de un tag. */
    fun toggleTag(tagId: String) {
        if (selected.contains(tagId)) selected.remove(tagId) else selected.add(tagId)

        val cur = _state.value
        val newFeatured = cur.featuredTags.map { it.copy(selected = it.id in selected) }
        val newOthers = cur.otherTags.map { it.copy(selected = it.id in selected) }

        _state.value = cur.copy(
            featuredTags = newFeatured,
            otherTags = newOthers,
            selectedCount = selected.size,
            canSearch = selected.isNotEmpty()
        )
    }

    /**
     * Ejecuta búsqueda con los tags seleccionados.
     * - Por defecto, modo AND (deben cumplir todos los filtros).
     * - Cambia a FilterMode.OR si prefieres unión.
     */
    fun search(mode: FilterMode = FilterMode.AND) {
        if (selected.isEmpty()) return
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val previews = searchPreviews(selected.toList(), mode)
                val ui = FilterUiMapper.toPreviewUi(previews)

                _state.value = _state.value.copy(
                    isLoading = false,
                    lastResults = ui
                )
                // Efecto de navegación (la Route decide a dónde ir)
                _effects.emit(FilterEffect.ShowResults(ui))
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error al buscar: ${e.message ?: "desconocido"}"
                )
            }
        }
    }
}

/** Efectos one-shot para la Route/View (navegación, snackbars, etc.) */
sealed class FilterEffect {
    data class ShowResults(val items: List<PreviewCardUi>) : FilterEffect()
}
