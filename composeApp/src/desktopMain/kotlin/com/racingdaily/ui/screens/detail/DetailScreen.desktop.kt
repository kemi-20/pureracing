package com.racingdaily.ui.screens.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import org.eclipse.swt.SWT
import org.eclipse.swt.awt.SWT_AWT
import org.eclipse.swt.browser.Browser
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import java.awt.Canvas
import javax.swing.JPanel
import kotlin.concurrent.thread

@Composable
actual fun HtmlView(url: String) {
    val swtReady = remember { mutableStateOf(false) }
    val canvas = remember { Canvas() }
    val panel = remember { JPanel(java.awt.BorderLayout()).apply { add(canvas, java.awt.BorderLayout.CENTER) } }

    DisposableEffect(url) {
        thread(isDaemon = true) {
            val display = Display()
            val shell = SWT_AWT.new_Shell(display, canvas)
            shell.layout = FillLayout()
            val browser = Browser(shell, SWT.EDGE)
            browser.setUrl(url)
            shell.setSize(panel.width.coerceAtLeast(1), panel.height.coerceAtLeast(1))
            swtReady.value = true

            while (!shell.isDisposed) {
                if (!display.readAndDispatch()) display.sleep()
            }
            display.dispose()
        }
        onDispose { }
    }

    SwingPanel(factory = { panel }, modifier = Modifier.fillMaxSize())
}
