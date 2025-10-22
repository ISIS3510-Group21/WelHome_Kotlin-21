package com.team21.myapplication.ui.detailView

import android.util.Log
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.team21.myapplication.analytics.AnalyticsHelper
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.data.repository.HousingPostRepository
import com.team21.myapplication.data.repository.StudentUserRepository

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

    // 2) Instancias - sin di
    val context = LocalContext.current
    val analytics = remember { AnalyticsHelper(context.applicationContext) }
    val authRepo = remember { AuthRepository() }
    val studentRepo = remember { StudentUserRepository() }
    val housingRepo = remember { HousingPostRepository() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val messaging = remember { FirebaseMessaging.getInstance() }

    val startMs = remember(housingId) { System.currentTimeMillis() }

    DisposableEffect(housingId) {
        onDispose {
            val duration = System.currentTimeMillis() - startMs
            Log.d("DetailHousing", "onDispose -> durationMs=$duration, housingId=$housingId")
            // 4) Llamada al VM pasando las dependencias como parámetros
            vm.onDetailVisibleFor(
                durationMs = duration,
                analytics = analytics,
                authRepo = authRepo,
                studentRepo = studentRepo,
                housingRepo = housingRepo,
                firestore = firestore,
                messaging = messaging
            )
        }
    }

    // Renderizado simple de estados
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