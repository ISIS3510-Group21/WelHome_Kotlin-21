package com.team21.myapplication.ui.components.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.ui.theme.AppTheme
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import com.team21.myapplication.R
import com.team21.myapplication.ui.theme.WhiteBackground
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip

@Composable
fun ProfilePicture(
    painter: Painter,
    modifier: Modifier = Modifier,
    backgroundColor: Color = WhiteBackground,
    borderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
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
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
        )
    }
}


@Composable
fun ProfilePicture(
    imageUrl: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = WhiteBackground,
    borderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    placeholder: Painter? = null
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .background(backgroundColor, shape = CircleShape)
            .border(1.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Profile picture",
            placeholder = placeholder,
            error = placeholder,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
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
