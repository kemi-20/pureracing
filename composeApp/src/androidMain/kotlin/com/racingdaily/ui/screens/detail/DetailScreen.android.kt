package com.racingdaily.ui.screens.detail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
    var fullscreenController by remember(articleId) { mutableStateOf<FullscreenVideoController?>(null) }
    var fullscreenVisible by remember(articleId) { mutableStateOf(false) }
    BackHandler(enabled = fullscreenVisible) {
        fullscreenController?.hide()
    }
    key(articleId, document) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    val activity = ctx.findActivity()
                    val videoController = FullscreenVideoController(activity) { showing ->
                        fullscreenVisible = showing
                        if (!showing) fullscreenController = null
                    }
                    fullscreenController = videoController
                    tag = videoController
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

                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            return request.isForMainFrame && view.context.openExternalUrl(request.url)
                        }

                        @Suppress("DEPRECATION")
                        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                            return view.context.openExternalUrl(Uri.parse(url))
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun getDefaultVideoPoster(): Bitmap =
                            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

                        override fun onShowCustomView(view: View, callback: WebChromeClient.CustomViewCallback) {
                            videoController.show(view, callback)
                            fullscreenController = videoController
                        }

                        override fun onHideCustomView() {
                            videoController.hide()
                        }
                    }
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadsImagesAutomatically = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    settings.setSupportMultipleWindows(false)
                    isHapticFeedbackEnabled = false
                    setOnLongClickListener { true }
                    loadDataWithBaseURL(baseUrl, document, "text/html", "UTF-8", null)
                }
            },
            onRelease = { webView ->
                val controller = webView.tag as? FullscreenVideoController
                controller?.hide()
                if (fullscreenController === controller) fullscreenController = null
                fullscreenVisible = false
                webView.tag = null
                webView.stopLoading()
                webView.webChromeClient = null
                webView.webViewClient = WebViewClient()
                webView.destroy()
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private class FullscreenVideoController(
    private val activity: Activity?,
    private val onVisibilityChanged: (Boolean) -> Unit
) {
    private var videoView: View? = null
    private var callback: WebChromeClient.CustomViewCallback? = null

    fun show(view: View, customViewCallback: WebChromeClient.CustomViewCallback) {
        val host = activity ?: return customViewCallback.onCustomViewHidden()
        if (videoView != null) {
            customViewCallback.onCustomViewHidden()
            return
        }
        videoView = view
        callback = customViewCallback
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
        onVisibilityChanged(true)
    }

    fun hide() {
        val host = activity
        val view = videoView
        if (host != null && view != null) {
            (view.parent as? ViewGroup)?.removeView(view)
        }
        videoView = null
        callback?.onCustomViewHidden()
        callback = null
        host?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        onVisibilityChanged(false)
    }
}

private fun Context.openExternalUrl(uri: Uri): Boolean {
    if (uri.scheme != "http" && uri.scheme != "https") return true
    return runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    }.getOrDefault(true)
}

private tailrec fun android.content.Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}
