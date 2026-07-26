package com.racingdaily.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.prefs.Preferences

@Composable
actual fun rememberThemeController(): ThemeController = remember {
    val preferences = Preferences.userRoot().node("com/racingdaily/pureracing")
    ThemeController(
        initialMode = preferences.get("theme_mode", null).toThemeMode(),
        persist = { mode -> preferences.put("theme_mode", mode.name) }
    )
}
