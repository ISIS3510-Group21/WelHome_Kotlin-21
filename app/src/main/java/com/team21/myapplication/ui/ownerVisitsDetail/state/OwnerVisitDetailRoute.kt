package com.team21.myapplication.ui.ownerVisitsDetail.state

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team21.myapplication.ui.ownerVisitsDetail.OwnerVisitDetailView
import com.team21.myapplication.ui.ownerVisitsDetail.OwnerVisitDetailViewModel
import kotlinx.coroutines.launch
import com.team21.myapplication.utils.NetworkMonitor
import androidx.compose.runtime.collectAsState
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import com.team21.myapplication.ui.components.banners.BannerPosition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun OwnerVisitDetailRoute(
    bookingId: String,
    isAvailable: Boolean,
    propertyImageUrl: String,
    propertyName: String,
    visitDate: String,
    visitTime: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val factory = OwnerVisitDetailViewModelFactory(application)
    val viewModel: OwnerVisitDetailViewModel = viewModel(factory = factory)

    val networkMonitor = remember { NetworkMonitor.get(context) }
    val isOnline by networkMonitor.isOnline.collectAsState()

    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Escuchar mensajes de guardado de comentario y mostrarlos como snackbar
    LaunchedEffect(state.commentSaveMessage) {
        val message = state.commentSaveMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.onCommentSaveMessageConsumed()
        }
    }


    // Cargar datos cuando se monta el composable
    LaunchedEffect(bookingId) {
        viewModel.loadVisitDetail(
            bookingId = bookingId,
            isAvailable = isAvailable,
            propertyImageUrl = propertyImageUrl,
            propertyName = propertyName,
            visitDate = visitDate,
            visitTime = visitTime
        )
    }

    val view = LocalView.current

    val statusBarColor = if (!isOnline) {
        androidx.compose.ui.graphics.Color.Black
    } else {
        MaterialTheme.colorScheme.background
    }

    SideEffect {
        val window = (view.context as android.app.Activity).window
        // Variable de fondo
        window.statusBarColor = statusBarColor.toArgb()

        // La lógica para decidir el color de los íconos
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            if (!isOnline) {
                false // Íconos blancos para fondo negro
            } else {
                statusBarColor.luminance() > 0.5f // Decide según la luminancia del fondo
            }
    }

    Column(modifier = modifier.fillMaxSize().statusBarsPadding()) {

        // Banner fijo arriba, pero debajo de los íconos nativos
        ConnectivityBanner(
            visible = !isOnline,
            position = BannerPosition.Top,
        )

        // Contenido de la pantalla (ocupa el resto)
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Error: ${state.error}")
                }
            }

            else -> {
                OwnerVisitDetailView(
                    propertyImageUrl = state.propertyImageUrl,
                    visitStatus = state.visitStatus,
                    visitDate = state.visitDate,
                    visitTime = state.visitTime,
                    visitorName = state.visitorName,
                    visitorNationality = state.visitorNationality,
                    visitorPhotoUrl = state.visitorPhotoUrl,
                    visitorFeedback = state.visitorFeedback,
                    visitorRating = state.visitorRating,
                    ownerComment = state.ownerComment,
                    ownerCommentDraft = state.ownerCommentDraft,
                    isEditingOwnerComment = state.isEditingOwnerComment,
                    onCommentChange = { viewModel.updateOwnerComment(it) },
                    onSaveComment = {
                        scope.launch { viewModel.saveOwnerComment() }
                    },
                    onStartEditOwnerComment = { viewModel.setEditingOwnerComment(true) },
                    onCancelEditOwnerComment = { viewModel.setEditingOwnerComment(false) },
                    onMessageClick = { /* TODO */ },
                    onBackClick = onBack,
                    snackbarHostState = snackbarHostState,
                    isOnline = isOnline,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

}