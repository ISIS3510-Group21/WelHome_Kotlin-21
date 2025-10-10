package com.team21.myapplication.ui.profileView

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team21.myapplication.ui.profileView.state.ProfileUiState

@Composable
fun ProfileRoute(
    onLogout: () -> Unit
) {

    val vm: ProfileViewModel = viewModel()
    val state: ProfileUiState = vm.state.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) { vm.load() }
    ProfileView(
        onLogout = onLogout,
        name = state.name,
        email = state.email,
        country = state.country,
        phoneNumber = state.phoneNumber
    )
}
