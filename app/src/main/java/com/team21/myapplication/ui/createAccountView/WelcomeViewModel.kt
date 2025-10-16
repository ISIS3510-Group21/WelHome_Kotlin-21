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


class WelcomeViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun updateEmail(value: String) {
        val touched = _uiState.value.emailTouched
        _uiState.value = _uiState.value.copy(
            email = value,
            emailError = if (touched && !isValidEmail(value) && value.isNotBlank()) "Not valid email" else null
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
                    SignInOperationState.Success(result.getOrNull()!!)
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
                emailError = if (!isValidEmail(current.email) && current.email.isNotBlank()) "Not valid email" else null
            )
        }
    }
}
