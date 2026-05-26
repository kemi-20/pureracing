package com.racingdaily.ui.screens.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.racingdaily.data.remote.newsReferer
import org.eclipse.swt.SWT
import org.eclipse.swt.awt.SWT_AWT
import org.eclipse.swt.browser.Browser
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import java.awt.BorderLayout
import java.awt.Canvas
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.HierarchyEvent
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JPanel
import kotlin.concurrent.thread

@Composable
actual fun HtmlView(articleId: Int, html: String) {
    val document = remember(articleId, html) { buildArticleHtmlDocument(html) }
    val pageUrl = remember(articleId) { "${newsReferer}news.html?id=$articleId" }
    val canvas = remember { Canvas() }
    val panel = remember { JPanel(BorderLayout()).apply { add(canvas, BorderLayout.CENTER) } }
    val shellRef = remember { AtomicReference<Shell?>() }
    val browserRef = remember { AtomicReference<Browser?>() }

    DisposableEffect(articleId, document) {
        var initialized = false

        fun renderExistingBrowser() {
            browserRef.get()?.let { browser ->
                SwtThread.async {
                    if (!browser.isDisposed) {
                        browser.loadArticleDocument(pageUrl, document)
                    }
                }
            }
        }

        fun createBrowserIfReady() {
            if (!canvas.isDisplayable || initialized) return
            initialized = true
            SwtThread.async { display ->
                val shell = SWT_AWT.new_Shell(display, canvas)
                shellRef.set(shell)
                shell.layout = FillLayout()
                val browser = createBrowser(shell)
                browserRef.set(browser)
                browser.loadArticleDocument(pageUrl, document)
                shell.setSize(canvas.width.coerceAtLeast(1), canvas.height.coerceAtLeast(1))
                shell.open()
            }
        }

        val hierarchyListener = java.awt.event.HierarchyListener { event ->
            if ((event.changeFlags and HierarchyEvent.DISPLAYABILITY_CHANGED.toLong()) != 0L) {
                createBrowserIfReady()
            }
        }

        val resizeListener = object : ComponentAdapter() {
            override fun componentResized(event: ComponentEvent) {
                val shell = shellRef.get() ?: return
                SwtThread.async {
                    if (!shell.isDisposed) {
                        shell.setSize(canvas.width.coerceAtLeast(1), canvas.height.coerceAtLeast(1))
                    }
                }
            }
        }

        canvas.addHierarchyListener(hierarchyListener)
        canvas.addComponentListener(resizeListener)
        createBrowserIfReady()
        renderExistingBrowser()

        onDispose {
            canvas.removeHierarchyListener(hierarchyListener)
            canvas.removeComponentListener(resizeListener)
            shellRef.get()?.let { shell ->
                SwtThread.async {
                    if (!shell.isDisposed) shell.dispose()
                }
            }
            shellRef.set(null)
            browserRef.set(null)
        }
    }

    SwingPanel(factory = { panel }, modifier = Modifier.fillMaxSize())
}

private object SwtThread {
    private val ready = CountDownLatch(1)

    @Volatile
    private var display: Display? = null

    init {
        thread(name = "PureRacing-SWT", isDaemon = true) {
            display = Display()
            ready.countDown()
            val current = display ?: return@thread
            while (!current.isDisposed) {
                if (!current.readAndDispatch()) current.sleep()
            }
        }
    }

    fun async(block: (Display) -> Unit) {
        ready.await()
        val current = display ?: return
        current.asyncExec {
            if (!current.isDisposed) block(current)
        }
    }
}

private fun createBrowser(shell: Shell): Browser =
    runCatching { Browser(shell, SWT.EDGE) }.getOrElse { Browser(shell, SWT.NONE) }

private fun Browser.loadArticleDocument(pageUrl: String, document: String) {
    val script = "document.open();document.write(${document.toJavaScriptString()});document.close();"
    var injected = false

    fun inject() {
        if (injected || isDisposed) return
        injected = true
        if (!execute(script)) {
            setText(document)
        }
    }

    addProgressListener(object : org.eclipse.swt.browser.ProgressListener {
        override fun completed(event: org.eclipse.swt.browser.ProgressEvent) {
            inject()
        }

        override fun changed(event: org.eclipse.swt.browser.ProgressEvent) = Unit
    })
    setUrl(pageUrl)
    display.timerExec(2500) {
        inject()
    }
}

private fun String.toJavaScriptString(): String = buildString(length + 16) {
    append('"')
    this@toJavaScriptString.forEach { char ->
        when (char) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            '\b' -> append("\\b")
            else -> {
                val code = char.code
                if (code < 0x20) append("\\u").append(code.toString(16).padStart(4, '0')) else append(char)
            }
        }
    }
    append('"')
}
