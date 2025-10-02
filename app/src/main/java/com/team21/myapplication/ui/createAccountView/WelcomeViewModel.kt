package com.team21.myapplication.ui.createAccountView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team21.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class WelcomeViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _signInState = MutableStateFlow<SignInState>(SignInState.Idle)
    val signInState: StateFlow<SignInState> = _signInState.asStateFlow()

    fun updateEmail(value: String) { _email.value = value }
    fun updatePassword(value: String) { _password.value = value }

    fun signIn() {
        if (_email.value.isBlank() || !_email.value.contains("@")) {
            _signInState.value = SignInState.Error("Enter a valid email")
            return
        }

        if (_password.value.length < 6) {
            _signInState.value = SignInState.Error("Password must contain at least 6 characters")
            return
        }

        _signInState.value = SignInState.Loading

        viewModelScope.launch {
            val result = repository.signIn(_email.value.trim(), _password.value)

            _signInState.value = if (result.isSuccess) {
                SignInState.Success(result.getOrNull()!!)
            } else {
                SignInState.Error(result.exceptionOrNull()?.message ?: "Error logging in")
            }
        }
    }

    fun resetState() {
        _signInState.value = SignInState.Idle
    }
}

sealed class SignInState {
    object Idle : SignInState()
    object Loading : SignInState()
    data class Success(val userId: String) : SignInState()
    data class Error(val message: String) : SignInState()
}