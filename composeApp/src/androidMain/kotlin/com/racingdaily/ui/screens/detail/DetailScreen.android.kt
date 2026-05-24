package com.racingdaily.ui.screens.detail

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun HtmlView(html: String) {
    AndroidView(
        factory = { ctx -> WebView(ctx).apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            loadDataWithBaseURL("https://news.romielf.com/", html, "text/html", "UTF-8", null)
        } },
        modifier = Modifier.fillMaxSize()
    )
}
