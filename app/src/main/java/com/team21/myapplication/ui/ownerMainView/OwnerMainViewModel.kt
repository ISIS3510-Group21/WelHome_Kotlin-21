package com.team21.myapplication.ui.ownerMainView

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.ui.ownerMainView.state.OwnerHomeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.team21.myapplication.data.cache.RecentlySeenCache
import com.team21.myapplication.utils.NetworkMonitor
import com.team21.myapplication.data.model.HousingPreview
import com.team21.myapplication.data.repository.HousingPostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import com.team21.myapplication.data.local.AppDatabase
import com.team21.myapplication.data.local.entity.toOwnerOfflineEntity
import com.team21.myapplication.data.local.entity.toHousingPreview


class OwnerMainViewModel(
    private val appContext: Context
) : ViewModel() {

    private val _state = MutableStateFlow(OwnerHomeState())
    val state: StateFlow<OwnerHomeState> = _state
    private val network = NetworkMonitor.get(appContext)
    private val housingRepo = HousingPostRepository()
    init {
        RecentlySeenCache.init(appContext)
        viewModelScope.launch {
            // observar cambios de conectividad
            network.isOnline.collectLatest { online ->
                _state.value = _state.value.copy(isOnline = online)
            }

        }
    }

    private val authRepo = AuthRepository()
    private val db = AppDatabase.getDatabase(appContext)
    private val ownerOfflineDao = db.ownerOfflineDao()

    fun loadOwnerHome() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val uid = authRepo.getCurrentUserId()
            if (uid == null) {
                _state.value = _state.value.copy(isLoading = false)
                return@launch
            }
            _state.value = _state.value.copy(currentUserId = uid)

            // Conectividad para banner
            val onlineNow = network.isOnline.value

            // 1) Intenta “recently seen” del LRU
            val recent = withContext(Dispatchers.IO) { RecentlySeenCache.getMRU(limit = 20)}

            val defaults = withContext(Dispatchers.IO) {
                if (onlineNow) {
                    // ONLINE: trae TODOS los previews
                    val all = housingRepo.getAllPreviews()

                    // Guarda snapshot TOP-15 en Room (reemplaza)
                    ownerOfflineDao.clearAll()
                    ownerOfflineDao.insertAll(all.take(15).map { it.toOwnerOfflineEntity() })

                    // Lo que mostrará la UI cuando online (todos)
                    all
                } else {
                    // OFFLINE: si no hay “recent”, usa snapshot TOP-15 de Room
                    ownerOfflineDao.getTopN(15).map { it.toHousingPreview() }
                }
            }


            _state.value = _state.value.copy(
                recentlySeen = recent,
                defaultTop = defaults,
                isOnline = onlineNow,
                isLoading = false
            )

        }
    }

    fun onPostClicked(item: HousingPreview) {
        viewModelScope.launch(Dispatchers.IO) {
            // No modificar LRU on modo offline
            if (!network.isOnline.value) return@launch
            RecentlySeenCache.put(item)
            val updated = RecentlySeenCache.getMRU(20)
            _state.value = _state.value.copy(
                recentlySeen = updated
            )
        }
    }

}
