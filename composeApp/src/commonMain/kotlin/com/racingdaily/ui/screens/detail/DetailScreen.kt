package com.racingdaily.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.racingdaily.data.model.ArticleDetail
import com.racingdaily.data.remote.ApiService
import com.racingdaily.platform.rememberShareLauncher
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassIconButton
import com.racingdaily.ui.components.ScreenHeader
import kotlinx.coroutines.delay

@Composable
@Suppress("UNUSED_PARAMETER")
fun DetailScreen(
    articleId: Int,
    initialTitle: String,
    initialUrl: String,
    onBack: () -> Unit,
    api: ApiService,
    pageVisible: Boolean
) {
    var article by remember(articleId) { mutableStateOf<ArticleDetail?>(null) }
    var loading by remember(articleId) { mutableStateOf(true) }
    var error by remember(articleId) { mutableStateOf<String?>(null) }
    var reloadKey by remember(articleId) { mutableIntStateOf(0) }
    val shareLauncher = rememberShareLauncher()
    val darkTheme = isSystemInDarkTheme()
    val articleBackground = if (darkTheme) Color(0xFF1C2732) else Color(0xFFEAF4F8)
    val title = article?.title?.ifBlank { initialTitle } ?: initialTitle.ifBlank { "News" }
    val shareUrl = "https://news.romielf.com/news.html?id=$articleId"

    LaunchedEffect(articleId, reloadKey) {
        loading = true
        error = null
        article = null
        delay(240)
        runCatching { api.getNewsDetail(articleId).details }
            .onSuccess { article = it }
            .onFailure { error = it.message ?: "Unable to load article" }
        loading = false
    }

    Column(Modifier.fillMaxSize().background(articleBackground)) {
        ScreenHeader(
            title = title,
            subtitle = article?.temotime?.ifBlank { "Article" } ?: "Article",
            navigationIcon = {
                GlassIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Back", onBack)
            },
            actions = {
                GlassIconButton(Icons.Rounded.Share, "Share", onClick = { shareLauncher.share("$title\n$shareUrl") })
            }
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                error != null -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    GlassButton({ reloadKey++ }) {
                        Icon(Icons.Rounded.Refresh, null)
                        Text("Retry")
                    }
                }
                article != null && pageVisible -> HtmlView(
                    articleId = articleId,
                    html = article?.htmlContent().orEmpty(),
                    darkTheme = darkTheme
                )
            }
        }
    }
}

@Composable
expect fun HtmlView(articleId: Int, html: String, darkTheme: Boolean)

internal fun buildArticleHtmlDocument(html: String, darkTheme: Boolean): String {
    val background = if (darkTheme) "#1C2732" else "#EAF4F8"
    val foreground = if (darkTheme) "#E6EDF3" else "#17212B"
    val mediaBackground = if (darkTheme) "#161B22" else "#D4E4EC"
    val linkColor = if (darkTheme) "#79B8FF" else "#1769AA"
    val colorScheme = if (darkTheme) "dark" else "light"
    return """
<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
  <meta name="referrer" content="origin">
  <base href="https://news.romielf.com/">
  <style>
    html, body {
      margin: 0;
      padding: 0;
      min-height: 100%;
      width: 100%;
      overflow-x: hidden;
      background: $background !important;
      color: $foreground !important;
      color-scheme: $colorScheme;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Microsoft YaHei", sans-serif;
      font-size: 16px;
      line-height: 1.65;
    }
    *, *::before, *::after { box-sizing: border-box; }
    body { padding: 0 16px 28px; box-sizing: border-box; }
    div, p, span, section, article, h1, h2, h3, h4, h5, h6,
    li, strong, em, blockquote, table, th, td {
      color: $foreground !important;
      line-height: 1.65 !important;
      overflow-wrap: anywhere;
    }
    p { margin: 0 0 14px; }
    img, video, iframe {
      display: block;
      max-width: 100% !important;
      height: auto !important;
      margin: 12px auto;
      border-radius: 16px;
      background: $mediaBackground;
    }
    video { width: 100% !important; object-fit: contain; }
    table { width: 100% !important; table-layout: fixed; }
    pre, code { white-space: pre-wrap; overflow-wrap: anywhere; }
    a { color: $linkColor !important; }
  </style>
</head>
<body>
$html
<script>
(function () {
  function useOfficialVideoControls(video) {
    video.setAttribute("controls", "");
    video.preload = "metadata";
    video.setAttribute("playsinline", "");
    video.setAttribute("webkit-playsinline", "");
    var seeked = false;
    video.addEventListener("loadedmetadata", function () {
      if (seeked || video.readyState >= 2) return;
      seeked = true;
      try {
        video.currentTime = Math.min(0.08, Math.max(0, (video.duration || 1) - 0.01));
      } catch (ignored) {}
    }, { once: true });
    video.load();
  }

  function boot() {
    Array.prototype.forEach.call(document.querySelectorAll("video"), useOfficialVideoControls);
  }
  if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", boot);
  else boot();
})();
</script>
</body>
</html>
""".trimIndent()
}

private fun ArticleDetail.htmlContent(): String =
    content.ifBlank {
        if (conten.isBlank()) "" else "<p>${conten.escapeHtml()}</p>"
    }

private fun String.escapeHtml(): String = buildString(length) {
    this@escapeHtml.forEach { char ->
        when (char) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            '\'' -> append("&#39;")
            else -> append(char)
        }
    }
}
