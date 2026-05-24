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
                        var h = document.querySelector('header');
                        if (h) h.style.display = 'none';
                        document.body.style.backgroundColor = '#0a0e14';
                        document.body.style.color = '#e6edf3';
                        var imgs = document.querySelectorAll('img');
                        imgs.forEach(function(img) { img.style.maxWidth = '100%'; img.style.height = 'auto'; });
                        var vids = document.querySelectorAll('video');
                        vids.forEach(function(v) { v.style.maxWidth = '100%'; v.style.height = 'auto'; });
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
