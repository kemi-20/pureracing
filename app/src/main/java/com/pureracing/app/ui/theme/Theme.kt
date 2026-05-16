package com.pureracing.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE10600),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8B0000),
    secondary = Color(0xFFFFD700),
    background = Color(0xFF0D0D0D),
    surface = Color(0xFF1A1A1A),
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFE10600),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFB8860B),
    background = Color(0xFFF5F5F5),
    surface = Color.White,
)

@Composable
fun PureRacingTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
