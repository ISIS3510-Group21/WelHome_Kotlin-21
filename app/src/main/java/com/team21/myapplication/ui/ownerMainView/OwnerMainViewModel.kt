package com.team21.myapplication.ui.ownerMainView

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.ui.ownerMainView.state.OwnerHomeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OwnerMainViewModel(
    private val appContext: Context
) : ViewModel() {

    private val _state = MutableStateFlow(OwnerHomeState())
    val state: StateFlow<OwnerHomeState> = _state

    private val authRepo = AuthRepository()
    // TODO: private val ownerRepo = OwnerRepository()

    fun loadOwnerHome() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val uid = authRepo.getCurrentUserId()
            if (uid == null) {
                _state.value = _state.value.copy(isLoading = false)
                return@launch
            }
            _state.value = _state.value.copy(currentUserId = uid)

            // TODO: trae data real del owner cuando exista
            _state.value = _state.value.copy(
                ownedHousings = emptyList(),
                isLoading = false
            )
        }
    }
}
