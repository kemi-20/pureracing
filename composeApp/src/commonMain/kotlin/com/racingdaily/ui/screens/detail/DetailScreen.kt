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
    img, video, iframe {
      display: block;
      max-width: 100% !important;
      height: auto !important;
      margin: 12px auto;
      border-radius: 16px;
      background: #161B22;
    }
    video { width: 100% !important; }
    .pr-video-shell {
      position: relative;
      display: block;
      width: 100%;
      max-width: 100%;
      margin: 12px auto;
      overflow: hidden;
      border-radius: 18px;
      background: #05070A;
      box-shadow: 0 12px 36px rgba(0, 0, 0, 0.28);
      -webkit-tap-highlight-color: transparent;
    }
    .pr-video-shell video {
      display: block;
      width: 100% !important;
      margin: 0 !important;
      border-radius: 0;
      background: #05070A;
    }
    .pr-video-shell video::-webkit-media-controls {
      display: none !important;
    }
    .pr-video-play {
      position: absolute;
      left: 50%;
      top: 50%;
      width: 58px;
      height: 58px;
      transform: translate(-50%, -50%);
      border: 0;
      border-radius: 999px;
      background: rgba(18, 18, 20, 0.58);
      backdrop-filter: blur(18px) saturate(1.45);
      -webkit-backdrop-filter: blur(18px) saturate(1.45);
      box-shadow: inset 0 0 0 1px rgba(255,255,255,0.24), 0 10px 28px rgba(0,0,0,0.32);
      transition: opacity 160ms ease, transform 160ms ease;
    }
    .pr-video-play::before {
      content: "";
      position: absolute;
      left: 23px;
      top: 18px;
      border-left: 17px solid white;
      border-top: 11px solid transparent;
      border-bottom: 11px solid transparent;
    }
    .pr-video-shell.is-playing .pr-video-play {
      opacity: 0;
      pointer-events: none;
      transform: translate(-50%, -50%) scale(0.92);
    }
    .pr-video-controls {
      position: absolute;
      left: 10px;
      right: 10px;
      bottom: 10px;
      display: flex;
      align-items: center;
      gap: 10px;
      min-height: 40px;
      padding: 7px 10px;
      box-sizing: border-box;
      border-radius: 999px;
      color: white;
      background: rgba(20, 20, 22, 0.58);
      backdrop-filter: blur(20px) saturate(1.55);
      -webkit-backdrop-filter: blur(20px) saturate(1.55);
      box-shadow: inset 0 0 0 1px rgba(255,255,255,0.18), 0 8px 24px rgba(0,0,0,0.24);
      opacity: 0;
      transform: translateY(8px);
      transition: opacity 160ms ease, transform 160ms ease;
    }
    .pr-video-shell.show-controls .pr-video-controls,
    .pr-video-shell:not(.is-playing) .pr-video-controls {
      opacity: 1;
      transform: translateY(0);
    }
    .pr-video-control-button {
      width: 26px;
      height: 26px;
      flex: 0 0 26px;
      padding: 0;
      border: 0;
      border-radius: 999px;
      color: white;
      background: rgba(255,255,255,0.12);
      font-size: 13px;
      line-height: 26px;
      text-align: center;
    }
    .pr-video-time {
      min-width: 42px;
      color: rgba(255,255,255,0.88);
      font-size: 11px;
      font-variant-numeric: tabular-nums;
      text-align: center;
    }
    .pr-video-progress {
      flex: 1;
      min-width: 42px;
      accent-color: white;
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
    video.preload = "auto";
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

  function installPlayer(video) {
    if (video.dataset.pureRacingPlayer === "1") return;
    video.dataset.pureRacingPlayer = "1";
    video.removeAttribute("controls");

    var shell = document.createElement("div");
    shell.className = "pr-video-shell show-controls";
    video.parentNode.insertBefore(shell, video);
    shell.appendChild(video);

    var bigPlay = document.createElement("button");
    bigPlay.className = "pr-video-play";
    bigPlay.type = "button";
    bigPlay.setAttribute("aria-label", "Play");

    var controls = document.createElement("div");
    controls.className = "pr-video-controls";

    var play = document.createElement("button");
    play.className = "pr-video-control-button";
    play.type = "button";
    play.textContent = "▶";

    var current = document.createElement("span");
    current.className = "pr-video-time";
    current.textContent = "0:00";

    var progress = document.createElement("input");
    progress.className = "pr-video-progress";
    progress.type = "range";
    progress.min = "0";
    progress.max = "1000";
    progress.value = "0";

    var duration = document.createElement("span");
    duration.className = "pr-video-time";
    duration.textContent = "0:00";

    var fullscreen = document.createElement("button");
    fullscreen.className = "pr-video-control-button";
    fullscreen.type = "button";
    fullscreen.textContent = "⛶";

    controls.appendChild(play);
    controls.appendChild(current);
    controls.appendChild(progress);
    controls.appendChild(duration);
    controls.appendChild(fullscreen);
    shell.appendChild(bigPlay);
    shell.appendChild(controls);

    var hideTimer = 0;
    function showControls() {
      shell.classList.add("show-controls");
      clearTimeout(hideTimer);
      if (!video.paused) {
        hideTimer = setTimeout(function () { shell.classList.remove("show-controls"); }, 1800);
      }
    }
    function togglePlay() {
      if (video.paused) video.play(); else video.pause();
      showControls();
    }
    function update() {
      shell.classList.toggle("is-playing", !video.paused);
      play.textContent = video.paused ? "▶" : "Ⅱ";
      current.textContent = formatTime(video.currentTime);
      duration.textContent = formatTime(video.duration);
      if (isFinite(video.duration) && video.duration > 0 && !progress.matches(":active")) {
        progress.value = String(Math.round((video.currentTime / video.duration) * 1000));
      }
    }

    bigPlay.addEventListener("click", togglePlay);
    play.addEventListener("click", togglePlay);
    video.addEventListener("click", function () { if (video.paused) togglePlay(); else showControls(); });
    shell.addEventListener("mousemove", showControls);
    shell.addEventListener("touchstart", showControls, { passive: true });
    progress.addEventListener("input", function () {
      if (isFinite(video.duration) && video.duration > 0) {
        video.currentTime = (Number(progress.value) / 1000) * video.duration;
      }
    });
    fullscreen.addEventListener("click", function () {
      var target = shell.requestFullscreen ? shell : video;
      if (target.requestFullscreen) target.requestFullscreen();
      else if (video.webkitEnterFullscreen) video.webkitEnterFullscreen();
    });
    ["loadedmetadata", "loadeddata", "timeupdate", "play", "pause", "ended", "durationchange"].forEach(function (name) {
      video.addEventListener(name, update);
    });
    video.addEventListener("play", showControls);
    video.addEventListener("pause", showControls);

    primeFirstFrame(video);
    update();
  }

  function boot() {
    Array.prototype.forEach.call(document.querySelectorAll("video"), installPlayer);
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
