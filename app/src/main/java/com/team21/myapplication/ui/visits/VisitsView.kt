package com.team21.myapplication.ui.visits

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.team21.myapplication.data.model.Booking
import com.team21.myapplication.ui.components.cards.VisitInfoCard
import com.team21.myapplication.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun VisitsView(
    navController: NavController,
    modifier: Modifier = Modifier,
    visitsViewModel: VisitsViewModel = viewModel()
) {
    val state by visitsViewModel.state.collectAsState()
    VisitsScreen(
        bookings = state.visits,
        onRateClick = { booking ->
            // Handle rate click
        },
        modifier = modifier
    )



}

@Composable
fun VisitsScreen(
    bookings: List<Booking>,
    onRateClick: (Booking) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Scheduled", "Completed")

    val filteredBookings = when (tabs[selectedTabIndex]) {
        "Scheduled" -> bookings.filter { it.state.equals("Scheduled", ignoreCase = true) }
        "Completed" -> bookings.filter {
            it.state.equals("Completed", ignoreCase = true) || it.state.equals(
                "Missed",
                ignoreCase = true
            )
        }
        else -> bookings
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
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
                    val background = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

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
                        onRateClick = { onRateClick(booking) }
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Timestamp, slot: String): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val dateString = sdf.format(timestamp.toDate())
    return "$dateString"
}


@Preview(showBackground = true)
@Composable
fun VisitsViewPreview() {
    // Create timestamps for different dates to make the preview more realistic
    val now = Date()
    val tomorrow = Date(now.time + (1000 * 60 * 60 * 24))
    val yesterday = Date(now.time - (1000 * 60 * 60 * 24))

    val bookings = listOf(
        Booking(id = "1", housingTitle = "Portal de los Rosales", state = "Missed", date = Timestamp(yesterday), slot = "7:00am"),
        Booking(id = "2", housingTitle = "Living 72", state = "Completed", date = Timestamp(yesterday), slot = "7:00am"),
        Booking(id = "3", housingTitle = "CityU", state = "Scheduled", date = Timestamp(tomorrow), slot = "7:00am"),
    )
    AppTheme {
        VisitsScreen(bookings = bookings, onRateClick = {})
    }
}