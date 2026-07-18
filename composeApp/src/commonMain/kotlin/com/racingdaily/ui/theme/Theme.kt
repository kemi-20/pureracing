package com.racingdaily.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val RacingRed = Color(0xFFE10600)
val RacingBlue = Color(0xFF58A6FF)
val RacingGreen = Color(0xFF3FB950)
val RacingYellow = Color(0xFFD29922)

private val DarkColors = darkColorScheme(
    primary = RacingRed,
    secondary = RacingBlue,
    tertiary = RacingYellow,
    background = Color(0xFF17202A),
    surface = Color(0xFF26323D),
    surfaceVariant = Color(0xFF31404C),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE6EDF3),
    onSurface = Color(0xFFE6EDF3),
    onSurfaceVariant = Color(0xFFB8C7D4),
    outline = Color.White.copy(alpha = 0.22f),
)

private val LightColors = lightColorScheme(
    primary = RacingRed,
    secondary = Color(0xFF1769AA),
    tertiary = Color(0xFF8B6508),
    background = Color(0xFFF1F6FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFDCE7EF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF17212B),
    onSurface = Color(0xFF17212B),
    onSurfaceVariant = Color(0xFF526575),
    outline = Color(0xFF526575).copy(alpha = 0.28f),
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 34.sp, letterSpacing = 0.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 23.sp, lineHeight = 28.sp, letterSpacing = 0.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 24.sp, letterSpacing = 0.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 21.sp, letterSpacing = 0.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 23.sp, letterSpacing = 0.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp, letterSpacing = 0.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.sp),
)

@Composable
fun RacingDailyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
