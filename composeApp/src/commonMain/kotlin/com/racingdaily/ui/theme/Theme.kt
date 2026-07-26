package com.racingdaily.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val RacingRed = Color(0xFFFF453A)
val RacingBlue = Color(0xFF0A84FF)
val RacingGreen = Color(0xFF30D158)
val RacingYellow = Color(0xFFFFD60A)

val LocalPureRacingDarkTheme = staticCompositionLocalOf { false }

private val DarkColors = darkColorScheme(
    primary = RacingRed,
    secondary = RacingBlue,
    tertiary = RacingYellow,
    background = Color(0xFF1B2024),
    surface = Color(0xFF293136),
    surfaceVariant = Color(0xFF364147),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF4F7F8),
    onSurface = Color(0xFFF4F7F8),
    onSurfaceVariant = Color(0xFFC1CDD2),
    outline = Color.White.copy(alpha = 0.22f),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFFD81B24),
    secondary = Color(0xFF007AFF),
    tertiary = Color(0xFF8A6400),
    background = Color(0xFFF1F7F9),
    surface = Color(0xFFFCFEFF),
    surfaceVariant = Color(0xFFDDEAF0),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF172126),
    onSurface = Color(0xFF172126),
    onSurfaceVariant = Color(0xFF53666F),
    outline = Color(0xFF53666F).copy(alpha = 0.26f),
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 29.sp, lineHeight = 34.sp, letterSpacing = 0.sp),
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
fun RacingDailyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    CompositionLocalProvider(LocalPureRacingDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypography,
            content = content
        )
    }
}
