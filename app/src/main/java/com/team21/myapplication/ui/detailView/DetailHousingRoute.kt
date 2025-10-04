package com.team21.myapplication.ui.detailView

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * MVVM (Route):
 * - Recibe argumentos de navegación (housingId).
 * - Crea/obtiene el ViewModel.
 * - OBSERVER: observa el StateFlow del VM.
 * - Entrega el STATE a la View.
 */
@Composable
fun DetailHousingRoute(
    housingId: String,
    onBack: () -> Unit = {}
) {
    // VM: por ahora sin Factory porque le pusimos dependencias por defecto
    val vm: DetailHousingViewModel = viewModel()

    // OBSERVER: observar el STATE de la VM
    val uiState by vm.state.collectAsState()

    // Cargar datos cuando cambia el id
    LaunchedEffect(housingId) {
        vm.load(housingId)
    }

    // Renderizado simple de estados (puedes personalizarlo a tu gusto)
    when {
        uiState.isLoading -> {
            CircularProgressIndicator()
        }
        uiState.error != null -> {
            Text(text = uiState.error ?: "Error")
        }
        else -> {
            DetailHousingView(
                uiState = uiState,
                onBack = onBack,
                onBookVisit = { /* TODO: hook */ },
                onViewAllAmenities = { /* TODO */ },
                onToggleFavorite = { /* TODO */ },
                onCallHost = { /* TODO */ },
                onMessageHost = { /* TODO */ }
            )
        }
    }
}