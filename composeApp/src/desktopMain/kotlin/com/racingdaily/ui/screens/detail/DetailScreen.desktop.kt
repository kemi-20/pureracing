package com.racingdaily.ui.screens.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import javax.swing.SwingUtilities

@Composable
actual fun HtmlView(url: String) {
    val jfxPanel = remember { JFXPanel() }

    DisposableEffect(url) {
        SwingUtilities.invokeLater {
            Platform.runLater {
                val wv = WebView()
                wv.engine.load(url)
                jfxPanel.scene = Scene(wv)
            }
        }
        onDispose { }
    }

    SwingPanel(factory = { jfxPanel }, modifier = Modifier.fillMaxSize())
}
