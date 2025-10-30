package com.team21.myapplication.ui.filterView

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.repository.FilterMode
import com.team21.myapplication.data.repository.HousingTagRepository
import com.team21.myapplication.data.repository.offline.FilterOfflineRepository
import com.team21.myapplication.data.repository.offline.ResultsOfflineRepository
import com.team21.myapplication.domain.mapper.FilterUiMapper
import com.team21.myapplication.domain.usecase.GetAllTagsUseCase
import com.team21.myapplication.domain.usecase.SearchPreviewsByTagsUseCase
import com.team21.myapplication.local.ResultsPrefs
import com.team21.myapplication.ui.filterView.state.FilterUiState
import com.team21.myapplication.ui.filterView.state.PreviewCardUi
import com.team21.myapplication.utils.TokenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FilterViewModel(app: Application) : AndroidViewModel(app) {

    private val appCtx = app.applicationContext

    private val tagRepo = HousingTagRepository()
    private val getAllTags = GetAllTagsUseCase(tagRepo)
    private val searchPreviews = SearchPreviewsByTagsUseCase(tagRepo)

    private val filtersOfflineRepo = FilterOfflineRepository(appCtx)
    private val resultsPrefs = ResultsPrefs.get(appCtx)
    private val resultsOfflineRepo = ResultsOfflineRepository(appCtx)

    private val _state = MutableStateFlow(FilterUiState())
    val state: StateFlow<FilterUiState> = _state

    private val _effects = MutableSharedFlow<FilterEffect>()
    val effects: SharedFlow<FilterEffect> = _effects

    private val selected = linkedSetOf<String>()

    fun load() {
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val cachedChips = filtersOfflineRepo.readCachedTagsAsUi()
            val lastSel = filtersOfflineRepo.readLastSelection()

            if (cachedChips.isNotEmpty()) {
                selected.clear()
                selected.addAll(lastSel)

                val featured = cachedChips.take(4).map { it.copy(selected = it.id in selected) }
                val others = cachedChips.drop(4).map { it.copy(selected = it.id in selected) }

                _state.value = _state.value.copy(
                    isLoading = false,
                    featuredTags = featured,
                    otherTags = others,
                    selectedCount = selected.size,
                    canSearch = selected.isNotEmpty()
                )
            }

            runCatching {
                val tags = getAllTags()
                val (featured, others) = com.team21.myapplication.domain.mapper.FilterUiMapper
                    .toFeaturedAndOthers(tags, selected)

                _state.value = _state.value.copy(
                    isLoading = false,
                    featuredTags = featured,
                    otherTags = others,
                    selectedCount = selected.size,
                    canSearch = selected.isNotEmpty()
                )

                viewModelScope.launch(Dispatchers.IO) {
                    filtersOfflineRepo.saveCachedTagsFromUi(featured + others)
                }
            }.onFailure { e ->
                if (_state.value.featuredTags.isEmpty() && _state.value.otherTags.isEmpty()) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Failed to load filters: ${e.message ?: "unknown"}"
                    )
                }
            }
        }
    }

    fun toggleTag(tagId: String) {
        val willSelect = !selected.contains(tagId)
        if (willSelect) selected.add(tagId) else selected.remove(tagId)

        val cur = _state.value
        val newFeatured = cur.featuredTags.map { it.copy(selected = it.id in selected) }
        val newOthers = cur.otherTags.map { it.copy(selected = it.id in selected) }
        _state.value = cur.copy(
            featuredTags = newFeatured,
            otherTags = newOthers,
            selectedCount = selected.size,
            canSearch = selected.isNotEmpty()
        )

        viewModelScope.launch(Dispatchers.IO) {
            filtersOfflineRepo.saveLastSelection(selected.toList())
        }
    }

    fun search(mode: FilterMode = FilterMode.AND) {
        if (selected.isEmpty()) return

        // Marcar estado de búsqueda para deshabilitar botón y mostrar spinner
        _state.value = _state.value.copy(
            isLoading = true,         // si quieres mantener el loader global
            isSearching = true,       // NUEVO: loader específico del botón
            error = null
        )

        // Analytics opcional
        Firebase.analytics.logEvent("filter_search") {
            param("selected_count", selected.size.toLong())
            param("selected_ids_csv", selected.joinToString(","))
            param("mode", mode.name)
        }

        val labelMap = (_state.value.featuredTags + _state.value.otherTags)
            .associate { it.id to it.label }

        selected.forEach { tagId ->
            val tagName = labelMap[tagId] ?: tagId
            Firebase.analytics.logEvent("filterNotificationTag") {
                param("tag_name", tagName)
            }
        }

        viewModelScope.launch {
            try {
                viewModelScope.launch(Dispatchers.IO) {
                    filtersOfflineRepo.saveLastSelection(selected.toList())
                }

                val previews = searchPreviews(selected.toList(), mode)
                val ui: List<PreviewCardUi> = FilterUiMapper.toPreviewUi(previews)

                _state.value = _state.value.copy(
                    isLoading = false,
                    isSearching = false,    // <-- liberar el botón
                    lastResults = ui
                )
                _effects.emit(FilterEffect.ShowResults(ui))

                // Persistencia Results (token + combo)
                viewModelScope.launch(Dispatchers.IO) {
                    val token = TokenUtil.tokenFor(selected.toList(), mode.name)
                    resultsPrefs.saveLastResultsToken(token)
                    resultsOfflineRepo.saveCombo(token, ui)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isSearching = false,     // <-- liberar el botón también en error
                    error = "Search error: ${e.message ?: "unknown"}"
                )
            }
        }
    }
}

sealed class FilterEffect {
    data class ShowResults(val items: List<PreviewCardUi>) : FilterEffect()
}
