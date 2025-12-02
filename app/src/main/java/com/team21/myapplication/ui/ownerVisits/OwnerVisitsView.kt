package com.team21.myapplication.ui.ownerVisits

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import android.app.Activity
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.team21.myapplication.data.model.OwnerScheduledVisit
import com.team21.myapplication.ui.components.cards.ScheduledVisitCard
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.GrayText
import com.team21.myapplication.ui.ownerVisits.state.OwnerVisitsViewModelFactory
import com.team21.myapplication.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.team21.myapplication.ui.components.banners.BannerPosition
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import com.team21.myapplication.utils.NetworkMonitor

@Composable
fun OwnerVisitsRoute(
    onVisitClick: (OwnerScheduledVisit) -> Unit = {},
    onAddVisitClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val factory = OwnerVisitsViewModelFactory(application)
    val viewModel: OwnerVisitsViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()
    //observar conectividad
    val networkMonitor = remember {
        NetworkMonitor.get(context)
    }
    val isOnline by networkMonitor.isOnline.collectAsState()

    val statusBarColor = if (!isOnline) {
        Color.Black
    } else {
        MaterialTheme.colorScheme.background
    }
    val view = LocalView.current

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

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val hasVisits = viewModel.state.value.visits.isNotEmpty()

                // Siempre recargamos, incluso offline (usará cache + drafts locales)
                viewModel.loadOwnerVisits(showLoading = !hasVisits)


                // Solo refrescamos si hay internet
                //if (isOnline) {
                    //val hasVisits = viewModel.state.value.visits.isNotEmpty()

                    // Si ya hay visitas, refrescamos SIN mostrar loading
                    // Si no hay visitas, sí mostramos el loading
                    //viewModel.loadOwnerVisits(showLoading = !hasVisits)
                //}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    OwnerVisitsScreen(
        visits = state.visits,
        isLoading = state.isLoading,
        error = state.error,
        onVisitClick = onVisitClick,
        onAddVisitClick = onAddVisitClick,
        modifier = modifier,
        isOnline = isOnline
    )
}

@Composable
private fun OwnerVisitsScreen(
    visits: List<OwnerScheduledVisit>,
    isLoading: Boolean,
    error: String?,
    onVisitClick: (OwnerScheduledVisit) -> Unit,
    onAddVisitClick: () -> Unit,
    modifier: Modifier = Modifier,
    isOnline: Boolean = true
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Past", "Available", "All")

    val currentTime = System.currentTimeMillis()

    val filteredVisits = when (tabs[selectedTabIndex]) {
        "Upcoming" -> visits.filter { it.timestamp >= currentTime && !it.isAvailable }
        "Past" -> visits.filter { it.timestamp < currentTime && !it.isAvailable }
        "Available" -> visits.filter { it.isAvailable }
        else -> visits
    }.sortedBy { it.timestamp }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddVisitClick,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add visit",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // banner de conectividad
            ConnectivityBanner(
                visible = !isOnline,
                position = BannerPosition.Top
            )

            val currentTab = tabs[selectedTabIndex]

            val emptyStateMessage = when (currentTab) {
                "Upcoming" -> "You have no upcoming visits"
                "Past" -> "You have no past visits"
                "Available" -> "You have no available time slots"
                "All" -> "You have no visits yet"
                else -> "You have no visits yet"
            }

            // Contenido según el estado
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GrayText(
                                text = "Error loading visits",
                                size = 16.sp
                            )
                            GrayText(
                                text = error,
                                size = 14.sp
                            )
                        }
                    }
                }

                filteredVisits.isEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        // Header + tabs
                        item {
                            VisitsHeaderWithTabs(
                                tabs = tabs,
                                selectedTabIndex = selectedTabIndex,
                                onTabSelected = { selectedTabIndex = it }
                            )
                        }

                        // Mensaje de estado vacío
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp)
                                    .padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                GrayText(
                                    text = emptyStateMessage,
                                    size = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }


                else -> {
                    // Lista de visitas
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        // Primer item: título + tabs + espacio
                        item {
                            VisitsHeaderWithTabs(
                                tabs = tabs,
                                selectedTabIndex = selectedTabIndex,
                                onTabSelected = { selectedTabIndex = it }
                            )
                        }

                        // Resto de ítems: las visitas
                        items(filteredVisits) { visit ->
                            val clickHandler: () -> Unit =
                                if (visit.isPendingDraft) {
                                    {}   // no hace nada
                                } else {
                                    { onVisitClick(visit) }
                                }

                            ScheduledVisitCard(
                                date = SimpleDateFormat("EEE, MMM dd", Locale.ENGLISH)
                                    .format(visit.date.toDate()),
                                timeRange = visit.timeRange,
                                propertyName = visit.propertyName,
                                visitorName = visit.visitorName,
                                propertyImageUrl = visit.propertyImageUrl,
                                status = visit.status,
                                isPending = visit.isPendingDraft,
                                onCardClick = clickHandler
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun VisitsHeaderWithTabs(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Spacer(modifier = Modifier.height(40.dp))

    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        BlackText(
            text = "Scheduled Visits",
            size = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                val background = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    Color.Transparent

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(background)
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    BlackText(
                        text = title,
                        size = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC)
@Composable
fun OwnerVisitsScreenPreview() {
    val currentTime = System.currentTimeMillis()
    val oneDayMillis = 24 * 60 * 60 * 1000L

    val sampleVisits = listOf(
        OwnerScheduledVisit(
            bookingId = "1",
            date = Timestamp(Date(currentTime + oneDayMillis)),
            timeRange = "4:00 PM - 5:00 PM",
            propertyName = "Portal de los Rosales",
            visitorName = "John Doe",
            propertyImageUrl = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=400",
            status = "Scheduled",
            timestamp = currentTime + oneDayMillis
        )
    )

    AppTheme {
        OwnerVisitsScreen(
            visits = sampleVisits,
            isLoading = false,
            error = null,
            onVisitClick = {},
            onAddVisitClick = {}
        )
    }
}