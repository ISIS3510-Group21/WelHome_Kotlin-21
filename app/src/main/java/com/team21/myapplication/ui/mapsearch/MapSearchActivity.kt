package com.team21.myapplication.ui.mapsearch

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.team21.myapplication.analytics.AnalyticsHelper
import com.team21.myapplication.ui.components.cards.HousingCardListItem
import com.team21.myapplication.ui.components.inputs.SearchBar
import com.team21.myapplication.ui.components.navbar.AppNavBar
import com.team21.myapplication.ui.detailView.DetailHousingActivity
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.LocalDSTypography
import com.team21.myapplication.utils.NetworkMonitor
import com.team21.myapplication.utils.getNetworkType
import kotlinx.coroutines.launch
import java.io.File

class MapSearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { AppNavBar(navController) }
                ) { innerPadding ->
                    MapSearchView(
                        navController,
                        modifier = Modifier.padding(innerPadding),
                        onNavigateToDetail = { housingId ->
                            startActivity(
                                Intent(this, DetailHousingActivity::class.java)
                                    .putExtra(DetailHousingActivity.EXTRA_HOUSING_ID, housingId)
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun resolveHousingId(p: MapLocation): String? {
    return when (val h = p.id) {
        else -> if (h.contains("/")) h.substringAfterLast("/") else h
    }
}

@Composable
fun MapSearchView(
    navController: NavController,
    modifier: Modifier = Modifier,
    mapViewModel: MapSearchViewModel = viewModel(factory = MapSearchViewModelFactory(LocalContext.current.applicationContext as Application)),
    onNavigateToDetail: (String) -> Unit
) {
    val state = mapViewModel.state.collectAsState().value
    val context = LocalContext.current
    val networkMonitor = remember { NetworkMonitor.get(context) }
    val isOnline by networkMonitor.isOnline.collectAsState()

    LaunchedEffect(isOnline) {
        mapViewModel.loadLocations(isOnline)
    }

    // ----- Analytics + timing -----
    val analytics = remember { AnalyticsHelper(context) }

    var loadStartMs by rememberSaveable { mutableStateOf<Long?>(null) }
    var hasLoggedLoadingTime by rememberSaveable { mutableStateOf(false) }

    var mapLoaded by rememberSaveable { mutableStateOf(false) }      // tiles iniciales listos
    var markersDrawn by rememberSaveable { mutableStateOf(false) }   // marcadores ya dibujados en un frame


    LaunchedEffect(Unit) {
        analytics.logMapSearchOpen()  // Evento: map_search_open
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var userLocation by remember {
        mutableStateOf<LatLng?>(null)
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = LatLng(location.latitude, location.longitude)
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(state.userLocation, 12f)
    }

    LaunchedEffect(userLocation) {
        userLocation?.let {

            hasLoggedLoadingTime = false // resetea el flag
            mapLoaded = false
            markersDrawn = false
            loadStartMs = SystemClock.elapsedRealtime() // arranca cronómetro

            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it, 12f),
                durationMs = 1000
            )
            mapViewModel.onUserLocationReceived(it)
        }
    }

    LaunchedEffect(mapLoaded, markersDrawn) {
        val start = loadStartMs
        if (start != null && mapLoaded && markersDrawn && !hasLoggedLoadingTime) {
            withFrameNanos { /* frame extra */ }

            val elapsed = SystemClock.elapsedRealtime() - start
            analytics.logMapSearchLoadingTime(
                timeInMillis = elapsed,
                deviceModel = Build.MODEL,
                network = getNetworkType(context)
            )
            hasLoggedLoadingTime = true
        }
    }

    var searchQuery by remember { mutableStateOf("") }


    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Map Search",
                    style = LocalDSTypography.current.TitleView,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search",
                    onSearch = { /* Search logic */ }
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val coroutineScope = rememberCoroutineScope()
            val listState = rememberLazyListState()
            var selectedLocationId by remember { mutableStateOf<String?>(null) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                if (state.isOffline) {
                    state.mapSnapshotPath?.let {
                        val imageFile = File(it)
                        if (imageFile.exists()) {
                            val bitmap = android.graphics.BitmapFactory.decodeFile(imageFile.absolutePath)
                            Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Offline map snapshot")
                        }
                    }
                } else {
                    var map: GoogleMap? by remember { mutableStateOf(null) }
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapLoaded = {
                            mapLoaded = true
                        }
                    ) {
                        MapEffect(key1 = map) { googleMap ->
                            map = googleMap
                        }
                        userLocation?.let {
                            com.google.maps.android.compose.Marker(
                                state = MarkerState(it),
                                title = "Your Location",
                                snippet = "Your current location",
                                icon = BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                            )
                        }
                        state.locations.forEachIndexed { index, location ->
                            com.google.maps.android.compose.Marker(
                                state = MarkerState(location.position),
                                title = location.title,
                                snippet = "💲 ${location.price} " +
                                        "⭐ ${location.rating} ",
                                onClick = {
                                    selectedLocationId = location.id

                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index)
                                    }
                                    coroutineScope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(location.position, 20f),
                                            800
                                        )
                                    }
                                    true
                                }

                            )
                        }
                    }
                    LaunchedEffect(mapLoaded, state.locations) {
                        if (mapLoaded && state.locations.isNotEmpty()) {
                            map?.snapshot {
                                if (it != null && userLocation != null) {
                                    mapViewModel.saveMapToCache(it, userLocation!!)
                                }
                            }
                            markersDrawn = false               // reinicia la marca
                            withFrameNanos { /* espera 1 frame */ }
                            withFrameNanos { /* segundo frame */ }
                            markersDrawn = true
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(state.locations) { housingItem ->
                    val isSelected = housingItem.id == selectedLocationId

                    HousingCardListItem(
                        imageUrl = housingItem.imageUrl,
                        title = housingItem.title,
                        rating = housingItem.rating,
                        price = housingItem.price,
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .then(
                                if (isSelected) Modifier.border(
                                    width = 2.dp,
                                    color = Color(0xFF2196F3),
                                    shape = RoundedCornerShape(12.dp)
                                ) else Modifier
                            ),
                        onClick = {
                            resolveHousingId(housingItem)?.let { id ->
                                onNavigateToDetail(id)
                            }
                        }

                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapSearchViewPreview() {
    AppTheme {
        val navController = rememberNavController()
        MapSearchView(navController = navController, onNavigateToDetail = {})
    }
}

class MapSearchViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapSearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapSearchViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
