package com.racingdaily.ui.screens.detail

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun VideoPlayer(url: String) {
    val html = "<html><body style='margin:0;background:#000'><video src='$url' controls autoplay width='100%' height='100%'></video></body></html>"
    AndroidView(
        factory = { ctx -> WebView(ctx).apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        }},
        modifier = Modifier.fillMaxWidth().height(200.dp)
    )
}
