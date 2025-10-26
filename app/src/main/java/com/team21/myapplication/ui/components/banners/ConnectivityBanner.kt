package com.team21.myapplication.ui.components.banners

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team21.myapplication.ui.components.icons.AppIcons
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.style.TextAlign

enum class BannerPosition { Top, Bottom }

@Composable
fun ConnectivityBanner(
    visible: Boolean,
    modifier: Modifier = Modifier,
    message: String = "No internet connection",
    icon: ImageVector = AppIcons.WifiOff,
    position: BannerPosition = BannerPosition.Top
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier
            .fillMaxWidth(),
        enter = androidx.compose.animation.slideInVertically(
            initialOffsetY = { if (position == BannerPosition.Top) -it else it }
        ) + androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.slideOutVertically(
            targetOffsetY = { if (position == BannerPosition.Top) -it else it }
        ) + androidx.compose.animation.fadeOut()
    ) {
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
