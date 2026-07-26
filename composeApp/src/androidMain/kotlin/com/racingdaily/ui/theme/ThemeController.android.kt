package com.racingdaily.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberThemeController(): ThemeController {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val preferences = context.getSharedPreferences("pureracing_preferences", 0)
        ThemeController(
            initialMode = preferences.getString("theme_mode", null).toThemeMode(),
            persist = { mode -> preferences.edit().putString("theme_mode", mode.name).apply() }
        )
    }
}
