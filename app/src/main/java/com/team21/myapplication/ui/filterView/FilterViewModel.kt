package com.team21.myapplication.ui.filterView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.FilterMode
import com.team21.myapplication.data.repository.HousingTagRepository
import com.team21.myapplication.domain.mapper.FilterUiMapper
import com.team21.myapplication.domain.usecase.GetAllTagsUseCase
import com.team21.myapplication.domain.usecase.SearchPreviewsByTagsUseCase
import com.team21.myapplication.ui.filterView.state.FilterUiState
import com.team21.myapplication.ui.filterView.state.PreviewCardUi

// Firebase Analytics (KTX moderno)
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

// Firestore (SDK base para evitar dependencias KTX extra)
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * MVVM (ViewModel):
 * - Orquesta carga de tags y búsqueda con los filtros seleccionados.
 * - Telemetría: Analytics + Firestore (para pipeline de “trending”).
 */
class FilterViewModel(
    private val getAllTags: GetAllTagsUseCase =
        GetAllTagsUseCase(HousingTagRepository()),
    private val searchPreviews: SearchPreviewsByTagsUseCase =
        SearchPreviewsByTagsUseCase(HousingTagRepository())
) : ViewModel() {

    private val _state = MutableStateFlow(FilterUiState())
    val state: StateFlow<FilterUiState> = _state

    // Efectos one-shot (navegación, snackbars, etc.)
    private val _effects = MutableSharedFlow<FilterEffect>()
    val effects: SharedFlow<FilterEffect> = _effects

    // Mantiene el orden de selección
    private val selected = linkedSetOf<String>()

    // ----------- Public API -----------

    /** Carga inicial de tags. */
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

    /** Toggle de un tag con Analytics. */
    fun toggleTag(tagId: String) {
        val willSelect = !selected.contains(tagId)
        if (willSelect) selected.add(tagId) else selected.remove(tagId)

        // Analytics: toggle
        Firebase.analytics.logEvent("filter_toggle_tag") {
            param("tag_id", tagId)
            param("selected", if (willSelect) 1L else 0L)
        }

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
     * Ejecuta la búsqueda con los tags seleccionados.
     * - Analytics + Firestore logging para pipeline de notificaciones.
     */
    fun search(mode: FilterMode = FilterMode.AND) {
        if (selected.isEmpty()) return
        _state.value = _state.value.copy(isLoading = true, error = null)

        // Mapa id -> label (de tu propio UI state)
        val labelMap = (_state.value.featuredTags + _state.value.otherTags)
            .associate { it.id to it.label }

        // --- Analytics agregado (agregado + por tag con nombre)
        Firebase.analytics.logEvent("filter_search") {
            param("selected_count", selected.size.toLong())
            param("selected_ids_csv", selected.joinToString(","))
            param("mode", mode.name)
        }

        // Evento NUEVO por tag con NOMBRE
        selected.forEach { tagId ->
            val tagName = labelMap[tagId] ?: tagId
            Firebase.analytics.logEvent("filterNotificationTag") {
                param("tag_name", tagName)
            }
        }
        // --- Analytics agregado (agregado + por tag)
        val csv = selected.joinToString(",")
        Firebase.analytics.logEvent("filter_search") {
            param("selected_count", selected.size.toLong())
            param("selected_ids_csv", csv)
            param("mode", mode.name)
        }
        selected.forEach { tagId ->
            Firebase.analytics.logEvent("filter_search_tag") {
                param("tag_id", tagId)
            }
        }

        viewModelScope.launch {
            try {
                // Además del Analytics, escribimos eventos livianos en Firestore
                logSearchToFirestore(selected)

                val previews = searchPreviews(selected.toList(), mode)
                val ui = FilterUiMapper.toPreviewUi(previews)

                _state.value = _state.value.copy(
                    isLoading = false,
                    lastResults = ui
                )
                _effects.emit(FilterEffect.ShowResults(ui))
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error al buscar: ${e.message ?: "desconocido"}"
                )
            }
        }
    }

    // ----------- Internals -----------

    /**
     * Guarda en Firestore un documento por cada tag buscado:
     *   filterSearchEvents/{autoId} = { tagId, tagName, ts }
     * Esto permite que una Cloud Function programe el “top en 10 min” y envíe FCM.
     */
    private suspend fun logSearchToFirestore(selectedIds: Set<String>) {
        if (selectedIds.isEmpty()) return

        // Mapa id -> label (para enviar el nombre legible)
        val labelMap = (_state.value.featuredTags + _state.value.otherTags)
            .associate { it.id to it.label }

        val db = FirebaseFirestore.getInstance()
        val col = db.collection("filterSearchEvents")
        val batch = db.batch()

        selectedIds.forEach { id ->
            val doc = col.document()
            batch.set(
                doc,
                mapOf(
                    "tagId" to id,
                    "tagName" to (labelMap[id] ?: id),
                    "ts" to FieldValue.serverTimestamp()
                )
            )
        }

        val colNotif = db.collection("filterNotificationTag")
        selectedIds.forEach { id ->
            val tagName = labelMap[id] ?: id
            batch.set(colNotif.document(), mapOf(
                "tagName" to tagName,
                "ts" to FieldValue.serverTimestamp()
            ))
        }

        batch.commit().await()
    }
}

/** Efectos one-shot para Route/View (navegación, snackbars, etc.) */
sealed class FilterEffect {
    data class ShowResults(val items: List<PreviewCardUi>) : FilterEffect()
}
