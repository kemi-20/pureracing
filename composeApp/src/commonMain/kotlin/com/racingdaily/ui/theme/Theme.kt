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
    primary = Color(0xFFFF5A52),
    secondary = Color(0xFF45A3FF),
    tertiary = RacingYellow,
    background = Color(0xFF20272B),
    surface = Color(0xFF2C373D),
    surfaceVariant = Color(0xFF3A484F),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF6F8F9),
    onSurface = Color(0xFFF6F8F9),
    onSurfaceVariant = Color(0xFFC8D2D7),
    outline = Color.White.copy(alpha = 0.2f),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFFD91E2B),
    secondary = Color(0xFF0675E8),
    tertiary = Color(0xFF7D6200),
    background = Color(0xFFF4F7F8),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE5EDF1),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF172025),
    onSurface = Color(0xFF172025),
    onSurfaceVariant = Color(0xFF52636C),
    outline = Color(0xFF52636C).copy(alpha = 0.22f),
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 35.sp, letterSpacing = 0.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 29.sp, letterSpacing = 0.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 25.sp, letterSpacing = 0.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 21.sp, letterSpacing = 0.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp, letterSpacing = 0.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 17.sp, letterSpacing = 0.sp),
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
