package com.team21.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color


// Main Scheme Light Mode
private val LightColors = lightColorScheme(
    primary = BlueCallToAction,
    secondary = BlueSecondary,
    background = WhiteBackground,
    onBackground = BlackText,
    surface = WhiteBackground,
    onSurface = BlackText,
)

private val DarkColors = darkColorScheme(
    primary = BlueCallToAction,
    secondary = BlueSecondary,
    background = BlackText,
    onBackground = WhiteBackground,
    surface = Color(0xFF121212),
    onSurface = WhiteBackground,
)

@Composable
fun AppTheme(
    dsTypography: DSTypography = DefaultDSTypography, content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = if (isDark) DarkColors else LightColors
    CompositionLocalProvider(LocalDSTypography provides dsTypography) { // Our Topography
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}