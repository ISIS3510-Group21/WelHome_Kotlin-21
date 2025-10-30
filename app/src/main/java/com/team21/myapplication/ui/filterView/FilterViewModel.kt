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
import com.team21.myapplication.domain.mapper.FilterUiMapper
import com.team21.myapplication.domain.usecase.GetAllTagsUseCase
import com.team21.myapplication.domain.usecase.SearchPreviewsByTagsUseCase
import com.team21.myapplication.ui.filterView.state.FilterUiState
import com.team21.myapplication.ui.filterView.state.PreviewCardUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * FilterViewModel (factory-friendly)
 *
 * IMPORTANT: Only `Application` is in the primary constructor so the default
 * AndroidViewModelFactory can instantiate this ViewModel without a custom factory.
 *
 * Patterns (tagged inline):
 * - [EVENTUAL CONNECTIVITY] [CACHE-FIRST] load() hydrates from local cache first, then refreshes from network.
 * - [LOCAL STORAGE] SharedPreferences via FilterOfflineRepository (last selection + cached tags snapshot).
 * - [MULTITHREADING] viewModelScope + Dispatchers.IO for any I/O (prefs, firestore).
 */
class FilterViewModel(app: Application) : AndroidViewModel(app) {

    // --- Build dependencies inside (no custom factory / no DI required) ---
    private val appCtx = app.applicationContext
    private val tagRepo = HousingTagRepository()
    private val getAllTags = GetAllTagsUseCase(tagRepo)
    private val searchPreviews = SearchPreviewsByTagsUseCase(tagRepo)
    private val offlineRepo = FilterOfflineRepository(appCtx) // [LOCAL STORAGE]

    // --- State / Effects ---
    private val _state = MutableStateFlow(FilterUiState())
    val state: StateFlow<FilterUiState> = _state

    private val _effects = MutableSharedFlow<FilterEffect>()
    val effects: SharedFlow<FilterEffect> = _effects

    // Maintains selection order
    private val selected = linkedSetOf<String>()

    /** Initial load: cache-first from preferences, then try refreshing from network. */
    fun load() {
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            // [EVENTUAL CONNECTIVITY] [CACHE-FIRST]
            // 1) Offline-first hydrate from local cache (prefs) — runs on IO internally.
            val cachedChips = offlineRepo.readCachedTagsAsUi() // [MULTITHREADING] (IO)
            val lastSel = offlineRepo.readLastSelection()       // [MULTITHREADING] (IO)

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

            // 2) Network refresh (if available). If it fails, keep cached UI.
            runCatching {
                val tags = getAllTags() // network call via use case
                val (featured, others) = FilterUiMapper.toFeaturedAndOthers(tags, selected)

                _state.value = _state.value.copy(
                    isLoading = false,
                    featuredTags = featured,
                    otherTags = others,
                    selectedCount = selected.size,
                    canSearch = selected.isNotEmpty()
                )

                // [LOCAL STORAGE] Persist fresh tags snapshot for next offline launch.
                viewModelScope.launch(Dispatchers.IO) { // [MULTITHREADING]
                    offlineRepo.saveCachedTagsFromUi(featured + others)
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

    /** Toggle tag selection and persist it asynchronously in preferences. */
    fun toggleTag(tagId: String) {
        val willSelect = !selected.contains(tagId)
        if (willSelect) selected.add(tagId) else selected.remove(tagId)

        // Update UI immediately
        val cur = _state.value
        val newFeatured = cur.featuredTags.map { it.copy(selected = it.id in selected) }
        val newOthers = cur.otherTags.map { it.copy(selected = it.id in selected) }
        _state.value = cur.copy(
            featuredTags = newFeatured,
            otherTags = newOthers,
            selectedCount = selected.size,
            canSearch = selected.isNotEmpty()
        )

        // [LOCAL STORAGE] Persist selection to prefs
        viewModelScope.launch(Dispatchers.IO) { // [MULTITHREADING]
            offlineRepo.saveLastSelection(selected.toList())
        }
    }

    /**
     * Execute search with the selected tags.
     * - Sends analytics (optional)
     * - Logs to Firestore (optional)
     */
    fun search(mode: FilterMode = FilterMode.AND) {
        if (selected.isEmpty()) return
        _state.value = _state.value.copy(isLoading = true, error = null)

        // Collect labels for analytics
        val labelMap = (_state.value.featuredTags + _state.value.otherTags)
            .associate { it.id to it.label }

        // Firebase Analytics (optional)
        Firebase.analytics.logEvent("filter_search") {
            param("selected_count", selected.size.toLong())
            param("selected_ids_csv", selected.joinToString(","))
            param("mode", mode.name)
        }
        selected.forEach { tagId ->
            val tagName = labelMap[tagId] ?: tagId
            Firebase.analytics.logEvent("filterNotificationTag") {
                param("tag_name", tagName)
            }
        }

        viewModelScope.launch {
            try {
                // Persist current selection for resilience
                viewModelScope.launch(Dispatchers.IO) { // [LOCAL STORAGE] [MULTITHREADING]
                    offlineRepo.saveLastSelection(selected.toList())
                }

                // Optional: log search to Firestore in background
                viewModelScope.launch(Dispatchers.IO) { // [MULTITHREADING]
                    logSearchToFirestore(selected)
                }

                val previews = searchPreviews(selected.toList(), mode)
                val ui = FilterUiMapper.toPreviewUi(previews)

                _state.value = _state.value.copy(isLoading = false, lastResults = ui)
                _effects.emit(FilterEffect.ShowResults(ui))
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Search error: ${e.message ?: "unknown"}"
                )
            }
        }
    }

    // ---------------- Internals ----------------

    /**
     * Write one document per tag searched:
     *   filterSearchEvents/{autoId} = { tagId, tagName, ts }
     * And a parallel collection "filterNotificationTag" with tagName & ts.
     * [MULTITHREADING] Called from IO.
     */
    private suspend fun logSearchToFirestore(selectedIds: Set<String>) {
        if (selectedIds.isEmpty()) return

        val labelMap = (_state.value.featuredTags + _state.value.otherTags)
            .associate { it.id to it.label }

        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        val col = db.collection("filterSearchEvents")
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
            batch.set(
                colNotif.document(),
                mapOf(
                    "tagName" to tagName,
                    "ts" to FieldValue.serverTimestamp()
                )
            )
        }

        batch.commit().await()
    }
}

/** One-shot effects for navigation/snackbars. */
sealed class FilterEffect {
    data class ShowResults(val items: List<PreviewCardUi>) : FilterEffect()
}
