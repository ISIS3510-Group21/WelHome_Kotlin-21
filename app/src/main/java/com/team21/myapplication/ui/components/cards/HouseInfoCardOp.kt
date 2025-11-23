package com.team21.myapplication.ui.components.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.team21.myapplication.R
import com.team21.myapplication.ui.theme.LocalDSTypography
import com.team21.myapplication.ui.theme.AppTheme
import androidx.compose.ui.platform.LocalContext

@Composable
fun HouseInfoCard(
    title: String,
    rating: Double,
    reviewsCount: Int,
    pricePerMonthLabel: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    imageRes: Int? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.width(300.dp).height(270.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick ?: {}
    ) {
        Column(Modifier.fillMaxWidth()) {

            val imgModifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))

            when {
                imageUrl != null -> {
                    val context = LocalContext.current
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(150)
                            .memoryCacheKey(imageUrl)
                            .diskCacheKey(imageUrl)
                            .size(800, 600) // Limita el tamaño de decodificación
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = imgModifier
                    )
                }
                imageRes != null -> Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = imgModifier
                )
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = title,
                    style = LocalDSTypography.current.Section,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(6.dp))

                // ⭐ rating + reviews
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = String.format("%.2f", rating),
                        style = LocalDSTypography.current.Description,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$reviewsCount reviews",
                        style = LocalDSTypography.current.Description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = pricePerMonthLabel,
                    style = LocalDSTypography.current.Description,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun HousingInfoCard_Preview() {
    AppTheme {
        HouseInfoCard(
            title = "Portal de los Rosales",
            rating = 4.95,
            reviewsCount = 22,
            pricePerMonthLabel = "$700’000 /month",
            imageRes = R.drawable.sample_house
        )
    }
}
