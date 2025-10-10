package com.team21.myapplication.ui.profileView.state

data class ProfileUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val name: String = "",
    val email: String = "",
    val country: String = "",
    val phoneNumber: String = ""
)
