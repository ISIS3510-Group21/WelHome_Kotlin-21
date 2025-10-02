package com.team21.myapplication.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team21.myapplication.data.model.HousingPreview
import com.team21.myapplication.ui.components.cards.HousingBasicInfoCard
import com.team21.myapplication.ui.components.cards.HousingInfoCard
import com.team21.myapplication.ui.components.carousel.HorizontalCarousel
import com.team21.myapplication.ui.components.inputs.SearchBar
import com.team21.myapplication.ui.components.navbar.AppNavBar
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.LocalDSTypography
import com.team21.myapplication.ui.theme.BlueCallToAction


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
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.homeState.collectAsStateWithLifecycle()

    Scaffold (
        bottomBar = {
            if (!state.isLoading) {
                AppNavBar(
                    currentRoute = "home",
                    onNavigate = { /* noop */ }
                )
            }
        }
    ) {
        innerPadding ->
        if (state.isLoading) {
            LoadingScreen()
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                item {
                    Row(
                        modifier = Modifier
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
                    RecommendedForYouSection(state.recommendedHousings)
                }
                item {
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

                items(state.recentlySeenHousings) { listing ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                    ) {
                        HousingBasicInfoCard(
                            title = listing.title,
                            rating = listing.rating,
                            pricePerMonthLabel = "$" + listing.price.toString() + "/month",
                            imageUrl = listing.photoPath
                        )
                    }
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
fun RecommendedForYouSection(recommendedItems: List<HousingPreview>) {

    HorizontalCarousel( items = recommendedItems) {
        item ->
        HousingInfoCard(
            title = item.title,
            rating = item.rating.toDouble(),
            reviewsCount = 0,
            pricePerMonthLabel = "$" + item.price.toString() + "/month",
            imageUrl = item.photoPath
        )
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BlueCallToAction)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color.White
            )

            Text(
                text = "Preparando todo para ti...",
                style = LocalDSTypography.current.SubtitleView,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview_MainScreen() {
    AppTheme {
        LoadingScreen()
    }
}