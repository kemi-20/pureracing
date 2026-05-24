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
import java.awt.event.HierarchyEvent
import javax.swing.JPanel
import kotlin.concurrent.thread

@Composable
actual fun HtmlView(url: String) {
    val canvas = remember { Canvas() }
    val panel = remember { JPanel(java.awt.BorderLayout()).apply { add(canvas, java.awt.BorderLayout.CENTER) } }
    val shellRef = remember { mutableStateOf<Shell?>(null) }

    DisposableEffect(url) {
        var initialized = false
        canvas.addHierarchyListener { e ->
            if ((e.changeFlags and HierarchyEvent.DISPLAYABILITY_CHANGED.toLong()) != 0L) {
                if (canvas.isDisplayable && !initialized) {
                    initialized = true
                    
                    val display = Display.getDefault()
                    if (display == null) {
                        // First time: Create new display and run event loop in a daemon thread
                        thread(isDaemon = true) {
                            val newDisplay = Display()
                            newDisplay.syncExec {
                                createShellAndBrowser(newDisplay, canvas, url, shellRef)
                            }
                            while (!newDisplay.isDisposed) {
                                try {
                                    if (!newDisplay.readAndDispatch()) newDisplay.sleep()
                                } catch (ex: Exception) { ex.printStackTrace() }
                            }
                        }
                    } else {
                        // Subsequent times: Re-use existing display thread
                        display.asyncExec {
                            createShellAndBrowser(display, canvas, url, shellRef)
                        }
                    }
                }
            }
        }

        onDispose {
            shellRef.value?.let { shell ->
                if (!shell.isDisposed) {
                    shell.display.asyncExec { if (!shell.isDisposed) shell.dispose() }
                }
            }
        }
    }

    SwingPanel(factory = { panel }, modifier = Modifier.fillMaxSize())
}

private fun createShellAndBrowser(display: Display, canvas: Canvas, url: String, shellRef: MutableState<Shell?>) {
    try {
        val shell = SWT_AWT.new_Shell(display, canvas)
        shellRef.value = shell
        shell.layout = FillLayout()
        
        canvas.addComponentListener(object : java.awt.event.ComponentAdapter() {
            override fun componentResized(e: java.awt.event.ComponentEvent) {
                display.asyncExec {
                    if (!shell.isDisposed) {
                        shell.setSize(canvas.width.coerceAtLeast(1), canvas.height.coerceAtLeast(1))
                    }
                }
            }
        })
        
        val browser = Browser(shell, SWT.EDGE)
        browser.addProgressListener(object : ProgressListener {
            override fun completed(event: org.eclipse.swt.browser.ProgressEvent) {
                display.timerExec(500) {
                    if (!shell.isDisposed) {
                        browser.execute("""
                            (function() {
                                if (document.getElementById('injected-dark-mode')) return;
                                var style = document.createElement('style');
                                style.id = 'injected-dark-mode';
                                style.innerHTML = `
                                    header, .ad-header, .navbar { display: none !important; }
                                    body { background-color: #000000 !important; color: #E0E0E0 !important; }
                                    img, video { max-width: 100% !important; height: auto !important; border-radius: 12px !important; }
                                    a { color: #82B1FF !important; }
                                `;
                                document.head.appendChild(style);
                            })();
                        """.trimIndent())
                    }
                }
            }
            override fun changed(event: org.eclipse.swt.browser.ProgressEvent) {}
        })
        
        browser.setUrl(url)
        shell.setSize(canvas.width.coerceAtLeast(1), canvas.height.coerceAtLeast(1))
        shell.open()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
