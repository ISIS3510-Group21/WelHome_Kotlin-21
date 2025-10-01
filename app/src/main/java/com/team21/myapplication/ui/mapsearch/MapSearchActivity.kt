package com.team21.myapplication.ui.mapsearch

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerState
import com.team21.myapplication.R
import com.team21.myapplication.ui.components.cards.HousingCardListItem
import com.team21.myapplication.ui.components.inputs.SearchBar
import com.team21.myapplication.ui.components.navbar.AppNavBar
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.LocalDSTypography
import com.google.maps.android.compose.rememberCameraPositionState

class MapSearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val navController = rememberNavController()
                MapSearchView(navController = navController)
            }
        }
    }
}

@Composable
fun MapSearchView(
    navController: NavController,
    modifier: Modifier = Modifier,
    mapViewModel: MapSearchViewModel = viewModel()
) {
    val state by mapViewModel.state.collectAsState()

    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var userLocation by remember {
        mutableStateOf<LatLng?>(null)
    }

    // Request location permission and get the user's current location
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

    // Set the initial camera position to the user's location or a default location
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(state.userLocation, 12f)
    }

    // If userLocation changes, animate the camera to the new location
    // and notify the ViewModel
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it, 12f),
                durationMs = 1000
            )
            mapViewModel.onUserLocationReceived(it)
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
                    onSearch = {
                        // Search logic
                    }
                )
            }
        },
        bottomBar = {
            AppNavBar(navController = navController)
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {

                GoogleMap (
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
               ) {
                    userLocation?.let {
                        com.google.maps.android.compose.Marker(
                            state = MarkerState(it),
                            title = "Your Location",
                            snippet = "Your current location",
                            icon = BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                    }
                   state.locations.forEach { location ->
                       com.google.maps.android.compose.Marker(
                           state = MarkerState(location.position),
                           title = location.title,
                           snippet = "ID ${location.title}"
                       )
                   }
               }
            }

            Spacer(modifier = Modifier.height(16.dp))


            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(state.locations) { housingItem ->
                    HousingCardListItem(
                        imageUrl = housingItem.imageUrl,
                        title = housingItem.title,
                        rating = housingItem.rating.toDouble(),
                        price = housingItem.price,
                        modifier = Modifier.padding(horizontal = 2.dp)
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
        MapSearchView(navController = navController)
    }
}
