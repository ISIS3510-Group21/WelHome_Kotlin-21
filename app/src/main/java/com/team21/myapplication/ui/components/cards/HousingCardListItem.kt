package com.team21.myapplication.ui.components.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.team21.myapplication.R
import com.team21.myapplication.ui.theme.LocalDSTypography

@Composable
fun HousingCardListItem(
    modifier: Modifier = Modifier,
    imageRes: Int? = null,
    imageUrl: String? = null,
    title: String,
    rating: Double,
    price: String,
    onClick: (() -> Unit)? = null
){
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(0.dp),
        //elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = onClick ?: {}
    ) {

        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val imageModifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp))

            when {
                imageUrl != null -> {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier = imageModifier,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.sample_house)
                    )
                }

                imageRes != null -> {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = title,
                        modifier = imageModifier,
                        contentScale = ContentScale.Crop
                    )
                }

                else -> {
                    Image(
                        painter = painterResource(id = R.drawable.sample_house),
                        contentDescription = title,
                        modifier = imageModifier,
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = LocalDSTypography.current.SubtitleView,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = String.format("%.2f", rating),
                        style = LocalDSTypography.current.IconText,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = price,
                    style = LocalDSTypography.current.Description,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HousingCardListItemPreview() {
    MaterialTheme {
        HousingCardListItem(
            imageRes = R.drawable.sample_house,
            title = "Portal de los Rosales",
            rating = 4.95,
            price = "$700'000 /month"
        )
    }
}