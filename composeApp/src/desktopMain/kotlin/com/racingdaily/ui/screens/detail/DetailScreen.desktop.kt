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

    DisposableEffect(url) {
        thread(isDaemon = true) {
            val display = Display()
            val shell = SWT_AWT.new_Shell(display, canvas)
            shell.layout = FillLayout()
            val browser = Browser(shell, SWT.EDGE)
            browser.addProgressListener(object : ProgressListener {
                override fun completed(event: org.eclipse.swt.browser.ProgressEvent) {
                    browser.execute("""
                        var h = document.querySelector('header');
                        if (h) h.style.display = 'none';
                        document.body.style.backgroundColor = '#0a0e14';
                        document.body.style.color = '#e6edf3';
                        var imgs = document.querySelectorAll('img');
                        imgs.forEach(function(img) { img.style.maxWidth = '100%'; img.style.height = 'auto'; });
                        var vids = document.querySelectorAll('video');
                        vids.forEach(function(v) { v.style.maxWidth = '100%'; v.style.height = 'auto'; });
                    """.trimIndent())
                }
                override fun changed(event: org.eclipse.swt.browser.ProgressEvent) {}
            })
            browser.setUrl(url)
            shell.setSize(panel.width.coerceAtLeast(1), panel.height.coerceAtLeast(1))

            while (!shell.isDisposed) {
                if (!display.readAndDispatch()) display.sleep()
            }
            display.dispose()
        }
        onDispose { }
    }

    SwingPanel(factory = { panel }, modifier = Modifier.fillMaxSize())
}
