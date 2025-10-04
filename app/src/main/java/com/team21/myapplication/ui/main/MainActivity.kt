package com.team21.myapplication.ui.main

import android.annotation.SuppressLint
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
import androidx.navigation.compose.rememberNavController
import com.team21.myapplication.data.model.HousingPreview
import com.team21.myapplication.ui.components.cards.HousingBasicInfoCard
import com.team21.myapplication.ui.components.cards.HousingInfoCard
import com.team21.myapplication.ui.components.carousel.HorizontalCarousel
import com.team21.myapplication.ui.components.inputs.SearchBar
import com.team21.myapplication.ui.components.navbar.AppNavBar
import com.team21.myapplication.ui.components.navbar.AppNavGraph
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.BlueCallToAction
import com.team21.myapplication.ui.theme.LocalDSTypography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme { MainEntry() }
        }
    }
}

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
private fun MainEntry() {
    val navController = rememberNavController()
    androidx.compose.material3.Scaffold(
        bottomBar = { AppNavBar(navController) }
    ) {
        AppNavGraph(
            navController = navController
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onOpenFilters: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.homeState.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
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
                            placeholder = "Search",
                            asButton = true,
                            onClick = onOpenFilters
                        )
                    }
                }

                item {
                    SectionTitle("Recommended for you", onBellClick = {})
                }
                item {
                    RecommendedForYouSection(
                        recommendedItems = state.recommendedHousings,
                        onNavigateToDetail = onNavigateToDetail
                    )
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
                            pricePerMonthLabel = "$${listing.price}/month",
                            imageUrl = listing.photoPath,
                            reviewsCount = 0,
                            onClick = {
                                resolveHousingId(listing)?.let { id ->
                                    onNavigateToDetail(id)
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}

/** Resuelve el ID del housing venga como DocumentReference o como String/path. */
private fun resolveHousingId(p: HousingPreview): String? {
    return when (val h = p.housing) {
        is com.google.firebase.firestore.DocumentReference -> h.id
        is String -> if (h.contains("/")) h.substringAfterLast("/") else h
        else -> null
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
fun RecommendedForYouSection(
    recommendedItems: List<HousingPreview>,
    onNavigateToDetail: (String) -> Unit
) {
    HorizontalCarousel(items = recommendedItems) { item ->
        HousingInfoCard(
            title = item.title,
            rating = item.rating.toDouble(),
            reviewsCount = 0,
            pricePerMonthLabel = "$${item.price}/month",
            imageUrl = item.photoPath,
            onClick = {
                resolveHousingId(item)?.let { id ->
                    onNavigateToDetail(id)
                }
            }
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
            CircularProgressIndicator(color = Color.White)
            Text(
                text = "Getting everything ready for you...",
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
    AppTheme { LoadingScreen() }
}
