package com.team21.myapplication.ui.detailView

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.team21.myapplication.analytics.AnalyticsHelper
import com.team21.myapplication.cache.RecentDetailCache
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.data.repository.HousingPostRepository
import com.team21.myapplication.data.repository.StudentUserRepository
import com.team21.myapplication.ui.bookVisit.BookVisitActivity

// Connectivity banner
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import com.team21.myapplication.ui.components.banners.BannerPosition
import com.team21.myapplication.utils.NetworkMonitor

@Composable
fun DetailHousingRoute(
    housingId: String,
    onBack: () -> Unit = {}
) {
    // Factory-friendly VM (AndroidViewModel(Application) primary constructor)
    val vm: DetailHousingViewModel = viewModel()

    // [EVENTUAL CONNECTIVITY] observe connectivity and show banner
    val ctx = LocalContext.current
    val isOnline by remember { NetworkMonitor.get(ctx).isOnline }.collectAsStateWithLifecycle()

    // [CACHING] Non-singleton MRU kept by the Route
    val recentCache = remember { RecentDetailCache(maxSize = 20) }

    // State
    val uiState by vm.state.collectAsStateWithLifecycle()

    // Load or refresh whenever id / connectivity changes
    LaunchedEffect(housingId, isOnline) {
        vm.load(housingId = housingId, isOnline = isOnline, recentCache = recentCache)
    }

    // Existing analytics dependencies (unchanged)
    val analytics = remember { AnalyticsHelper(ctx.applicationContext) }
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

    Column {
        // [EVENTUAL CONNECTIVITY] Banner visible when offline
        ConnectivityBanner(visible = !isOnline, position = BannerPosition.Top)

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
                    onBookVisit = {
                        val intent = Intent(ctx, BookVisitActivity::class.java)
                            .putExtra(BookVisitActivity.EXTRA_HOUSING_ID, housingId)
                        ctx.startActivity(intent)
                    },
                    onViewAllAmenities = { /* TODO */ },
                    onToggleFavorite = { /* TODO */ },
                    onCallHost = { /* TODO */ },
                    onMessageHost = { /* TODO */ }
                )
            }
        }
    }
}
