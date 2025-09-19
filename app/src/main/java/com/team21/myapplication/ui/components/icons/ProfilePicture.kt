package com.team21.myapplication.ui.components.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.theme.LavanderLight
import com.team21.myapplication.ui.theme.GrayIcon

@Composable
fun ProfilePicture(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LavanderLight,
    iconTint: Color = GrayIcon
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .background(backgroundColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Profile Picture",
            tint = iconTint,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewProfilePicture() {
    AppTheme {
        ProfilePicture(
            icon = AppIcons.ProfilePicture
        )
    }
}
