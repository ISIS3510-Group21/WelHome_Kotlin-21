package com.team21.myapplication.ui.ownerVisitsDetail.state

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team21.myapplication.ui.ownerVisitsDetail.OwnerVisitDetailView
import com.team21.myapplication.ui.ownerVisitsDetail.OwnerVisitDetailViewModel

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

    val state by viewModel.state.collectAsState()

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

    // Mostrar loading o contenido
    when {
        state.isLoading -> {
            // TODO: Mostrar loading indicator
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        state.error != null -> {
            // TODO: Mostrar error
            Box(
                modifier = Modifier.fillMaxSize(),
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
                onCommentChange = { viewModel.updateOwnerComment(it) },
                onSaveComment = { /* TODO: Implementar después */ },
                onMessageClick = { /* TODO: Implementar después */ },
                onBackClick = onBack,
                modifier = modifier
            )
        }
    }
}