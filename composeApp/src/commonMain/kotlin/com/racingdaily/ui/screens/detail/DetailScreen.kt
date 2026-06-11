package com.racingdaily.ui.screens.detail

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
fun DetailScreen(articleId: Int, initialTitle: String, initialUrl: String, onBack: () -> Unit, api: ApiService) {
    var article by remember(articleId) { mutableStateOf<ArticleDetail?>(null) }
    var loading by remember(articleId) { mutableStateOf(true) }
    var error by remember(articleId) { mutableStateOf<String?>(null) }
    var reloadKey by remember(articleId) { mutableIntStateOf(0) }
    val shareLauncher = rememberShareLauncher()
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

    Column(Modifier.fillMaxSize()) {
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
                        Icon(Icons.Rounded.Refresh, null, tint = Color.White)
                        Text("Retry", color = Color.White)
                    }
                }
                article != null -> HtmlView(articleId, article?.htmlContent().orEmpty())
            }
        }
    }
}

@Composable
expect fun HtmlView(articleId: Int, html: String)

private const val OfficialArticlePlayIcon =
    "data:image/png;base64,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"

internal fun buildArticleHtmlDocument(html: String): String = """
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
      background: transparent;
      color: #E6EDF3;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Microsoft YaHei", sans-serif;
      font-size: 16px;
      line-height: 1.65;
    }
    body { padding: 0 16px 28px; box-sizing: border-box; }
    div, p, span, section, article { color: #E6EDF3 !important; line-height: 1.65 !important; }
    p { margin: 0 0 14px; }
    img, iframe {
      display: block;
      max-width: 100% !important;
      height: auto !important;
      margin: 12px auto;
      border-radius: 16px;
      background: #161B22;
    }
    video {
      display: block;
      width: 100% !important;
      max-width: 100% !important;
      height: auto !important;
      margin: 12px auto;
      border-radius: 16px;
      background: #05070A;
      object-fit: contain;
    }
    .rd-video-shell {
      position: relative;
      width: 100%;
      max-width: 100%;
      margin: 12px auto;
      overflow: hidden;
      border-radius: 16px;
      background: #05070A;
      -webkit-tap-highlight-color: transparent;
    }
    .rd-video-shell video {
      width: 100% !important;
      margin: 0 !important;
      border-radius: 0;
    }
    .rd-video-shell video::-webkit-media-controls,
    .rd-video-shell video::-webkit-media-controls-enclosure {
      display: none !important;
    }
    .rd-video-primary {
      position: absolute;
      left: 50%;
      top: 50%;
      width: 64px;
      height: 63px;
      padding: 0;
      border: 0;
      outline: 0;
      border-radius: 999px;
      background: transparent url("$OfficialArticlePlayIcon") center / contain no-repeat;
      transform: translate(-50%, -50%);
      opacity: 1;
      transition: opacity 160ms ease, transform 160ms ease;
    }
    .rd-video-shell.is-playing .rd-video-primary {
      opacity: 0;
      pointer-events: none;
      transform: translate(-50%, -50%) scale(0.92);
    }
    .rd-video-controls {
      position: absolute;
      left: 10px;
      right: 10px;
      bottom: 10px;
      display: flex;
      align-items: center;
      gap: 8px;
      min-height: 38px;
      padding: 6px 10px;
      box-sizing: border-box;
      color: #FFFFFF;
      border-radius: 999px;
      background: rgba(0, 0, 0, 0.58);
      backdrop-filter: blur(18px) saturate(1.3);
      -webkit-backdrop-filter: blur(18px) saturate(1.3);
      box-shadow: inset 0 0 0 1px rgba(255,255,255,0.14);
      opacity: 0;
      transform: translateY(8px);
      transition: opacity 160ms ease, transform 160ms ease;
    }
    .rd-video-shell.show-controls .rd-video-controls,
    .rd-video-shell:not(.is-playing) .rd-video-controls {
      opacity: 1;
      transform: translateY(0);
    }
    .rd-video-button {
      flex: 0 0 28px;
      width: 28px;
      height: 28px;
      padding: 0;
      border: 0;
      border-radius: 999px;
      color: white;
      background: rgba(255,255,255,0.13);
      font-size: 13px;
      line-height: 28px;
      text-align: center;
    }
    .rd-video-progress {
      flex: 1 1 auto;
      min-width: 44px;
      height: 3px;
      accent-color: #FFFFFF;
    }
    .rd-video-time {
      flex: 0 0 auto;
      min-width: 34px;
      color: rgba(255,255,255,0.86);
      font-size: 11px;
      font-variant-numeric: tabular-nums;
      text-align: center;
    }
    a { color: #58A6FF !important; }
  </style>
</head>
<body>
$html
<script>
(function () {
  function formatTime(seconds) {
    if (!isFinite(seconds) || seconds < 0) return "0:00";
    var whole = Math.floor(seconds);
    var minutes = Math.floor(whole / 60);
    var secs = String(whole % 60);
    if (secs.length < 2) secs = "0" + secs;
    return minutes + ":" + secs;
  }

  function primeFirstFrame(video) {
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

  function installOfficialPlayer(video) {
    if (video.dataset.pureRacingPlayer === "1") return;
    video.dataset.pureRacingPlayer = "1";
    video.controls = false;
    video.removeAttribute("controls");
    primeFirstFrame(video);

    var shell = document.createElement("div");
    shell.className = "rd-video-shell";
    var parent = video.parentNode;
    parent.insertBefore(shell, video);
    shell.appendChild(video);

    var primary = document.createElement("button");
    primary.className = "rd-video-primary";
    primary.type = "button";
    primary.setAttribute("aria-label", "play");

    var controls = document.createElement("div");
    controls.className = "rd-video-controls";
    controls.innerHTML =
      '<button class="rd-video-button rd-video-toggle" type="button">▶</button>' +
      '<span class="rd-video-time rd-video-current">0:00</span>' +
      '<input class="rd-video-progress" type="range" min="0" max="1000" value="0" step="1">' +
      '<span class="rd-video-time rd-video-duration">0:00</span>' +
      '<button class="rd-video-button rd-video-fullscreen" type="button">⛶</button>';
    shell.appendChild(primary);
    shell.appendChild(controls);

    var toggle = controls.querySelector(".rd-video-toggle");
    var progress = controls.querySelector(".rd-video-progress");
    var current = controls.querySelector(".rd-video-current");
    var duration = controls.querySelector(".rd-video-duration");
    var fullscreen = controls.querySelector(".rd-video-fullscreen");
    var hideTimer = 0;

    function showControls() {
      shell.classList.add("show-controls");
      clearTimeout(hideTimer);
      if (!video.paused) {
        hideTimer = setTimeout(function () { shell.classList.remove("show-controls"); }, 1800);
      }
    }

    function update() {
      shell.classList.toggle("is-playing", !video.paused && !video.ended);
      toggle.textContent = video.paused || video.ended ? "▶" : "Ⅱ";
      current.textContent = formatTime(video.currentTime);
      duration.textContent = formatTime(video.duration);
      if (isFinite(video.duration) && video.duration > 0 && !progress.matches(":active")) {
        progress.value = String(Math.round((video.currentTime / video.duration) * 1000));
      }
    }

    function togglePlayback() {
      if (video.paused || video.ended) video.play();
      else video.pause();
      showControls();
    }

    primary.addEventListener("click", function (event) {
      event.stopPropagation();
      togglePlayback();
    });
    toggle.addEventListener("click", function (event) {
      event.stopPropagation();
      togglePlayback();
    });
    shell.addEventListener("click", showControls);
    video.addEventListener("click", togglePlayback);
    video.addEventListener("play", update);
    video.addEventListener("pause", update);
    video.addEventListener("ended", update);
    video.addEventListener("loadedmetadata", update);
    video.addEventListener("timeupdate", update);
    progress.addEventListener("input", function () {
      if (isFinite(video.duration) && video.duration > 0) {
        video.currentTime = (Number(progress.value) / 1000) * video.duration;
      }
      showControls();
    });
    fullscreen.addEventListener("click", function (event) {
      event.stopPropagation();
      var target = video;
      var request = target.requestFullscreen || target.webkitEnterFullscreen || target.webkitRequestFullscreen;
      if (request) request.call(target);
      showControls();
    });
    update();
  }

  function boot() {
    Array.prototype.forEach.call(document.querySelectorAll("video"), installOfficialPlayer);
  }
  if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", boot);
  else boot();
})();
</script>
</body>
</html>
""".trimIndent()

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
