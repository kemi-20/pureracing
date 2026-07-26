package com.racingdaily.ui.screens.detail

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.racingdaily.data.remote.newsReferer

@Composable
actual fun HtmlView(
    articleId: Int,
    html: String,
    darkTheme: Boolean,
    playerScript: String,
    playerTemplate: String,
    onContentReady: () -> Unit
) {
    val document = remember(articleId, html, darkTheme, playerScript, playerTemplate) {
        buildArticleHtmlDocument(html, darkTheme, playerScript, playerTemplate)
    }
    val baseUrl = remember(articleId) { "${newsReferer}news.html?id=$articleId" }
    key(articleId, document) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    var customVideoView: View? = null
                    var customVideoCallback: WebChromeClient.CustomViewCallback? = null
                    val activity = ctx.findActivity()
                    setBackgroundColor(
                        if (darkTheme) Color.rgb(0x1B, 0x20, 0x24)
                        else Color.rgb(0xF1, 0xF7, 0xF9)
                    )
                    alpha = 0f
                    webViewClient = object : WebViewClient() {
                        private var revealed = false

                        private fun revealRenderedPage(view: WebView) {
                            if (revealed) return
                            revealed = true
                            val reveal = Runnable {
                                view.animate()
                                    .alpha(1f)
                                    .setDuration(140L)
                                    .withEndAction { onContentReady() }
                                    .start()
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                view.postVisualStateCallback(
                                    0L,
                                    object : WebView.VisualStateCallback() {
                                        override fun onComplete(requestId: Long) {
                                            view.post(reveal)
                                        }
                                    }
                                )
                            } else {
                                view.post(reveal)
                            }
                        }

                        override fun onPageCommitVisible(view: WebView, url: String?) {
                            revealRenderedPage(view)
                        }

                        override fun onPageFinished(view: WebView, url: String?) {
                            revealRenderedPage(view)
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun getDefaultVideoPoster(): Bitmap =
                            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

                        override fun onShowCustomView(view: View, callback: WebChromeClient.CustomViewCallback) {
                            val host = activity ?: return callback.onCustomViewHidden()
                            if (customVideoView != null) {
                                callback.onCustomViewHidden()
                                return
                            }
                            customVideoView = view
                            customVideoCallback = callback
                            host.window.decorView.systemUiVisibility =
                                View.SYSTEM_UI_FLAG_FULLSCREEN or
                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            (host.window.decorView as ViewGroup).addView(
                                view,
                                ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            )
                        }

                        override fun onHideCustomView() {
                            val host = activity ?: return
                            customVideoView?.let { (host.window.decorView as ViewGroup).removeView(it) }
                            customVideoView = null
                            customVideoCallback?.onCustomViewHidden()
                            customVideoCallback = null
                            host.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                        }
                    }
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadsImagesAutomatically = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    loadDataWithBaseURL(baseUrl, document, "text/html", "UTF-8", null)
                }
            },
            onRelease = { webView ->
                webView.stopLoading()
                webView.webChromeClient = null
                webView.webViewClient = WebViewClient()
                webView.destroy()
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private tailrec fun android.content.Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}
