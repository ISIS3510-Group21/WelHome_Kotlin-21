package com.team21.myapplication.ui.components.text

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.team21.myapplication.ui.theme.BlackText
import com.team21.myapplication.ui.theme.BlueCallToAction
import com.team21.myapplication.ui.theme.GrayIcon
import com.team21.myapplication.ui.theme.WhiteBackground
import com.team21.myapplication.ui.theme.Poppins
import androidx.compose.material3.MaterialTheme

@Composable
fun BlueText(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    underline: Boolean = false
) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            fontFamily = Poppins,
            fontSize = size,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            textDecoration = if (underline) TextDecoration.Underline else TextDecoration.None,
            color = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun WhiteText(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    underline: Boolean = false
) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            fontFamily = Poppins,
            fontSize = size,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            textDecoration = if (underline) TextDecoration.Underline else TextDecoration.None,
            color = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun GrayText(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    underline: Boolean = false
) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            fontFamily = Poppins,
            fontSize = size,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            textDecoration = if (underline) TextDecoration.Underline else TextDecoration.None,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
fun BlackText(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    underline: Boolean = false
) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            fontFamily = Poppins,
            fontSize = size,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            textDecoration = if (underline) TextDecoration.Underline else TextDecoration.None,
            color = MaterialTheme.colorScheme.onSurface
        )
    )
}
