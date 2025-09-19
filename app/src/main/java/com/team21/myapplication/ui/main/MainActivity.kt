package com.team21.myapplication.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.R // Assuming you have a placeholder image in drawable
import com.team21.myapplication.ui.components.cards.HousingTitleCard
import com.team21.myapplication.ui.components.inputs.SearchBar
import com.team21.myapplication.ui.components.navigation.icons.AppIcons // Corrected import
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
    Scaffold { innerPadding ->
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
                    HousingTitleCard(listing.title, imageRes = R.drawable.sample_house)
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
                    label, icon
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
        ListingItemData("Beautiful house #$index", R.drawable.ic_launcher_background) // Replace with actual image
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(recommendedItems) { item ->
            HousingTitleCard(item.title, imageRes = R.drawable.sample_house)
        }
    }
}

// Data class for listing items (you should define this according to your model)
data class ListingItemData(val title: String, val imageUrl: Int) // Use String for actual image URLs

// Sample data for demonstration
val sampleListingData = List(10) { index ->
    ListingItemData("Spacious Apartment ${index + 1}", R.drawable.sample_house) // Replace with actual image
}

@Composable
fun RecentlySeenItem(listing: ListingItemData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = listing.imageUrl), // Load image
                contentDescription = listing.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = listing.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 3
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview_MainScreen() {
    AppTheme {
        MainScreen()
    }
}
