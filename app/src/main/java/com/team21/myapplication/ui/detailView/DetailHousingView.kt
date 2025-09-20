package com.team21.myapplication.ui.detailView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.R
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.buttons.GrayButton
import com.team21.myapplication.ui.components.cards.GrayBorderCardWithIcon
import com.team21.myapplication.ui.components.cards.HousingCard
import com.team21.myapplication.ui.components.carousel.HorizontalCarousel
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.icons.ProfilePicture
import com.team21.myapplication.ui.components.navbar.AppNavBar
import com.team21.myapplication.ui.components.text.HousingInfoText
import com.team21.myapplication.ui.components.cards.ImageCarouselCard
import com.team21.myapplication.ui.theme.AppTextStyles
import com.team21.myapplication.ui.theme.BlackText
import com.team21.myapplication.ui.theme.GrayIcon
import com.team21.myapplication.ui.theme.WhiteBackground
import com.team21.myapplication.ui.theme.BlueCallToAction
import com.team21.myapplication.ui.theme.AppTheme

@Composable
fun DetailHousingView(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onCallHost: () -> Unit = {},
    onMessageHost: () -> Unit = {},
    onViewAllAmenities: () -> Unit = {},
    onBookVisit: () -> Unit = {}
) {
    Scaffold(
        containerColor = WhiteBackground,
        bottomBar = {
            AppNavBar(
                currentRoute = "home",
                onNavigate = { /* TODO: conectar navegación */ },
                showDivider = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = AppIcons.GoBack,
                    contentDescription = "Back",
                    tint = BlueCallToAction,
                    modifier = Modifier
                        .size(30.dp)
                        .padding(4.dp)
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = AppIcons.HeartSaved,
                    contentDescription = "Save",
                    tint = BlueCallToAction,
                    modifier = Modifier
                        .size(30.dp)
                        .padding(4.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

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

            HousingInfoText(
                title = "Portal de los Rosales",
                rating = 4.95,
                reviewsCount = 22,
                pricePerMonthLabel = "$750’000/month"
            )

            Spacer(Modifier.height(12.dp))

            Divider(color = GrayIcon, thickness = 1.dp)

            Spacer(Modifier.height(15.dp))

            Text(
                text = "Amenities",
                style = AppTextStyles.SubtitleView,
                color = BlackText
            )

            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GrayBorderCardWithIcon(
                        text = "5 beds",
                        icon = AppIcons.Houses,
                        modifier = Modifier.weight(1f)
                    )
                    GrayBorderCardWithIcon(
                        text = "2 bath",
                        icon = AppIcons.Bath,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GrayBorderCardWithIcon(
                        text = "70 meters",
                        icon = AppIcons.SquareMeters,
                        modifier = Modifier.weight(1f)
                    )
                    GrayBorderCardWithIcon(
                        text = "6 people",
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
                color = BlackText
            )

            Spacer(Modifier.height(20.dp))

            val roommates = listOf(
                R.drawable.profile_picture_women,
                R.drawable.profile_picture_women2,
                R.drawable.profile_picture_man
            )
            HorizontalCarousel(
                items = roommates,
                contentPadding = PaddingValues(horizontal = 0.dp),
                horizontalSpacing = 12.dp,
                snapToItems = false
            ) { icon ->
                ProfilePicture(painter = painterResource(icon))
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfilePicture(painter = painterResource(R.drawable.profile_picture_owner), modifier = Modifier.size(56.dp))
                Spacer(Modifier.width(15.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vova Parkhomchuk",
                        style = AppTextStyles.Description,
                        color = BlackText
                    )
                    Text(
                        text = "World-renowned startup founder",
                        style = AppTextStyles.Description,
                        color = GrayIcon
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(
                        imageVector = AppIcons.Phone,
                        contentDescription = "Call",
                        tint = BlackText,
                        modifier = Modifier.size(40.dp)
                    )
                    Icon(
                        imageVector = AppIcons.Message,
                        contentDescription = "Message",
                        tint = BlackText,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Location",
                style = AppTextStyles.SubtitleView,
                color = BlackText
            )
            Spacer(Modifier.height(18.dp))
            HousingCard(imageRes = R.drawable.example_map)

            Spacer(Modifier.height(16.dp))

            Divider(color = GrayIcon, thickness = 1.dp)
            Spacer(Modifier.height(18.dp))
            BlueButton(
                text = "Book Visit",
                onClick = onBookVisit,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, name = "DetailHousingView")
@Composable
private fun PreviewDetailHousingView() {
    AppTheme { DetailHousingView() }
}
