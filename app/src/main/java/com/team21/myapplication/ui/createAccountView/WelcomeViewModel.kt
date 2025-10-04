package com.team21.myapplication.ui.createAccountView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.ui.createAccountView.state.SignInOperationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.team21.myapplication.ui.createAccountView.state.SignInUiState


class WelcomeViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun signIn() {
        val state = _uiState.value
        // email validation
        if (state.email.isBlank() || !state.email.contains("@")) {
            _uiState.value = state.copy(
                operationState = SignInOperationState.Error("Enter a valid email")
            )
            return
        }
        if (state.password.length< 6) { //password validation
            _uiState.value = state.copy(
                operationState = SignInOperationState.Error("Password must contain at least 6 characters")
            )
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
}

//sealed class SignInState {
//    object Idle : SignInState()
//    object Loading : SignInState()
//    data class Success(val userId: String) : SignInState()
//    data class Error(val message: String) : SignInState()
//}