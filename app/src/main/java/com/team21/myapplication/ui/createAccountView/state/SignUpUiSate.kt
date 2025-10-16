package com.team21.myapplication.ui.createAccountView.state

data class SignUpUiState(
    // Form fields
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val phonePrefix: String = "+57",
    val birthDay: String = "",
    val birthMonth: String = "",
    val birthYear: String = "",
    val gender: String = "",
    val nationality: String = "",
    val language: String = "",
    val isStudent: Boolean = false,
    val isHost: Boolean = false,
    val emailError: String? = null,
    val emailTouched: Boolean = false,

    // Operation state
    val operationState: OperationState = OperationState.Idle
)

sealed class OperationState {
    object Idle : OperationState()
    object Loading : OperationState()
    data class Success(val userId: String) : OperationState()
    data class Error(val message: String) : OperationState()
}