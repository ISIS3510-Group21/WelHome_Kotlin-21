package com.team21.myapplication.ui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.BlueCallToAction
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.theme.BlackText

@Composable
fun BorderButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null // Optional Composable for the icon
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent, // No fill
            contentColor = BlackText // Text and icon color
        ),
        border = BorderStroke(1.dp, BlueCallToAction), // Blue border
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start, // set content to left
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display leading icon if provided
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
            }
            BlackText(
                text = text
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BorderButton_Preview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Preview 1: "Add main photo" button with an Add icon
            BorderButton(
                text = "Add main photo",
                onClick = {},
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.Home, // Using your custom icon
                        contentDescription = "Add main photo icon",
                        tint = BlackText
                    )
                }
            )

            // Preview 2: Button without an icon
            BorderButton(
                text = "Map Search",
                onClick = {}
            )
        }
    }
}