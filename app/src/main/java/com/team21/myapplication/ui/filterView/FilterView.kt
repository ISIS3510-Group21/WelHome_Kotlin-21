package com.team21.myapplication.ui.filterView

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.R
import com.team21.myapplication.ui.components.carousel.HorizontalCarousel
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.navbar.AppNavBar
import com.team21.myapplication.ui.components.inputs.SearchBar
import com.team21.myapplication.ui.components.buttons.GrayButtonWithIcon
import com.team21.myapplication.ui.components.buttons.GrayButton
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.cards.HousingInfoCard
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.theme.AppTextStyles
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.BlackText
import com.team21.myapplication.ui.theme.WhiteBackground

// The view represented as an Activity
class FilterViewActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                FilterView()
            }
        }
    }
}

// Main Screen
@Composable
fun FilterView(
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = WhiteBackground,
        topBar = {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                SearchBar(
                    query = "",
                    onQueryChange = {},
                    placeholder = "Search",
                    onSearch = { /* noop */ },
                    enabled = true
                )
            }
        },
        bottomBar = {
            // Navbar with home selected
            AppNavBar(
                currentRoute = "home",
                onNavigate = { /* TODO conectar navegación */ },
                showDivider = true
            )
        }
    ) { inner ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            // Title "Filter by"
            Text(
                text = "Filter by",
                style = AppTextStyles.TitleView,
                color = BlackText
            )

            Spacer(Modifier.height(12.dp))

            // Grid 2x2 of categories (Houses, Rooms, Cabins, Apartments)
            // Two manual rows with two buttons each
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GrayButtonWithIcon(
                        text = "Houses",
                        imageVector = AppIcons.Houses,
                        onClick = { /* TODO: open map screen */ },
                        modifier = Modifier.weight(1f)
                    )
                    GrayButtonWithIcon(
                        text = "Rooms",
                        imageVector = AppIcons.Rooms,
                        onClick = { /* TODO: open map screen */ },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GrayButtonWithIcon(
                        text = "Cabins",
                        imageVector = AppIcons.Cabins,
                        onClick = { /* TODO: open map screen */ },
                        modifier = Modifier.weight(1f)
                    )
                    GrayButtonWithIcon(
                        text = "Apartments",
                        imageVector = AppIcons.Apartments,
                        onClick = { /* TODO: open map screen */ },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Horizontal carousel of chips (GrayButton)
            HorizontalCarousel(
                items = listOf("Private Backyard", "Vape Free", "Car Park"),
                contentPadding = PaddingValues(horizontal = 0.dp),
                horizontalSpacing = 8.dp,
                snapToItems = false // chips can be partially visible
            ) { label ->
                GrayButton(text = label, onClick = { /* TODO: open map screen */ })
            }

            Spacer(Modifier.height(16.dp))

            // Horizontal carousel of housing cards
            HorizontalCarousel(
                items = listOf(
                    // Example Data
                    HouseUi("Living 72", 4.95, 22, "$700'000 /month"),
                    HouseUi("City U", 4.95, 22, "$700'000 /month"),
                    HouseUi("Modern Loft", 4.90, 18, "$680'000 /month")
                ),
                contentPadding = PaddingValues(horizontal = 0.dp),
                horizontalSpacing = 12.dp,
                snapToItems = true
            ) { house ->
                // Card with house info
                HousingInfoCard(
                    title = house.title,
                    rating = house.rating,
                    reviewsCount = house.reviews,
                    pricePerMonthLabel = house.price,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    imageRes = R.drawable.sample_house
                )
            }

            Spacer(Modifier.height(16.dp))

            // Button "Map Search"
            BlueButton(
                text = "Map Search",
                onClick = { /* TODO: open map screen */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

// Data class for housing info in the carousel
private data class HouseUi(
    val title: String,
    val rating: Double,
    val reviews: Int,
    val price: String
)

@Preview(showBackground = true, name = "FilterView")
@Composable
private fun PreviewFilterView() {
    AppTheme { FilterView() }
}
