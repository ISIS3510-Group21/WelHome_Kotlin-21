package com.team21.myapplication.ui.components.buttons

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.GrayIcon
import com.team21.myapplication.ui.theme.WhiteBackground
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call

@Composable
fun GrayButtonOnlyIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageVector: ImageVector? = null,
    painter: Painter? = null,
    contentDescription: String? = null,
    enabled: Boolean = true,
    size: Int = 64
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(size.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GrayIcon,
            contentColor = WhiteBackground
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        when {
            imageVector != null -> Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = WhiteBackground
            )
            painter != null -> Icon(
                painter = painter,
                contentDescription = contentDescription,
                tint = WhiteBackground
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GrayButtonOnlyIcon_Preview() {
    AppTheme {
        GrayButtonOnlyIcon(
            onClick = {},
            imageVector = Icons.Filled.Call,
            contentDescription = "Call"
        )
    }
}
