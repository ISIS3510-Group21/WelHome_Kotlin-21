package com.team21.myapplication.ui.components.text

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.team21.myapplication.ui.theme.BlackText
import com.team21.myapplication.ui.theme.GrayIcon
import com.team21.myapplication.ui.theme.LocalDSTypography
import com.team21.myapplication.ui.theme.AppTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme

@Composable
fun HousingInfoText(
    title: String,
    rating: Double,
    reviewsCount: Int,
    pricePerMonthLabel: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {

        // Título
        Text(
            text = title,
            style = LocalDSTypography.current.Section,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(6.dp))

        // ⭐ rating + reviews
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = String.format("%.2f", rating),
                style = LocalDSTypography.current.Description,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "$reviewsCount reviews",
                style = LocalDSTypography.current.Description,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(6.dp))

        // Precio
        Text(
            text = pricePerMonthLabel,
            style = LocalDSTypography.current.Description,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHousingInfoText() {
    AppTheme {
        HousingInfoText(
            title = "Portal de los Rosales",
            rating = 4.95,
            reviewsCount = 22,
            pricePerMonthLabel = "$700’000 /month",
            modifier = Modifier.padding(16.dp)
        )
    }
}
