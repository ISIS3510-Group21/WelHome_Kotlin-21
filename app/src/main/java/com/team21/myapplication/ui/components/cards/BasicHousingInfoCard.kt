package com.team21.myapplication.ui.components.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.team21.myapplication.ui.theme.BlackText
import com.team21.myapplication.ui.theme.GrayIcon
import com.team21.myapplication.ui.theme.LocalDSTypography
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.WhiteBackground

@Composable
fun BasicHousingInfoCard(
    title: String,
    pricePerMonthLabel: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    imageRes: Int? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.width(300.dp).height(250.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteBackground),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick ?: {}
    ) {
        Column(Modifier.fillMaxWidth()) {

            val imgModifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))

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
                    color = BlackText
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = pricePerMonthLabel,
                    style = LocalDSTypography.current.Description,
                    color = BlackText
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BasicHousingInfoCard_Preview() {
    AppTheme {
        BasicHousingInfoCard(
            title = "Portal de los Rosales",
            pricePerMonthLabel = "$700’000 /month",
            imageRes = R.drawable.sample_house
        )
    }
}
