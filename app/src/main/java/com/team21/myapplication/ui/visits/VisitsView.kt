package com.team21.myapplication.ui.visits

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.team21.myapplication.data.model.Booking
import com.team21.myapplication.ui.components.banners.BannerPosition
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import com.team21.myapplication.ui.components.cards.VisitInfoCard
import com.team21.myapplication.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VisitsView(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val factory = VisitsViewModelFactory(application)
    val visitsViewModel: VisitsViewModel = viewModel(factory = factory)

    val state by visitsViewModel.state.collectAsState()
    val isOnline by visitsViewModel.isOnline.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                visitsViewModel.loadBookings()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        VisitsScreen(
            bookings = state.visits,
            pendingRatings = state.pendingRatings, // Pass the pending ratings map
            onRateClick = { booking ->
                // Prevent rating if there is a pending rating for this visit
                if (state.pendingRatings[booking.id] == null) {
                    navController.navigate("rateVisit/${booking.id}")
                }
            },
            isOnline = isOnline
        )
    }
}

@Composable
fun VisitsScreen(
    bookings: List<Booking>,
    pendingRatings: Map<String, Float>, // Receive the map
    onRateClick: (Booking) -> Unit,
    modifier: Modifier = Modifier,
    isOnline: Boolean = true
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Scheduled", "Completed")

    val filteredBookings = when (tabs[selectedTabIndex]) {
        "Scheduled" -> bookings.filter { it.state.equals("Scheduled", ignoreCase = true) }
        "Completed" -> bookings.filter { it.state.equals("Completed", ignoreCase = true) }
        else -> bookings
    }

    val view = LocalView.current

    val statusBarColor = if (!isOnline) {
        androidx.compose.ui.graphics.Color.Black
    } else {
        MaterialTheme.colorScheme.background
    }

    SideEffect {
        val window = (view.context as android.app.Activity).window
        window.statusBarColor = statusBarColor.toArgb()

        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            if (!isOnline) {
                false
            } else {
                statusBarColor.luminance() > 0.5f
            }
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = if (!isOnline) 40.dp else 0.dp)
        ) {
            Spacer(Modifier.height(40.dp))

            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Your Visits",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTabIndex == index
                        val background =
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        val textColor =
                            if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(background)
                                .clickable { selectedTabIndex = index }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = title,
                                color = textColor,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredBookings) { booking ->
                        VisitInfoCard(
                            imageUrl = booking.thumbnail,
                            housingTitle = booking.housingTitle,
                            visitDateTime = formatTimestamp(booking.date, booking.slot),
                            visitStatus = booking.state,
                            rating = booking.rating,
                            pendingRating = pendingRatings[booking.id],
                            onRateClick = { onRateClick(booking) }
                        )
                    }
                }
            }
        }

        ConnectivityBanner(
            visible = !isOnline,
            position = BannerPosition.Top,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        )
    }
}

private fun formatTimestamp(timestamp: Timestamp, slot: String): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

@Preview(showBackground = true)
@Composable
fun VisitsViewPreview() {
    val now = Date()
    val tomorrow = Date(now.time + (1000 * 60 * 60 * 24))
    val yesterday = Date(now.time - (1000 * 60 * 60 * 24))

    val bookings = listOf(
        Booking(id = "1", housingTitle = "Portal de los Rosales", state = "Missed", date = Timestamp(yesterday), slot = "7:00am"),
        Booking(id = "2", housingTitle = "Living 72", state = "Completed", date = Timestamp(yesterday), slot = "7:00am", rating = 4.5f),
        Booking(id = "3", housingTitle = "CityU", state = "Scheduled", date = Timestamp(tomorrow), slot = "7:00am"),
        Booking(id = "4", housingTitle = "Another Place", state = "Completed", date = Timestamp(yesterday), slot = "9:00am", rating = 0f) // Not rated
    )
    AppTheme {
        VisitsScreen(
            bookings = bookings,
            pendingRatings = mapOf("4" to 3.5f), // Example of a pending rating
            onRateClick = {}
        )
    }
}
