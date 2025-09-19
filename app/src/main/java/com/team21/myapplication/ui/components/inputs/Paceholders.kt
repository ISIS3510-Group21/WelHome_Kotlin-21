package com.team21.myapplication.ui.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team21.myapplication.ui.theme.Poppins
import com.team21.myapplication.ui.theme.LavanderLight
import com.team21.myapplication.ui.theme.GrayIcon
import com.team21.myapplication.ui.components.navigation.icons.AppIcons

@Composable
fun PlaceholderTextField(
    placeholderText: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LavanderLight,
    textColor: Color = GrayIcon,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 1.dp,
    height: Dp = 52.dp,
    trailingIcon: @Composable (() -> Unit)? = null // Optional Composable for the icon
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.TopStart // Aligns content to the top-left
    ) {
        // Use a Row to position the text and icon
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top, // Align items to the top of the Row
            horizontalArrangement = Arrangement.SpaceBetween // Push text to the start, icon to the end
        ) {
            Text(
                text = placeholderText,
                style = TextStyle(
                    fontFamily = Poppins,
                    fontSize = 16.sp,
                    color = textColor
                ),
                modifier = Modifier
                    .weight(1f) // Text takes up all available space
                    .padding(end = 8.dp) // Add some space before the icon
            )
            // If an icon is provided, display it
            if (trailingIcon != null) {
                Box(
                    modifier = Modifier.wrapContentSize(),
                    contentAlignment = Alignment.TopEnd // Align icon to the top-right
                ) {
                    trailingIcon()
                }
            }
        }
    }
}

// A preview of the component
@Preview(showBackground = true)
@Composable
private fun PlaceholderTextFieldPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Default placeholder (no icon)
        PlaceholderTextField(
            placeholderText = "Ex: Cozy Home"
        )

        // 2. Placeholder with a custom icon
        PlaceholderTextField(
            placeholderText = "Enter a valid address",
            trailingIcon = {
                Icon(
                    imageVector = AppIcons.Notification,
                    contentDescription = "Info",
                    tint = GrayIcon
                )
            }
        )

        // 3. Placeholder with a different height and an icon on top-right
        PlaceholderTextField(
            placeholderText = "Long description",
            height = 100.dp,
            trailingIcon = {
                Icon(
                    imageVector = AppIcons.Notification,
                    contentDescription = "Info",
                    tint = Color.Blue
                )
            },
            borderColor = Color.Blue
        )
    }
}