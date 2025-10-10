package com.team21.myapplication.ui.profileView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.ui.profileView.state.ProfileUiState
import com.team21.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val auth: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    fun load() {
        // evita recargas innecesarias
        if (!_state.value.isLoading && _state.value.error == null) return

        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val uid = auth.getCurrentUserId()
            if (uid == null) {
                _state.value = _state.value.copy(isLoading = false, error = "No active session")
                return@launch
            }
            val res = auth.fetchBasicProfile(uid)
            if (res.isSuccess) {
                val p = res.getOrNull()!!
                _state.value = ProfileUiState(
                    isLoading = false,
                    name = p.name,
                    email = p.email,
                    country = p.nationality,
                    phoneNumber = p.phoneNumber
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = res.exceptionOrNull()?.message ?: "Failed to load profile"
                )
            }
        }
    }
}
