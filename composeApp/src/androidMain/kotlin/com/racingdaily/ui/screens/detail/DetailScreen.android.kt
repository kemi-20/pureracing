package com.racingdaily.ui.screens.detail

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun HtmlView(url: String) {
    AndroidView(
        factory = { ctx -> WebView(ctx).apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    view.evaluateJavascript("""
                        (function() {
                            if (document.getElementById('injected-dark-mode')) return;
                            var style = document.createElement('style');
                            style.id = 'injected-dark-mode';
                            style.innerHTML = `
                                header, .ad-header { display: none !important; }
                                body { background-color: #000000 !important; color: #E0E0E0 !important; }
                                img, video { max-width: 100% !important; height: auto !important; border-radius: 12px !important; }
                                a { color: #82B1FF !important; }
                            `;
                            document.head.appendChild(style);
                        })();
                    """.trimIndent(), null)
                }
            }
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            loadUrl(url)
        } },
        modifier = Modifier.fillMaxSize()
    )
}
