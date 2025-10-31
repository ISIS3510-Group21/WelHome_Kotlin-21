package com.team21.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.isSystemInDarkTheme

// Main Scheme Dark Mode
private val DarkColors = darkColorScheme(
    primary = BlueCTA_Dark,
    secondary = BlueSecondary_Dark,
    background = Background_Dark,
    onBackground = OnBackground_Dark,
    surface = Lavender_Dark,
    onSurface = OnBackground_Dark,
    onSurfaceVariant = GrayIcon_Dark,
    onSecondaryContainer = GrayIcon_Dark, //border button
    tertiaryContainer = Lavender_Dark,
    onTertiaryContainer = OnBackground_Dark, //grayButtonWithIcon
    surfaceContainer = OnBackground_Dark, //GreyButton - selected
    inverseOnSurface = Background_Dark
    )


// Main Scheme Light Mode
private val LightColors = lightColorScheme(
    primary = BlueCallToAction,
    secondary = BlueSecondary,
    background = WhiteBackground,
    onBackground = BlackText,
    surface = WhiteBackground,
    onSurface = BlackText,
    onSurfaceVariant = GrayIcon,
    onSecondaryContainer = BlueCallToAction, //border button
    tertiaryContainer = LavanderLight,
    onTertiaryContainer = BlueCallToAction, //grayButtonWithIcon
    surfaceContainer = LavanderLight, //GreyButton - selected
    inverseOnSurface = BlueCallToAction
)

@Composable
fun AppTheme(
    dsTypography: DSTypography = DefaultDSTypography, content: @Composable () -> Unit) {
    //val isDark = isSystemInDarkTheme() //saber si está encendido el dark mode
    //val colorScheme = if (isDark) DarkColors else LightColors // esquema de colores
    val colorScheme = LightColors
    CompositionLocalProvider(LocalDSTypography provides dsTypography) { // Our Topography
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}