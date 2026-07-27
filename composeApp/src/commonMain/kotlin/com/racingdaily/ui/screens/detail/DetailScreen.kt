package com.racingdaily.ui.screens.detail

import androidx.compose.foundation.background
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.racingdaily.data.model.ArticleDetail
import com.racingdaily.data.model.ArticleComment
import com.racingdaily.data.model.CommentListData
import com.racingdaily.data.remote.ApiService
import com.racingdaily.platform.rememberShareLauncher
import com.racingdaily.resources.Res
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassIconButton
import com.racingdaily.ui.components.ScreenHeader
import com.racingdaily.ui.theme.LocalPureRacingDarkTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.jetbrains.compose.resources.ExperimentalResourceApi

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
    var comments by remember(articleId) { mutableStateOf<CommentListData?>(null) }
    var loading by remember(articleId) { mutableStateOf(true) }
    var error by remember(articleId) { mutableStateOf<String?>(null) }
    var reloadKey by remember(articleId) { mutableIntStateOf(0) }
    var contentReady by remember(articleId, reloadKey) { mutableStateOf(false) }
    val shareLauncher = rememberShareLauncher()
    val playerAssets = rememberArticlePlayerAssets()
    val darkTheme = LocalPureRacingDarkTheme.current
    val isChinese = Locale.current.language.startsWith("zh")
    val articleBackground = MaterialTheme.colorScheme.background
    val title = article?.title?.ifBlank { initialTitle } ?: initialTitle.ifBlank { "新闻" }
    val shareUrl = "https://news.romielf.com/news.html?id=$articleId"

    LaunchedEffect(articleId, reloadKey) {
        loading = true
        contentReady = false
        error = null
        article = null
        comments = null
        val (articleResult, commentsResult) = coroutineScope {
            val articleRequest = async { runCatching { api.getNewsDetail(articleId).details } }
            val commentsRequest = async { runCatching { api.getArticleCommentsWithReplies(articleId) } }
            articleRequest.await() to commentsRequest.await()
        }
        articleResult
            .onSuccess { article = it }
            .onFailure { error = it.message ?: "无法加载文章" }
        comments = commentsResult.getOrNull()
        loading = false
    }

    Column(Modifier.fillMaxSize().background(articleBackground)) {
        ScreenHeader(
            title = title,
            subtitle = article?.temotime?.ifBlank { "文章" } ?: "文章",
            marqueeTitle = true,
            navigationIcon = {
                GlassIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "返回", onBack)
            },
            actions = {
                GlassIconButton(Icons.Rounded.Share, "分享", onClick = { shareLauncher.share("$title\n$shareUrl") })
            }
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                error != null -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    GlassButton({ reloadKey++ }) {
                        Icon(Icons.Rounded.Refresh, null)
                        Text("重试")
                    }
                }
                article != null && pageVisible && playerAssets != null -> {
                    HtmlView(
                        articleId = articleId,
                        html = article?.htmlContent().orEmpty() + comments.toCommentsHtml(isChinese),
                        darkTheme = darkTheme,
                        playerScript = playerAssets.mediaChromeScript,
                        playerTemplate = playerAssets.cupertinoTemplate,
                        onContentReady = { contentReady = true }
                    )
                    if (!contentReady) {
                        CircularProgressIndicator(
                            Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                loading || (article != null && playerAssets == null) -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
expect fun HtmlView(
    articleId: Int,
    html: String,
    darkTheme: Boolean,
    playerScript: String,
    playerTemplate: String,
    onContentReady: () -> Unit
)

internal fun buildArticleHtmlDocument(
    html: String,
    darkTheme: Boolean,
    playerScript: String,
    playerTemplate: String
): String {
    val background = if (darkTheme) "#20272B" else "#F4F7F8"
    val foreground = if (darkTheme) "#F6F8F9" else "#172025"
    val secondaryForeground = if (darkTheme) "#C8D2D7" else "#52636C"
    val mediaBackground = if (darkTheme) "#171D20" else "#E5EDF1"
    val commentSurface = if (darkTheme) "rgba(255,255,255,.065)" else "rgba(255,255,255,.58)"
    val replySurface = if (darkTheme) "rgba(255,255,255,.055)" else "rgba(225,238,243,.76)"
    val hairline = if (darkTheme) "rgba(255,255,255,.11)" else "rgba(23,33,38,.10)"
    val linkColor = if (darkTheme) "#64B5FF" else "#0675E8"
    val accent = if (darkTheme) "#FF5A52" else "#D91E2B"
    val colorScheme = if (darkTheme) "dark" else "light"
    return """
<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
  <meta name="referrer" content="origin">
  <base href="https://news.romielf.com/">
  <script>${playerScript.escapeClosingScriptTag()}</script>
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
    span[style*="background"] {
      background: transparent !important;
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
    media-theme.pureracing-video-player {
      display: block;
      width: 100%;
      max-width: 100%;
      aspect-ratio: 16 / 9;
      margin: 12px auto;
      overflow: hidden;
      border-radius: 16px;
      background: $mediaBackground;
      line-height: 0;
      -webkit-touch-callout: none;
      -webkit-tap-highlight-color: transparent;
      -webkit-user-select: none;
      user-select: none;
    }
    media-theme.pureracing-video-player > video {
      width: 100% !important;
      height: 100% !important;
      margin: 0;
      border-radius: 0;
      background: $mediaBackground;
      object-fit: contain;
    }
    table { width: 100% !important; table-layout: fixed; }
    pre, code { white-space: pre-wrap; overflow-wrap: anywhere; }
    a { color: $linkColor !important; }
    .pureracing-comments {
      margin: 44px -2px 0;
      padding: 28px 0 8px;
      border-top: 1px solid $hairline;
    }
    .comments-heading {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      gap: 16px;
      margin-bottom: 20px;
    }
    .comments-title {
      margin: 0;
      color: $foreground !important;
      font-size: 24px;
      line-height: 1.2 !important;
      font-weight: 760;
    }
    .comments-count {
      flex: 0 0 auto;
      padding: 7px 11px;
      border: 1px solid $hairline;
      border-radius: 999px;
      color: $secondaryForeground !important;
      background: $commentSurface;
      font-size: 12px;
      line-height: 1 !important;
      font-weight: 650;
    }
    .comment-list { display: grid; gap: 12px; }
    .comment-item {
      display: grid;
      grid-template-columns: 42px minmax(0, 1fr);
      gap: 11px;
      padding: 15px;
      border: 1px solid $hairline;
      border-radius: 18px;
      background: $commentSurface;
    }
    .comment-item,
    .comment-reply {
      opacity: .62;
      transform: translateY(8px) scale(.995);
      transform-origin: center;
      will-change: opacity, transform;
    }
    .comment-avatar,
    .comment-avatar-fallback {
      width: 42px !important;
      height: 42px !important;
      min-width: 42px;
      margin: 0 !important;
      border: 1px solid $hairline;
      border-radius: 50% !important;
      object-fit: cover;
      background: $replySurface;
    }
    .comment-avatar-fallback {
      display: grid;
      place-items: center;
      color: $foreground !important;
      font-size: 15px;
      font-weight: 700;
    }
    .comment-main { min-width: 0; }
    .comment-author-row {
      display: flex;
      align-items: baseline;
      justify-content: space-between;
      gap: 10px;
      margin-bottom: 6px;
    }
    .comment-author {
      min-width: 0;
      color: $foreground !important;
      font-size: 14px;
      line-height: 1.25 !important;
      font-weight: 720;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .comment-time {
      flex: 0 0 auto;
      color: $secondaryForeground !important;
      font-size: 11px;
      line-height: 1.25 !important;
    }
    .comment-body {
      margin: 0;
      color: $foreground !important;
      font-size: 15px;
      line-height: 1.6 !important;
      white-space: pre-wrap;
    }
    .comment-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-top: 9px;
      color: $secondaryForeground !important;
      font-size: 11px;
      line-height: 1.2 !important;
    }
    .comment-meta span { color: $secondaryForeground !important; }
    .comment-images {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 7px;
      margin-top: 10px;
    }
    .comment-image {
      width: 100% !important;
      aspect-ratio: 4 / 3;
      height: auto !important;
      margin: 0 !important;
      border-radius: 12px !important;
      object-fit: cover;
    }
    .comment-replies {
      display: grid;
      gap: 9px;
      padding: 11px;
      border-radius: 14px;
      background: $replySurface;
    }
    .comment-reply {
      display: grid;
      grid-template-columns: 30px minmax(0, 1fr);
      gap: 9px;
    }
    .comment-reply + .comment-reply {
      padding-top: 9px;
      border-top: 1px solid $hairline;
    }
    .comment-reply .comment-avatar,
    .comment-reply .comment-avatar-fallback {
      width: 30px !important;
      height: 30px !important;
      min-width: 30px;
      font-size: 11px;
    }
    .comment-reply .comment-author-row { margin-bottom: 3px; }
    .comment-reply .comment-body { font-size: 13px; line-height: 1.5 !important; }
    .comment-replies-disclosure {
      margin-top: 11px;
    }
    .comment-replies-disclosure summary {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 5px 0;
      border: 0;
      color: $accent !important;
      background: transparent;
      cursor: pointer;
      font-size: 12px;
      line-height: 1.3 !important;
      font-weight: 680;
      list-style: none;
      -webkit-tap-highlight-color: transparent;
    }
    .comment-replies-disclosure summary::-webkit-details-marker { display: none; }
    .comment-replies-disclosure summary::after {
      content: "";
      width: 6px;
      height: 6px;
      border-right: 1.5px solid currentColor;
      border-bottom: 1.5px solid currentColor;
      transform: rotate(45deg) translateY(-2px);
      transform-origin: center;
      transition: transform 180ms ease;
    }
    .comment-replies-disclosure[open] summary::after {
      transform: rotate(225deg) translate(-1px, -1px);
    }
    .comment-replies-disclosure .comment-replies {
      margin-top: 6px;
    }
    .comments-empty {
      padding: 28px 18px;
      border: 1px solid $hairline;
      border-radius: 18px;
      color: $secondaryForeground !important;
      background: $commentSurface;
      text-align: center;
      font-size: 14px;
    }
    .comments-footnote {
      margin: 14px 0 0;
      color: $secondaryForeground !important;
      text-align: center;
      font-size: 11px;
      line-height: 1.4 !important;
    }
  </style>
</head>
<body>
<template id="pureracing-cupertino-theme">
$playerTemplate
</template>
$html
<script>
(function () {
  function useCupertinoPlayer(video) {
    if (video.closest("media-theme.pureracing-video-player")) return;

    video.removeAttribute("controls");
    video.preload = video.poster ? "metadata" : "auto";
    video.setAttribute("playsinline", "");
    video.setAttribute("webkit-playsinline", "");
    video.setAttribute("slot", "media");

    var player = document.createElement("media-theme");
    player.className = "pureracing-video-player";
    player.setAttribute("template", "pureracing-cupertino-theme");
    ["contextmenu", "selectstart", "dragstart"].forEach(function (eventName) {
      player.addEventListener(eventName, function (event) {
        event.preventDefault();
        event.stopPropagation();
      }, true);
    });
    video.parentNode.insertBefore(player, video);
    player.appendChild(video);

    function updateAspectRatio() {
      if (video.videoWidth > 0 && video.videoHeight > 0) {
        player.style.aspectRatio = video.videoWidth + " / " + video.videoHeight;
      }
    }

    var seeked = false;
    video.addEventListener("loadedmetadata", function () {
      updateAspectRatio();
      if (seeked || video.readyState >= 2) return;
      seeked = true;
      try {
        video.currentTime = Math.min(0.08, Math.max(0, (video.duration || 1) - 0.01));
      } catch (ignored) {}
    });
    video.addEventListener("loadeddata", function () {
      updateAspectRatio();
      player.classList.add("frame-ready");
    }, { once: true });
    video.load();
  }

  function boot() {
    Array.prototype.forEach.call(document.querySelectorAll("video"), useCupertinoPlayer);
    var animatedCards = document.querySelectorAll(".comment-item, .comment-reply");
    var omega = Math.sqrt(360);
    var frameCount = 30;
    var duration = 360;
    var initialOpacity = 0.62;
    var initialOffset = 8;
    var initialScale = 0.995;
    var revealFrames = [];
    for (var frame = 0; frame <= frameCount; frame++) {
      var offset = frame / frameCount;
      var elapsed = duration * offset / 1000;
      var progress = 1 - (1 + omega * elapsed) * Math.exp(-omega * elapsed);
      if (frame === frameCount) progress = 1;
      revealFrames.push({
        opacity: initialOpacity + (1 - initialOpacity) * progress,
        transform: "translateY(" + ((1 - progress) * initialOffset) + "px) scale(" + (initialScale + (1 - initialScale) * progress) + ")",
        offset: offset
      });
    }
    var revealObserver = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          if (entry.target.__pureracingReveal) entry.target.__pureracingReveal.cancel();
          entry.target.__pureracingReveal = entry.target.animate(revealFrames, {
            duration: duration,
            fill: "forwards"
          });
        } else {
          if (entry.target.__pureracingReveal) entry.target.__pureracingReveal.cancel();
          entry.target.style.opacity = String(initialOpacity);
          entry.target.style.transform = "translateY(" + initialOffset + "px) scale(" + initialScale + ")";
        }
      });
    }, { threshold: 0.04 });
    Array.prototype.forEach.call(animatedCards, function (card) {
      revealObserver.observe(card);
    });
  }
  if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", boot);
  else boot();
})();
</script>
</body>
</html>
""".trimIndent()
}

private data class ArticlePlayerAssets(
    val mediaChromeScript: String,
    val cupertinoTemplate: String
)

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun rememberArticlePlayerAssets(): ArticlePlayerAssets? {
    val assets by produceState<ArticlePlayerAssets?>(initialValue = null) {
        value = runCatching {
            ArticlePlayerAssets(
                mediaChromeScript = Res.readBytes(
                    "files/article-player/media-chrome-4.19.2.iife.js"
                ).decodeToString(),
                cupertinoTemplate = Res.readBytes(
                    "files/article-player/cupertino-liquid.html"
                ).decodeToString()
            )
        }.getOrNull()
    }
    return assets
}

private fun String.escapeClosingScriptTag(): String =
    replace("</script", "<\\/script", ignoreCase = true)

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

private fun CommentListData?.toCommentsHtml(isChinese: Boolean): String {
    val title = if (isChinese) "评论" else "Comments"
    if (this == null) {
        val unavailable = if (isChinese) "评论暂时无法加载" else "Comments are temporarily unavailable"
        return commentsSection(title, "-", "<div class=\"comments-empty\">${unavailable.escapeHtml()}</div>")
    }

    val body = if (comment_list.isEmpty()) {
        val empty = if (isChinese) "还没有评论" else "No comments yet"
        "<div class=\"comments-empty\">${empty.escapeHtml()}</div>"
    } else {
        "<div class=\"comment-list\">${comment_list.joinToString("") { it.toCommentHtml(isChinese) }}</div>"
    }
    val shown = comment_list.size + comment_list.sumOf { it.sub_list.size }
    val footnote = if (count > shown && shown > 0) {
        val label = if (isChinese) "显示最新 $shown 条，共 $count 条" else "Showing the latest $shown of $count"
        "<p class=\"comments-footnote\">${label.escapeHtml()}</p>"
    } else {
        ""
    }
    return commentsSection(title, count.toString(), body + footnote)
}

private fun commentsSection(title: String, count: String, body: String): String = """
<section class="pureracing-comments">
  <header class="comments-heading">
    <h2 class="comments-title">${title.escapeHtml()}</h2>
    <span class="comments-count">${count.escapeHtml()}</span>
  </header>
  $body
</section>
""".trimIndent()

private fun ArticleComment.toCommentHtml(isChinese: Boolean, reply: Boolean = false): String {
    val nickname = user?.nick_name.orEmpty().ifBlank { if (isChinese) "匿名车迷" else "Race fan" }
    val avatar = user?.avatar.orEmpty()
    val avatarHtml = if (avatar.isNotBlank()) {
        "<img class=\"comment-avatar\" src=\"${avatar.escapeHtml()}\" alt=\"\" loading=\"lazy\">"
    } else {
        "<span class=\"comment-avatar-fallback\">${nickname.firstOrNull()?.toString().orEmpty().escapeHtml()}</span>"
    }
    val picturesHtml = pics
        .mapNotNull { picture -> picture.pic_path.takeIf { path -> path.isNotBlank() } }
        .joinToString("") { path ->
            "<img class=\"comment-image\" src=\"${path.escapeHtml()}\" alt=\"\" loading=\"lazy\">"
        }
        .takeIf { it.isNotBlank() }
        ?.let { "<div class=\"comment-images\">$it</div>" }
        .orEmpty()
    val likes = if (digg_count > 0) {
        if (isChinese) "$digg_count 赞" else "$digg_count likes"
    } else {
        ""
    }
    val metadata = listOf(likes)
        .filter { it.isNotBlank() }
        .joinToString("<span aria-hidden=\"true\">·</span>") { "<span>${it.escapeHtml()}</span>" }
        .takeIf { it.isNotBlank() }
        ?.let { "<div class=\"comment-meta\">$it</div>" }
        .orEmpty()
    val repliesHtml = if (!reply && sub_list.isNotEmpty()) {
        val items = sub_list.joinToString("") { it.toCommentHtml(isChinese, reply = true) }
        val replyCount = maxOf(sub_count, sub_list.size)
        val label = if (isChinese) "共 $replyCount 条回复" else "$replyCount replies"
        """
<details class="comment-replies-disclosure">
  <summary>${label.escapeHtml()}</summary>
  <div class="comment-replies">$items</div>
</details>
""".trimIndent()
    } else {
        ""
    }

    return if (reply) {
        """
<div class="comment-reply">
  $avatarHtml
  <div class="comment-main">
    <div class="comment-author-row">
      <span class="comment-author">${nickname.escapeHtml()}</span>
      <span class="comment-time">${create_time.escapeHtml()}</span>
    </div>
    <p class="comment-body">${content.escapeHtml()}</p>
    $picturesHtml
  </div>
</div>
""".trimIndent()
    } else {
        """
<article class="comment-item">
  $avatarHtml
  <div class="comment-main">
    <div class="comment-author-row">
      <span class="comment-author">${nickname.escapeHtml()}</span>
      <span class="comment-time">${create_time.escapeHtml()}</span>
    </div>
    <p class="comment-body">${content.escapeHtml()}</p>
    $picturesHtml
    $metadata
    $repliesHtml
  </div>
</article>
""".trimIndent()
    }
}
