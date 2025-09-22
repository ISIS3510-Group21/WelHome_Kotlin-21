package com.team21.myapplication.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team21.myapplication.data.model.HousingPreview
import com.team21.myapplication.ui.components.cards.HousingInfoCard
import com.team21.myapplication.ui.components.carousel.HorizontalCarousel
import com.team21.myapplication.ui.components.inputs.SearchBar
import com.team21.myapplication.ui.components.navbar.AppNavBar
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.LocalDSTypography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainActivityViewModel = MainActivityViewModel()) {

    val recommendedHousingPosts by viewModel.recommendedHousingPosts.collectAsStateWithLifecycle()
    val recentlySeenHousingPosts by viewModel.recentlySeenHousingPosts.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Scaffold (
        bottomBar = {
            AppNavBar(
                currentRoute = "home",
                onNavigate = { /* noop */ }
            )
        }
    ) {
        innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            item {
                Row( modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                ) {
                    SearchBar(
                        query = "",
                        onQueryChange = {},
                        placeholder = "Search"
                    )
                }
            }
            item {
                SectionTitle("Recommended for you", onBellClick = {})
            }
            item {
                RecommendedForYouSection(recommendedHousingPosts)
            }
            item{
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "Recently seen",
                        style = LocalDSTypography.current.Section,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            items(recentlySeenHousingPosts) { listing ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                ) {
                   HousingInfoCard(
                       title = listing.title,
                       rating = listing.rating.toDouble(),
                       reviewsCount = 0,
                       pricePerMonthLabel = listing.price.toString() + "/month",
                       imageUrl = listing.photoPath

                   )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    onBellClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = LocalDSTypography.current.Section,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onBellClick) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Ver notificaciones"
            )
        }
    }
}

@Composable
fun RecommendedForYouSection(recommendedHousingPosts: List<HousingPreview> = emptyList()) {
    // Sample data - replace with your actual data source and data class
    val recommendedItems = recommendedHousingPosts.map {
        ListingItemData(
            title = it.title,
            imageUrl = it.photoPath,
            rating = 4.9,
            reviewsCount = 0,
            pricePerMonthLabel = it.price.toString() + "/month"
        )
    }

    HorizontalCarousel( items = recommendedItems) {
        item ->
        HousingInfoCard(
            title = item.title,
            rating = item.rating,
            reviewsCount = item.reviewsCount,
            pricePerMonthLabel = item.pricePerMonthLabel,
            imageUrl = item.imageUrl
        )
    }
}

// Data class for listing items
data class ListingItemData(
    val title: String,
    val imageUrl: String,
    val rating: Double = 4.0,
    val reviewsCount: Int = 30,
    val pricePerMonthLabel: String = "$700/month")

// Sample data for demonstration
val sampleListingData = List(10) { index ->
    ListingItemData("Spacious Apartment ${index + 1}", "")
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview_MainScreen() {
    AppTheme {
        MainScreen()
    }
}