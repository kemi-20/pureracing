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
import java.awt.Frame
import javax.swing.JPanel
import kotlin.concurrent.thread

@Composable
actual fun HtmlView(html: String) {
    val panel = remember { JPanel(java.awt.BorderLayout()) }
    val swtReady = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        thread(isDaemon = true) {
            val display = Display.getDefault()
            display.syncExec {
                val shell = Shell(display, SWT.NO_TRIM)
                shell.layout = FillLayout()
                val browser = Browser(shell, SWT.EDGE)
                browser.text = html
                val frame = SWT_AWT.new_Frame(shell)
                panel.add(frame, java.awt.BorderLayout.CENTER)
                panel.revalidate()
                shell.setSize(panel.width, panel.height)
                swtReady.value = true
            }
            while (!display.isDisposed) {
                if (!display.readAndDispatch()) display.sleep()
            }
        }
        onDispose { }
    }

    SwingPanel(factory = { panel }, modifier = Modifier.fillMaxSize())
}
