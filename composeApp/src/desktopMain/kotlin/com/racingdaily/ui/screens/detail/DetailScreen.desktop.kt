package com.racingdaily.ui.screens.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import javax.swing.JEditorPane
import javax.swing.JScrollPane
import javax.swing.text.html.HTMLEditorKit

@Composable
actual fun HtmlView(html: String) {
    SwingPanel(
        factory = {
            JScrollPane(JEditorPane().apply {
                isEditable = false
                contentType = "text/html"
                editorKit = HTMLEditorKit()
                text = html
                addHyperlinkListener { event ->
                    if (event.eventType == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                        try { java.awt.Desktop.getDesktop().browse(event.url.toURI()) } catch (_: Exception) {}
                    }
                }
            })
        },
        modifier = Modifier.fillMaxSize()
    )
}
