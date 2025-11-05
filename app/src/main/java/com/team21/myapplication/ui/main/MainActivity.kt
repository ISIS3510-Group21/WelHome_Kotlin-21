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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import com.google.firebase.messaging.FirebaseMessaging
import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.team21.myapplication.ui.components.banners.BannerPosition
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import com.team21.myapplication.utils.App

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_START_DEST = "EXTRA_START_DEST" // ← NEW
    }
    // Launcher para pedir POST_NOTIFICATIONS en Android 13+
    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* noop: si lo niegan, solo no mostramos notifs */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Suscribe al topic una vez (ok si se llama repetido)
        FirebaseMessaging.getInstance().subscribeToTopic("trending_filters")
        FirebaseMessaging.getInstance().subscribeToTopic("all")


        // Pide permiso de notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= 33) {
            requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            AppTheme {
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    val route = intent?.getStringExtra(EXTRA_START_DEST)
                    if (!route.isNullOrBlank()) {
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                        }
                        // evita que re-dispare si se recrea
                        intent?.removeExtra(EXTRA_START_DEST)
                    }
                }

                // Manejar deep link del intent que abrió la Activity
                val ctx = LocalContext.current
                LaunchedEffect(Unit) {
                    if ((ctx as? Activity)?.intent?.getBooleanExtra("login_success", false) == true) {
                        android.widget.Toast.makeText(ctx, "Successful login!", android.widget.Toast.LENGTH_SHORT).show()
                        ctx.intent.removeExtra("login_success")
                    }
                }

                Scaffold(
                    bottomBar = { AppNavBar(navController) },
                    // ⬇️ Evita que este Scaffold meta insets automáticos
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { inner ->
                    AppNavGraph(
                        navController = navController,
                        modifier = Modifier
                            .padding(inner)              // aplica el padding UNA sola vez
                            .consumeWindowInsets(inner)
                    )
                }
            }
        }
    }
}

/*
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
*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onOpenFilters: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
) {
    val appContext = LocalContext.current.applicationContext

    // Factory con ese context
    val viewModel: MainViewModel = viewModel(
        factory = remember {
            MainViewModel.Factory(
                context = appContext,
                networkMonitor = (appContext as App).networkMonitor
            )
        }
    )
    val state by viewModel.homeState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    val view = LocalView.current

    val statusBarColor = if (!isOnline) {
        androidx.compose.ui.graphics.Color.Black
    } else {
        MaterialTheme.colorScheme.background
    }

    LaunchedEffect(Unit) {
        viewModel.getHousingPosts()
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {

            SideEffect {
                val window = (view.context as android.app.Activity).window
                // Variable de fondo
                window.statusBarColor = statusBarColor.toArgb()

                // La lógica para decidir el color de los íconos
                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
                    if (!isOnline) {
                        false // Íconos blancos para fondo negro
                    } else {
                        statusBarColor.luminance() > 0.5f // Decide según la luminancia del fondo
                    }
            }

            ConnectivityBanner(
                visible = !isOnline,
                position = BannerPosition.Top,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            if (state.isLoading) {
                LoadingScreen(modifier = Modifier.padding(innerPadding))
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
                                    viewModel.logHousingPostClick(
                                        postId = listing.housing,
                                        postTitle = listing.title,
                                        price = listing.price
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Resuelve el ID del housing venga como DocumentReference o como String/path. */
private fun resolveHousingId(p: HousingPreview): String? {
    return when (val h = p.housing) {
        //is com.google.firebase.firestore.DocumentReference -> h.id
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
