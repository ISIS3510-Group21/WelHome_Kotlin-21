package com.team21.myapplication.ui.detailView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.R
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.alpha
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.buttons.GrayButton
import com.team21.myapplication.ui.components.cards.GrayBorderCardWithIcon
import com.team21.myapplication.ui.components.cards.HousingCard
import com.team21.myapplication.ui.components.cards.ImageCarouselCard
import com.team21.myapplication.ui.components.carousel.HorizontalCarousel
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.icons.ProfilePicture
import com.team21.myapplication.ui.components.navbar.AppNavBar
import com.team21.myapplication.ui.components.text.HousingInfoText
import com.team21.myapplication.ui.detailView.state.DetailHousingUiState
import com.team21.myapplication.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import com.team21.myapplication.ui.components.banners.BannerPosition
import com.team21.myapplication.utils.NetworkMonitor
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp


@Composable
fun DetailHousingView(
    uiState: DetailHousingUiState, // STATE: la View solo dibuja lo que hay en el estado
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onCallHost: () -> Unit = {},
    onMessageHost: () -> Unit = {},
    onViewAllAmenities: () -> Unit = {},
    onBookVisit: () -> Unit = {}
) {

    val ctx = LocalContext.current
    val isOnline by remember { NetworkMonitor.get(ctx) }.isOnline.collectAsState(initial = true)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .padding(top = if (!isOnline) 40.dp else 0.dp)
            ) {

                // Header con back y favorito (acciones siguen siendo callbacks)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = AppIcons.GoBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .size(30.dp)
                            .padding(4.dp)
                            .clickable { onBack() }
                    )
                    Spacer(Modifier.weight(1f))
                    val heartIcon =
                        if (uiState.isSaved) AppIcons.HeartUnSaved else AppIcons.HeartSaved
                    Icon(
                        imageVector = heartIcon,
                        contentDescription = if (uiState.isSaved) "Unsave" else "Save",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .size(30.dp)
                            .padding(4.dp)
                            .let { m ->
                                if (uiState.isFavoriteInFlight) m else m.clickable { onToggleFavorite() }
                            }
                            .alpha(if (uiState.isFavoriteInFlight) 0.4f else 1f) // feedback visual mientras carga
                    )
                }

                Spacer(Modifier.height(10.dp))

                // IMÁGENES: por tu pedido, se mantienen estáticas (defaults)
                ImageCarouselCard(
                    images = listOf(
                        "https://images.unsplash.com/photo-1600585154340-be6161a56a0c",
                        "https://images.unsplash.com/photo-1572120360610-d971b9b78825",
                        "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7",
                        "https://images.unsplash.com/photo-1507089947368-19c1da9775ae"
                    ),
                    onImageClick = { /* noop */ }
                )

                Spacer(Modifier.height(12.dp))

                // INFO PRINCIPAL: dinámico desde STATE
                HousingInfoText(
                    title = uiState.title.ifBlank { "No title" },
                    rating = uiState.rating,
                    reviewsCount = uiState.reviewsCount,
                    pricePerMonthLabel = uiState.pricePerMonthLabel.ifBlank { "$0/month" }
                )

                Spacer(Modifier.height(12.dp))
                Divider(color = GrayIcon, thickness = 1.dp)
                Spacer(Modifier.height(15.dp))

                Text(
                    text = "Amenities",
                    style = AppTextStyles.SubtitleView,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))

                // AMENITIES: textos dinámicos; íconos por defecto
                var defaultAmenities = listOf("5 beds", "2 bath", "70 meters", "6 people")
                var amenityTexts = if (uiState.amenityLabels.isNotEmpty())
                    uiState.amenityLabels.take(4).let { list ->
                        // Rellena con defaults si vienen menos de 4
                        list + List((4 - list.size).coerceAtLeast(0)) { defaultAmenities[it] }
                    } else defaultAmenities

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GrayBorderCardWithIcon(
                            text = amenityTexts[0],
                            icon = AppIcons.Houses,
                            modifier = Modifier.weight(1f)
                        )
                        GrayBorderCardWithIcon(
                            text = amenityTexts[1],
                            icon = AppIcons.Bath,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GrayBorderCardWithIcon(
                            text = amenityTexts[2],
                            icon = AppIcons.SquareMeters,
                            modifier = Modifier.weight(1f)
                        )
                        GrayBorderCardWithIcon(
                            text = amenityTexts[3],
                            icon = AppIcons.People,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(15.dp))
                GrayButton(
                    text = "View All Amenities",
                    onClick = onViewAllAmenities,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))
                Text(
                    text = "Roommates Profile",
                    style = AppTextStyles.SubtitleView,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(20.dp))

                // ROOMMATES: cantidad dinámica; avatares estáticos
                var avatarPool = listOf(
                    R.drawable.profile_picture_women,
                    R.drawable.profile_picture_women2,
                    R.drawable.profile_picture_man
                )
                var roommates = if (uiState.roommateCount > 0) {
                    List(uiState.roommateCount.coerceAtMost(12)) { idx ->
                        avatarPool[idx % avatarPool.size]
                    }
                } else avatarPool // fallback a tus 3 estáticos

                HorizontalCarousel(
                    items = roommates,
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    horizontalSpacing = 12.dp,
                    snapToItems = false
                ) { icon ->
                    ProfilePicture(painter = painterResource(icon))
                }

                Spacer(Modifier.height(20.dp))

                // OWNER: nombre dinámico (foto e íconos estáticos)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfilePicture(
                        painter = painterResource(R.drawable.profile_picture_owner),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.width(15.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.ownerName.ifBlank { "Host" },
                            style = AppTextStyles.Description,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "World-renowned startup founder", // estático
                            style = AppTextStyles.Description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(
                            imageVector = AppIcons.Phone,
                            contentDescription = "Call",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(40.dp)
                        )
                        Icon(
                            imageVector = AppIcons.Message,
                            contentDescription = "Message",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // LOCATION: mapa estático + address dinámico
                Text(
                    text = "Location",
                    style = AppTextStyles.SubtitleView,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                if (uiState.address.isNotBlank()) {
                    Text(
                        text = uiState.address,
                        style = AppTextStyles.Description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                }
                Spacer(Modifier.height(8.dp))
                HousingCard(imageRes = R.drawable.example_map) // imagen estática meanwhile

                Spacer(Modifier.height(16.dp))
                Divider(color = GrayIcon, thickness = 1.dp)
                Spacer(Modifier.height(18.dp))

                BlueButton(
                    text = "Book Visit",
                    onClick = onBookVisit,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(100.dp))
            }
        }

        ConnectivityBanner(
            visible = !isOnline,
            position = BannerPosition.Top,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        )
    }
}

@Preview(showBackground = true, name = "DetailHousingView")
@Composable
private fun PreviewDetailHousingView() {
    AppTheme {
        // STATE: ejemplo de estado (en ejecución real vendrá del ViewModel)
        var previewState = DetailHousingUiState(
            isLoading = false,
            title = "Portal de los Rosales",
            rating = 4.95,
            reviewsCount = 22,
            pricePerMonthLabel = "$750,000/month",
            address = "Cra. 7 #72-21, Bogotá",
            amenityLabels = listOf("3 beds", "2 bath", "75 m²", "Pet friendly"),
            roommateCount = 3,
            ownerName = "OwnerUser123"
        )
        DetailHousingView(uiState = previewState)
    }
}