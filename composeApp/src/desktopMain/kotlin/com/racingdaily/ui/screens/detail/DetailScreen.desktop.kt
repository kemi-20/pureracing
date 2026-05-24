package com.racingdaily.ui.screens.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import javax.swing.SwingUtilities

@Composable
actual fun HtmlView(html: String) {
    val jfxPanel = remember { JFXPanel() }

    DisposableEffect(html) {
        SwingUtilities.invokeLater {
            Platform.runLater {
                val wv = WebView()
                wv.engine.loadContent(html)
                jfxPanel.scene = Scene(wv)
            }
        }
        onDispose { }
    }

    SwingPanel(factory = { jfxPanel }, modifier = Modifier.fillMaxSize())
}
