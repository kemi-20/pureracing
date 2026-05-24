package com.racingdaily.ui.screens.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import org.eclipse.swt.SWT
import org.eclipse.swt.awt.SWT_AWT
import org.eclipse.swt.browser.Browser
import org.eclipse.swt.browser.ProgressListener
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import java.awt.Canvas
import javax.swing.JPanel
import kotlin.concurrent.thread

@Composable
actual fun HtmlView(url: String) {
    val canvas = remember { Canvas() }
    val panel = remember { JPanel(java.awt.BorderLayout()).apply { add(canvas, java.awt.BorderLayout.CENTER) } }
    val shellRef = remember { mutableStateOf<Shell?>(null) }

    DisposableEffect(url) {
        // Dispose old shell
        shellRef.value?.let { oldShell ->
            oldShell.display.asyncExec { if (!oldShell.isDisposed) oldShell.dispose() }
        }

        var disposed = false
        thread(isDaemon = true) {
            val display = Display()
            val shell = SWT_AWT.new_Shell(display, canvas)
            shellRef.value = shell
            shell.layout = FillLayout()
            val browser = Browser(shell, SWT.EDGE)
            browser.addProgressListener(object : ProgressListener {
                override fun completed(event: org.eclipse.swt.browser.ProgressEvent) {
                    display.timerExec(1500) {
                        if (!shell.isDisposed) {
                            browser.execute("""
                                var h = document.querySelector('header');
                                if (h) h.style.display = 'none';
                                document.body.style.backgroundColor = '#0a0e14';
                                document.body.style.color = '#e6edf3';
                            """.trimIndent())
                        }
                    }
                }
                override fun changed(event: org.eclipse.swt.browser.ProgressEvent) {}
            })
            browser.setUrl(url)
            shell.setSize(panel.width.coerceAtLeast(1), panel.height.coerceAtLeast(1))

            while (!shell.isDisposed && !disposed) {
                if (!display.readAndDispatch()) display.sleep()
            }
            if (!display.isDisposed) display.dispose()
        }

        onDispose {
            disposed = true
            shellRef.value?.let { shell ->
                shell.display.asyncExec { if (!shell.isDisposed) shell.dispose() }
            }
        }
    }

    SwingPanel(factory = { panel }, modifier = Modifier.fillMaxSize())
}
