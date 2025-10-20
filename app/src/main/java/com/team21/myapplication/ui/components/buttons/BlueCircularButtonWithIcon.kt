package com.team21.myapplication.ui.components.buttons

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.BlueCallToAction
import com.team21.myapplication.ui.theme.WhiteBackground

@Composable
fun BlueCircularButtonWithIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageVector: ImageVector? = null,
    painter: Painter? = null,
    contentDescription: String? = null,
    enabled: Boolean = true,
    size: Int = 72
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(size.dp),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor =  MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.background
        )
    ) {
        when {
            imageVector != null -> Icon(imageVector, contentDescription, tint = MaterialTheme.colorScheme.background)
            painter != null     -> Icon(painter,      contentDescription, tint = MaterialTheme.colorScheme.background)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BlueCircularButtonWithIcon_Preview() {
    AppTheme {
        BlueCircularButtonWithIcon(
            onClick = {},
            imageVector = Icons.Filled.Star,
            contentDescription = "Rate"
        )
    }
}
