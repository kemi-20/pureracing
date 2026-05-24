package com.racingdaily.ui.screens.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.net.URI

@Composable
actual fun rememberOpenUrl(): (String) -> Unit {
    return remember { { url -> try { if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI(url)) } catch (_: Exception) {} } }
}
