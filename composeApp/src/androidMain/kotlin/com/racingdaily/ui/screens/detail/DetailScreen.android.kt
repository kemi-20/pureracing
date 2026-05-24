package com.racingdaily.ui.screens.detail

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.racingdaily.data.remote.newsReferer

@Composable
actual fun HtmlView(articleId: Int, html: String) {
    val document = remember(articleId, html) { buildArticleHtmlDocument(html) }
    val baseUrl = remember(articleId) { "${newsReferer}news.html?id=$articleId" }
    AndroidView(
        factory = { ctx -> WebView(ctx).apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
        } },
        update = { webView ->
            webView.loadDataWithBaseURL(baseUrl, document, "text/html", "UTF-8", null)
        },
        modifier = Modifier.fillMaxSize()
    )
}
