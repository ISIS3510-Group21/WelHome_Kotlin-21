package com.team21.myapplication.ui.profileView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.local.BasicProfileLocal
import com.team21.myapplication.data.repository.AuthRepository.BasicProfile
import com.team21.myapplication.data.local.SecureSessionManager
import com.team21.myapplication.ui.profileView.state.ProfileUiState
import com.team21.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
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

        // Corrutina 1: En IO para obtener datos
        viewModelScope.launch(Dispatchers.IO) {
            val profileData = try {
                // 1) Determina UID
                val firebaseUid = auth.getCurrentUserId()
                val localUid = session.getSession()?.userId
                val uid = firebaseUid ?: localUid

                if (uid == null) {
                    ProfileData.Error("No active session")
                } else {
                    // 2) Intentar con Firebase
                    if (firebaseUid != null) {
                        val res: Result<BasicProfile> = auth.fetchBasicProfile(firebaseUid)

                        var result: ProfileData? = null
                        res.onSuccess { p ->
                            // Guarda snapshot local
                            session.saveBasicProfile(
                                BasicProfileLocal(
                                    name = p.name,
                                    nationality = p.nationality,
                                    phoneNumber = p.phoneNumber
                                )
                            )
                            result = ProfileData.Success(
                                name = p.name,
                                email = p.email,
                                country = p.nationality,
                                phoneNumber = p.phoneNumber
                            )
                        }

                        if (result != null) {
                            result!!
                        } else {
                            // Si falló Firebase, ir a caché
                            loadFromCache()
                        }
                    } else {
                        // 3) Fallback offline -> recuperar de cache
                        loadFromCache()
                    }
                }
            } catch (e: Exception) {
                ProfileData.Error("Error loading profile: ${e.message}")
            }

            // Lanzar Corrutina 2: En Main para actualizar UI
            viewModelScope.launch(Dispatchers.Main) {
                when (profileData) {
                    is ProfileData.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            name = profileData.name,
                            email = profileData.email,
                            country = profileData.country,
                            phoneNumber = profileData.phoneNumber,
                            error = null
                        )
                    }
                    is ProfileData.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = profileData.message
                        )
                    }
                }
            }
        }
    }

    // Función auxiliar para cargar desde cache
    private fun loadFromCache(): ProfileData {
        val cached = session.getBasicProfileOrNull()
        return if (cached != null) {
            ProfileData.Success(
                name = cached.name.orEmpty(),
                email = session.getSession()?.email.orEmpty(),
                country = cached.nationality.orEmpty(),
                phoneNumber = cached.phoneNumber.orEmpty()
            )
        } else {
            ProfileData.Error("No profile data available offline")
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

private sealed class ProfileData {
    data class Success(
        val name: String,
        val email: String,
        val country: String,
        val phoneNumber: String
    ) : ProfileData()

    data class Error(val message: String) : ProfileData()
}
