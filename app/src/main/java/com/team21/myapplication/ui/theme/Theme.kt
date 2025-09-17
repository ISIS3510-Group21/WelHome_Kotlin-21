package com.team21.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider





// Main Scheme Light Mode
private val LightColors = lightColorScheme(
    primary = BlueCallToAction,
    secondary = BlueSecondary,
    background = WhiteBackground,
    onBackground = BlackText,
    surface = WhiteBackground,
    onSurface = BlackText,
)

@Composable
fun AppTheme(
    dsTypography: DSTypography = DefaultDSTypography, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDSTypography provides dsTypography) { // Our Topography
        MaterialTheme(
            colorScheme = LightColors,
            typography = AppTypography,
            content = content
        )
    }
}