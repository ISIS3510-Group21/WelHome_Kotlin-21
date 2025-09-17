package com.team21.myapplication.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.team21.myapplication.R


// Font Family (Poppins)
val Poppins = FontFamily(
    Font(R.font.poppins_regular,  FontWeight.Normal),
    Font(R.font.poppins_medium,   FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold,     FontWeight.Bold),
)

// Figma Styles
object AppTextStyles {
    val TitleView = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    )

    val SubtitleView = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    )

    val Description = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )

    val IconText = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )

    val NavBarDescription = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )

    val Section = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    )
}

// DS Typography + Local
data class DSTypography(
    val TitleView: TextStyle,
    val SubtitleView: TextStyle,
    val Description: TextStyle,
    val IconText: TextStyle,
    val NavBarDescription: TextStyle,
    val Section: TextStyle,
)

// Our Typography
val DefaultDSTypography = DSTypography(
    TitleView = AppTextStyles.TitleView,
    SubtitleView = AppTextStyles.SubtitleView,
    Description = AppTextStyles.Description,
    IconText = AppTextStyles.IconText,
    NavBarDescription = AppTextStyles.NavBarDescription,
    Section = AppTextStyles.Section,
)
val LocalDSTypography = staticCompositionLocalOf { DefaultDSTypography }

// Compatibility With Material
val AppTypography = androidx.compose.material3.Typography(
    titleLarge = AppTextStyles.TitleView,
    titleMedium = AppTextStyles.SubtitleView,
    bodyLarge  = AppTextStyles.Description,
    labelSmall = AppTextStyles.NavBarDescription,
)




