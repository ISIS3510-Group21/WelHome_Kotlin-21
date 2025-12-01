package com.team21.myapplication.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun VisitInfoCard(
    modifier: Modifier = Modifier,
    imageUrl: String,
    housingTitle: String,
    visitDateTime: String,
    visitStatus: String,
    rating: Float,
    pendingRating: Float?, // New parameter for pending rating
    onRateClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Property Image",
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = housingTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(text = visitDateTime, color = Color.Gray, fontSize = 16.sp)
                Text(
                    text = visitStatus,
                    color = if (pendingRating != null) Color.Gray else Color(0xFFB0A8E0),
                    fontSize = 16.sp
                )
            }

            // Logic for the right side (rating/button)
            if (visitStatus.equals("Completed", ignoreCase = true)) {
                when {
                    // 1. If there's a pending rating, show it in a disabled state
                    pendingRating != null -> {
                        PendingRatingIndicator(rating = pendingRating)
                    }
                    // 2. If there's a confirmed rating, show it normally
                    rating > 0 -> {
                        ConfirmedRatingIndicator(rating = rating)
                    }
                    // 3. If no rating and not pending, show the "Rate" button
                    else -> {
                        RateButton(onClick = onRateClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingRatingIndicator(rating: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = String.format("%.1f", rating),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Gray // Disabled color
        )
        Icon(
            imageVector = Icons.Filled.Star, // Using filled star to indicate it's a set value
            contentDescription = "Pending Rating",
            tint = Color.Gray, // Disabled color
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "Pending",
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ConfirmedRatingIndicator(rating: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = String.format("%.1f", rating),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A448C)
        )
        Icon(
            imageVector = Icons.Outlined.Star,
            contentDescription = "Rated",
            tint = Color(0xFF4A448C),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun RateButton(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .size(60.dp)
                .background(Color(0xFF4A448C), shape = CircleShape),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Star,
                contentDescription = "Rate",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = "Rate",
            color = Color(0xFF4A448C),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Preview(showBackground = true, name = "Rated Visit")
@Composable
fun VisitInfoCardPreview() {
    VisitInfoCard(
        imageUrl = "https://www.nydailynews.com/wp-content/uploads/migration/2012/09/21/4YDFQ5XGGZKTZLJSZHGXLUON2A.jpg",
        housingTitle = "CityU",
        visitDateTime = "Aug 23 • 7:00am",
        visitStatus = "Completed",
        rating = 4.5f,
        pendingRating = null,
        onRateClick = {}
    )
}

@Preview(showBackground = true, name = "Not Rated Visit")
@Composable
fun VisitInfoCardNotRatedPreview() {
    VisitInfoCard(
        imageUrl = "https://www.nydailynews.com/wp-content/uploads/migration/2012/09/21/4YDFQ5XGGZKTZLJSZHGXLUON2A.jpg",
        housingTitle = "CityU",
        visitDateTime = "Aug 23 • 7:00am",
        visitStatus = "Completed",
        rating = 0f,
        pendingRating = null,
        onRateClick = {}
    )
}

@Preview(showBackground = true, name = "Pending Rating Visit")
@Composable
fun VisitInfoCardPendingPreview() {
    VisitInfoCard(
        imageUrl = "https://www.nydailynews.com/wp-content/uploads/migration/2012/09/21/4YDFQ5XGGZKTZLJSZHGXLUON2A.jpg",
        housingTitle = "CityU",
        visitDateTime = "Aug 23 • 7:00am",
        visitStatus = "Completed",
        rating = 0f,
        pendingRating = 3.5f,
        onRateClick = {}
    )
}