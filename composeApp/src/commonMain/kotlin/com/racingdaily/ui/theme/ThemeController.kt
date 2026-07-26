package com.racingdaily.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class ThemeController internal constructor(
    initialMode: ThemeMode,
    private val persist: (ThemeMode) -> Unit
) {
    var mode by mutableStateOf(initialMode)
        private set

    fun select(mode: ThemeMode) {
        if (this.mode == mode) return
        this.mode = mode
        persist(mode)
    }
}

@Composable
expect fun rememberThemeController(): ThemeController

internal fun String?.toThemeMode(): ThemeMode =
    ThemeMode.entries.firstOrNull { it.name == this } ?: ThemeMode.SYSTEM
