package com.team21.myapplication.ui.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.BlueCallToAction
import com.team21.myapplication.ui.theme.LocalDSTypography
import com.team21.myapplication.ui.theme.WhiteBackground

@Composable
fun BlueButtonWithIcon(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, imageVector: ImageVector? = null, painter: Painter? = null, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BlueCallToAction,
            contentColor = WhiteBackground
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when {
                imageVector != null -> Icon(imageVector, contentDescription = null, tint = WhiteBackground)
                painter != null     -> Icon(painter, contentDescription = null, tint = WhiteBackground)
            }
            Text(
                text = text,
                style = LocalDSTypography.current.Description,
                color = WhiteBackground
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BlueButtonWithIcon_Preview_MaterialIcon() {
    AppTheme {
        BlueButtonWithIcon(
            text = "Send a message",
            imageVector = Icons.Filled.Email,
            onClick = {}
        )
    }
}

