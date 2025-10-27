package com.team21.myapplication.ui.profileView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.local.BasicProfileLocal
import com.team21.myapplication.data.repository.AuthRepository.BasicProfile
import com.team21.myapplication.data.local.SecureSessionManager
import com.team21.myapplication.ui.profileView.state.ProfileUiState
import com.team21.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val auth: AuthRepository,
    private val session: SecureSessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    fun load() {

        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            // 1) Determina UID: Firebase si existe; si no, de sesión local
            val firebaseUid = auth.getCurrentUserId()
            val localUid = session.getSession()?.userId // de SecureSessionManager
            val uid = firebaseUid ?: localUid

            if (uid == null) {
                // Sin sesión de Firebase ni local -> no se puede mostrar info
                _state.value = _state.value.copy(isLoading = false, error = "No active session")
                return@launch
            }

            // 2) intentar con firebase
            if (firebaseUid != null) {
                val res: Result<BasicProfile> = auth.fetchBasicProfile(firebaseUid)

                res.onSuccess { p ->
                    // Guarda snapshot local para offline
                    session.saveBasicProfile(
                        BasicProfileLocal(
                            name = p.name,
                            nationality = p.nationality,
                            phoneNumber = p.phoneNumber
                        )
                    )
                    _state.value = _state.value.copy(
                        isLoading = false,
                        name = p.name,
                        email = p.email,
                        country = p.nationality,
                        phoneNumber = p.phoneNumber,
                        error = null
                    )
                    return@launch
                }.onFailure {
                }
            }

            // 3) Fallback offline: usa el snapshot local si existe
            val cached = session.getBasicProfileOrNull()
            if (cached != null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    name = cached.name.orEmpty(),
                    email = session.getSession()?.email.orEmpty(),
                    country = cached.nationality.orEmpty(),
                    phoneNumber = cached.phoneNumber.orEmpty(),
                    error = null
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "No profile data available offline"
                )
            }
        }
    }
}

class ProfileViewModelFactory(
    private val auth: AuthRepository,
    private val session: SecureSessionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ProfileViewModel::class.java))
        return ProfileViewModel(auth, session) as T
    }
}
