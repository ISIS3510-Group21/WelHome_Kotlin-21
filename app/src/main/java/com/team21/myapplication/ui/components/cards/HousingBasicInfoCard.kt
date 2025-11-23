package com.team21.myapplication.ui.components.cards

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.team21.myapplication.R
import com.team21.myapplication.ui.theme.LocalDSTypography
import com.team21.myapplication.ui.theme.AppTheme

@Composable
fun HousingBasicInfoCard(
    title: String,
    rating: Float,
    reviewsCount: Int,
    pricePerMonthLabel: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    imageRes: Int? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        // Color.Unspecified evita pintar un fondo transparente (reduce overdraw)
        colors = CardDefaults.cardColors(containerColor = Color.Unspecified),
        // Elevación a 0 para evitar sombra (otra pasada) y reducir overdraw
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = onClick ?: {}
    ) {
        Column(Modifier.fillMaxWidth()) {

            // El Card ya tiene shape, no necesitamos clip adicional en la imagen
            val imgModifier = Modifier
                .fillMaxWidth()
                .height(180.dp)

            when {
                imageUrl != null -> AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = imgModifier
                )
                imageRes != null -> Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = imgModifier
                )
            }

            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
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
        HousingBasicInfoCard(
            title = "Portal de los Rosales",
            rating = 4.95f,
            reviewsCount = 22,
            pricePerMonthLabel = "$700’000 /month",
            imageRes = R.drawable.sample_house
        )
    }
}
