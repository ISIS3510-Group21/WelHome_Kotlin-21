package com.team21.myapplication.ui.saved

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.data.repository.SavedPostsRepository
import com.team21.myapplication.data.repository.StudentUserRepository
import com.team21.myapplication.data.repository.offline.SavedPostsOfflineRepository
import com.team21.myapplication.ui.filterView.state.PreviewCardUi
import com.team21.myapplication.ui.saved.state.SavedPostsUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log


/**
 * Estrategias:
 * - [EVENTUAL CONNECTIVITY] [CACHE-FIRST] load(): ArrayMap -> Hive -> Online
 * - [LOCAL STORAGE] Persistencia en disco (Hive) tras fetch online
 * - [CACHING] ArrayMap en memoria
 * - [MULTITHREADING] Corutinas anidadas IO/Main
 */
class SavedPostsViewModel(app: Application) : AndroidViewModel(app) {
    private val TAG = "SavedViewmodel"

    private val appCtx = app.applicationContext
    private val offline = SavedPostsOfflineRepository(appCtx)
    private val online = SavedPostsRepository()
    private val auth = AuthRepository() // <-- usar AuthRepository para el ID del estudiante
    private val studentRepo = StudentUserRepository()

    private val _state = MutableStateFlow(SavedPostsUiState())
    val state: StateFlow<SavedPostsUiState> = _state

    private suspend fun currentUserId(): String? = studentRepo.findStudentIdByEmail(auth.getCurrentUserEmail())

    fun load(isOnline: Boolean) {
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            // 1) [CACHE-FIRST] ArrayMap / Hive
            val cached = offline.load()
            if (!cached.isNullOrEmpty()) {
                _state.value = _state.value.copy(isLoading = false, items = cached)
            }

            // 2) Online refresh si es posible
            if (isOnline) {
                runCatching {
                    val userId = currentUserId() ?: error("No user session")
                    Log.d(TAG, "getSavedPreviewsVIEWMODEL(userId=$userId)") // [DEBUG]
                    val fresh = withContext(Dispatchers.IO) {
                        online.getSavedPreviews(userId)
                    }
                    _state.value = _state.value.copy(isLoading = false, items = fresh)

                    // Persistir en disco
                    viewModelScope.launch(Dispatchers.IO) {
                        offline.saveAll(fresh)
                    }
                }.onFailure { e ->
                    if (_state.value.items.isEmpty()) {
                        _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Error")
                    }
                }
            } else {
                if (_state.value.items.isEmpty()) {
                    _state.value = _state.value.copy(isLoading = false, items = emptyList())
                }
            }
        }
    }

    fun addSaved(housingId: String, isOnline: Boolean) {
        viewModelScope.launch {
            val current = _state.value.items.toMutableList()
            // Optimistic insert
            if (current.none { it.housingId == housingId }) {
                current.add(0, PreviewCardUi(housingId, "Saving…", "", 0.0, 0, ""))
                _state.value = _state.value.copy(items = current)
            }

            if (isOnline) {
                runCatching {
                    val userId = currentUserId() ?: error("No user")
                    withContext(Dispatchers.IO) { online.addSaved(userId, housingId) } // I/O
                    load(isOnline = true) // re-sync
                }.onFailure {
                    _state.value = _state.value.copy(error = "Could not save post")
                }
            }
        }
    }

    fun removeSaved(housingId: String, isOnline: Boolean) {
        viewModelScope.launch {
            // Optimistic remove
            _state.value = _state.value.copy(items = _state.value.items.filterNot { it.housingId == housingId })
            if (isOnline) {
                runCatching {
                    val userId = currentUserId() ?: error("No user")
                    withContext(Dispatchers.IO) { online.removeSaved(userId, housingId) } // I/O
                    // Persist nuevo estado
                    viewModelScope.launch(Dispatchers.IO) {
                        offline.saveAll(_state.value.items)
                    }
                }.onFailure {
                    _state.value = _state.value.copy(error = "Could not remove post")
                }
            }
        }
    }
}
