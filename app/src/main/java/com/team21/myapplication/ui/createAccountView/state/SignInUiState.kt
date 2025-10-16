package com.team21.myapplication.ui.createAccountView.state

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val emailTouched: Boolean = false,
    val operationState: SignInOperationState = SignInOperationState.Idle
)

sealed class SignInOperationState {
    object Idle : SignInOperationState()
    object Loading : SignInOperationState()
    data class Success(val userId: String) : SignInOperationState()
    data class Error(val message: String) : SignInOperationState()
}