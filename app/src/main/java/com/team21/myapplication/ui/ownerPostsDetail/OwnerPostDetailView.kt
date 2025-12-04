package com.team21.myapplication.ui.ownerPostsDetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.buttons.GrayButton
import com.team21.myapplication.ui.components.cards.ImageCarouselCard
import com.team21.myapplication.ui.components.carousel.HorizontalCarousel
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.icons.ProfilePicture
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.BlueText
import com.team21.myapplication.ui.components.text.GrayText
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.GrayIcon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OwnerPostDetailView(
    images: List<String>,
    title: String,
    address: String,
    rating: Double,
    reviewsCount: Int,
    pricePerMonthLabel: String,
    amenities: List<String>,
    roommatesPhotoUrls: List<String>,
    onBack: () -> Unit,
    onManagePropertyClick: () -> Unit,
    onManageRoommatesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            // Top bar: back + título
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = AppIcons.GoBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                BlackText(
                    text = "Post Detail",
                    size = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }


            Spacer(Modifier.height(16.dp))

            // Carrusel de fotos (reutiliza el de DetailHousingView)
            ImageCarouselCard(
                images = images,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                showCounter = true,
                onImageClick = { /* noop por ahora */ }
            )

            Spacer(Modifier.height(16.dp))

            // Info principal
            BlackText(
                text = title,
                size = 22.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(4.dp))

            BlackText(
                text = address,
                size = 16.sp
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rating + reviews
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = AppIcons.StarFilled,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    BlackText(
                        text = String.format("%.2f", rating),
                        size = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(4.dp))
                    GrayText(
                        text = "$reviewsCount reviews",
                        size = 14.sp
                    )
                }

                Spacer(Modifier.weight(1f))

                // Precio / mes alineado a la derecha
                BlueText(
                    text = pricePerMonthLabel,
                    size = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp, color = GrayIcon)
            Spacer(Modifier.height(16.dp))


            // Amenities (chips grises)
            BlackText(
                text = "Amenities",
                size = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                amenities.forEach { label ->
                    GrayButton(
                        text = label,
                        onClick = { /* solo visual */ },
                        compact = false
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Botón Manage de la propiedad (compacto, centrado)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                BlueButton(
                    text = "Manage",
                    onClick = onManagePropertyClick
                )
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(thickness = 1.dp, color = GrayIcon)
            Spacer(Modifier.height(16.dp))

            // Roommates
            BlackText(
                text = "Current Roommates",
                size = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(16.dp))

            if (roommatesPhotoUrls.isNotEmpty()) {
                HorizontalCarousel(
                    items = roommatesPhotoUrls,
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    horizontalSpacing = 12.dp,
                    snapToItems = false
                ) { url ->
                    ProfilePicture(
                        imageUrl = url
                    )
                }
            } else {
                GrayText(
                    text = "No roommates registered yet.",
                    size = 14.sp
                )
            }

            Spacer(Modifier.height(18.dp))

            // Botón Manage para roommates (compacto, centrado)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                BlueButton(
                    text = "Manage",
                    onClick = onManageRoommatesClick
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFCFCFC, widthDp = 360)
@Composable
fun OwnerPostDetailView_Preview() {
    AppTheme {
        OwnerPostDetailView(
            images = listOf(
                "https://res.cloudinary.com/dzglyzgv3/image/upload/v1763486450/housing_posts/6bqcbrqH5bEhBbh1rFpY/image_2_da2621a6-2de2-4c56-bbae-ca69a79610b1.jpg",
                "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800",
                "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800"
            ),
            title = "Quiet Room with workspace – Usaquén",
            address = "Calle 127 #13-89",
            rating = 4.95,
            reviewsCount = 22,
            pricePerMonthLabel = "$2,906,060 / month",
            amenities = listOf(
                "Private Backyard",
                "New Kitchen",
                "Center Island",
                "Menlo Park",
                "Laundry in Unit"
            ),
            roommatesPhotoUrls = listOf(
                "https://images.unsplash.com/photo-1544723795-3fb6469f5b39?w=200",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=200",
                "https://images.unsplash.com/photo-1520813792240-56fc4a3765a7?w=200"
            ),
            onBack = {},
            onManagePropertyClick = {},
            onManageRoommatesClick = {}
        )
    }
}
