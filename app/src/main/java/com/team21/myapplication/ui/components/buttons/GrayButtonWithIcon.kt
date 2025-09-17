package com.team21.myapplication.ui.components.buttons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.LavanderLight
import com.team21.myapplication.ui.theme.BlackText
import com.team21.myapplication.ui.theme.LocalDSTypography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home

@Composable
fun GrayButtonWithIcon(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageVector: ImageVector? = null,
    painter: Painter? = null,
    contentDescription: String? = null
) {
    Card(
        modifier = modifier
            .size(width = 120.dp, height = 80.dp),
        colors = CardDefaults.cardColors(
            containerColor = LavanderLight
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                imageVector != null -> Icon(
                    imageVector = imageVector,
                    contentDescription = contentDescription,
                    tint = BlackText
                )
                painter != null -> Icon(
                    painter = painter,
                    contentDescription = contentDescription,
                    tint = BlackText
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = text,
                style = LocalDSTypography.current.IconText,
                color = BlackText
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GrayButtonWithIcon_Preview() {
    AppTheme {
        GrayButtonWithIcon(
            text = "Houses",
            imageVector = Icons.Filled.Home,
            contentDescription = "Houses",
            onClick = {}
        )
    }
}
