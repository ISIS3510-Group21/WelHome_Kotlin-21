package com.team21.myapplication.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.R
import com.team21.myapplication.ui.components.cards.HousingInfoCard
import com.team21.myapplication.ui.components.carousel.HorizontalCarousel
import com.team21.myapplication.ui.components.inputs.SearchBar
import com.team21.myapplication.ui.components.navigation.AppNavBar
import com.team21.myapplication.ui.components.navigation.icons.AppIcons
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.components.navigation.icons.IconTile
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
fun MainScreen() {
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
                IconButtonsRow()
            }
            item {
                ListDivider()
            }
            item {
                SectionTitle("Recommended for you")
            }
            item {
                RecommendedForYouSection()
            }
            item {
                ListDivider()
            }
            item {
                SectionTitle("Recently seen")
            }
            // Recently Seen items

            items(sampleListingData) { listing -> // Replace with your actual data
                Row(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
                ) {
                   HousingInfoCard(
                       title = listing.title,
                       rating = listing.rating,
                       reviewsCount = listing.reviewsCount,
                       pricePerMonthLabel = listing.pricePerMonthLabel,
                       imageRes = R.drawable.sample_house
                   )
                }
            }
        }
    }
}

@Composable
fun IconButtonsRow() {
    // Using the AppIcons you've defined
    // For now, using Home icon for all as requested. You can change these later.
    val iconButtonItems = listOf(
        "Houses" to AppIcons.Home,
        "Apartment" to AppIcons.Home, // Placeholder
        "Studio" to AppIcons.Home, // Placeholder
        "Co-Living" to AppIcons.Home, // Placeholder
        "Residence" to AppIcons.Home  // Placeholder
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        iconButtonItems.forEach { (label, icon) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f) // Distribute space equally
            ) {
                IconTile(
                    label, icon, Color.Black
                )
            }
        }
    }
}

@Composable
fun ListDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = LocalDSTypography.current.Section,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun RecommendedForYouSection() {
    // Sample data - replace with your actual data source and data class
    val recommendedItems = List(5) { index ->
        ListingItemData("Beautiful house #$index", R.drawable.sample_house)
    }

    HorizontalCarousel( items = recommendedItems) {
        item ->
        HousingInfoCard(
            title = item.title,
            rating = item.rating,
            reviewsCount = item.reviewsCount,
            pricePerMonthLabel = item.pricePerMonthLabel,
            imageRes = item.imageUrl
        )
    }
}

// Data class for listing items
data class ListingItemData(
    val title: String,
    val imageUrl: Int,
    val rating: Double = 4.0,
    val reviewsCount: Int = 30,
    val pricePerMonthLabel: String = "$700/month") // Use String for actual image URLs

// Sample data for demonstration
val sampleListingData = List(10) { index ->
    ListingItemData("Spacious Apartment ${index + 1}", R.drawable.sample_house) // Replace with actual image
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview_MainScreen() {
    AppTheme {
        MainScreen()
    }
}