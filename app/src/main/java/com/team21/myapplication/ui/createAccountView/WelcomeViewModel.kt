package com.team21.myapplication.ui.createAccountView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.ui.createAccountView.state.SignInOperationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Patterns
import com.team21.myapplication.ui.createAccountView.state.SignInUiState
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import androidx.lifecycle.ViewModelProvider

class WelcomeViewModel(
    private val networkMonitor: com.team21.myapplication.utils.NetworkMonitor
) : ViewModel() {
    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    val isOnline: StateFlow<Boolean> =
        networkMonitor.isOnline.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = networkMonitor.isOnline.value
        )

    fun updateEmail(value: String) {
        val touched = _uiState.value.emailTouched
        _uiState.value = _uiState.value.copy(
            email = value,
            emailError = if (touched && value.isNotBlank() && !isValidEmail(value)) "Not valid email" else null
        )
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun signIn() {
        val state = _uiState.value
        // email validation
        if (!isValidEmail(state.email)) {
            _uiState.value = state.copy(emailError = "Email is not valid")
            return
        }

        _uiState.value = state.copy(operationState = SignInOperationState.Loading)

        viewModelScope.launch {
            val result = repository.signIn(state.email.trim(), state.password)

            _uiState.value = _uiState.value.copy(
                operationState = if (result.isSuccess) {
                    val uid = result.getOrNull()!!
                    val owner = repository.isOwner(uid)
                    SignInOperationState.Success(uid, owner)
                } else {
                    SignInOperationState.Error(result.exceptionOrNull()?.message ?: "Error logging in")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(operationState = SignInOperationState.Idle)
    }

    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    fun onEmailFocusChanged(focused: Boolean) {
        val current = _uiState.value
        if (!focused) { // lost focus
            _uiState.value = current.copy(
                emailTouched = true,
                emailError = if (current.email.isNotBlank() && !isValidEmail(current.email)) "Not valid email" else null
            )
        }
    }

    class WelcomeViewModelFactory(
        private val networkMonitor: com.team21.myapplication.utils.NetworkMonitor
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(WelcomeViewModel::class.java))
            return WelcomeViewModel(networkMonitor) as T
        }
    }
}
