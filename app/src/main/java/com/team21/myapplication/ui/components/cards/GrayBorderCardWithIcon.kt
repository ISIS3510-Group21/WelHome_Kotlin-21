package com.team21.myapplication.ui.components.cards

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.team21.myapplication.ui.theme.*

@Composable
fun GrayBorderCardWithIcon(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = GrayIcon,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteBackground),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = BlackText,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
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
