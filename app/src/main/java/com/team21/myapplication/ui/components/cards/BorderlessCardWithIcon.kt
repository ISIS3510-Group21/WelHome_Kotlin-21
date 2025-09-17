package com.team21.myapplication.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.ui.theme.*

@Composable
fun BorderlessCardWithIcon(
    text: String,
    modifier: Modifier = Modifier,
    imageVector: ImageVector? = null,
    painter: Painter? = null,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
    iconTint: androidx.compose.ui.graphics.Color = LavanderLight,
    textColor: androidx.compose.ui.graphics.Color = BlackText,
    cornerRadiusDp: Int = 12
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = WhiteBackground,
                shape = RoundedCornerShape(cornerRadiusDp.dp)
            )
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            imageVector != null -> Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = iconTint
            )
            painter != null -> Icon(
                painter = painter,
                contentDescription = contentDescription,
                tint = iconTint
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = LocalDSTypography.current.Section,
            color = textColor
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BorderlessCardWithIcon_Preview() {
    AppTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BorderlessCardWithIcon(
                text = "First List Title",
                imageVector = androidx.compose.material.icons.Icons.Default.Home
            )
            BorderlessCardWithIcon(
                text = "Second List Title",
                imageVector = androidx.compose.material.icons.Icons.Default.Favorite
            )
        }
    }
}
