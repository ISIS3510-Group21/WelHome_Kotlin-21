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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import com.team21.myapplication.R
import com.team21.myapplication.ui.theme.WhiteBackground
import com.team21.myapplication.ui.theme.GrayIcon

@Composable
fun ProfilePicture(
    painter: Painter,
    modifier: Modifier = Modifier,
    backgroundColor: Color = WhiteBackground,
    borderColor: Color = GrayIcon
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .background(backgroundColor, shape = CircleShape)
            .border(1.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = "Profile picture",
            modifier = Modifier.size(55.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewProfilePicture() {
    AppTheme {
        ProfilePicture(
            painter = painterResource(R.drawable.profile_picture_women)
        )
    }
}
