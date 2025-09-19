package com.team21.myapplication.ui.components.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import com.team21.myapplication.ui.theme.*

@Composable
fun GrayBorderCardWithIcon(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    minHeight: Dp = 56.dp,
) {
    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GrayIcon),
        colors = CardDefaults.outlinedCardColors(containerColor = WhiteBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = BlackText,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = LocalDSTypography.current.Description,
                color = BlackText
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GrayBorderCardWithIcon_Preview() {
    AppTheme {
        GrayBorderCardWithIcon(
            text = "5 beds",
            icon = androidx.compose.material.icons.Icons.Default.Home
        )
    }
}
