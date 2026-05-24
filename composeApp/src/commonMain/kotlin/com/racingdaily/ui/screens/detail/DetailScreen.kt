package com.racingdaily.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.racingdaily.data.model.NewsDetail
import com.racingdaily.data.remote.ApiService
import kotlinx.coroutines.launch

@Composable
fun DetailScreen(articleId: Int, onBack: () -> Unit, api: ApiService) {
    var article by remember { mutableStateOf<NewsDetail?>(null) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(articleId) { scope.launch { runCatching { api.getNewsDetail(articleId) }.onSuccess { article = it }; loading = false } }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) { TextButton(onBack) { Text("< Back", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp) } }
        if (loading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else article?.details?.let { a ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                Text(a.title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${a.total_read} reads", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (a.temotime.isNotEmpty()) Text(a.temotime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(12.dp))
                HtmlRenderer(a.content)
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun HtmlRenderer(html: String) {
    val nodes = remember(html) { parseHtml(html) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        nodes.forEach { node ->
            when (node) {
                is HtmlNode.Image -> AsyncImage(node.src, null, Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.FillWidth)
                is HtmlNode.Video -> VideoPlayer(node.src)
                is HtmlNode.Text -> Text(node.richText, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp)
                is HtmlNode.Bold -> Text(node.richText, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
                is HtmlNode.Break -> Spacer(Modifier.height(8.dp))
            }
        }
    }
}

sealed class HtmlNode {
    data class Image(val src: String) : HtmlNode()
    data class Video(val src: String) : HtmlNode()
    data class Text(val richText: String) : HtmlNode()
    data class Bold(val richText: String) : HtmlNode()
    data object Break : HtmlNode()
}

fun parseHtml(html: String): List<HtmlNode> {
    val nodes = mutableListOf<HtmlNode>()
    var s = html
    while (s.isNotEmpty()) {
        when {
            s.startsWith("<img") -> { val src = Regex("""src="([^"]+)"""").find(s)?.groupValues?.get(1) ?: ""; nodes.add(HtmlNode.Image(src)); s = s.substringAfter(">").trimStart() }
            s.contains("<video") && s.indexOf("<video") < (s.indexOf("<img").let { if (it < 0) Int.MAX_VALUE else it }) -> {
                val vidTag = Regex("<video[^>]*>.*?</video>", RegexOption.DOT_MATCHES_ALL).find(s)
                if (vidTag != null) { val src = Regex("""src="([^"]+)"""").find(vidTag.value)?.groupValues?.get(1) ?: ""; nodes.add(HtmlNode.Video(src)); s = s.substring(vidTag.range.last + 1).trimStart() } else break
            }
            s.startsWith("<br") -> { nodes.add(HtmlNode.Break); s = s.substringAfter(">").trimStart() }
            s.startsWith("</p>") || s.startsWith("</div>") -> { nodes.add(HtmlNode.Break); s = s.substringAfter(">").trimStart() }
            s.startsWith("<strong>") || s.startsWith("<b>") -> {
                val tag = if (s.startsWith("<strong>")) "strong" else "b"; val end = s.indexOf("</$tag>")
                if (end > 0) { val text = cleanText(s.substring(s.indexOf(">") + 1, end)); if (text.isNotBlank()) nodes.add(HtmlNode.Bold(text)); s = s.substring(end + tag.length + 3).trimStart() } else { s = s.substringAfter(">").trimStart() }
            }
            s.first() == '<' -> s = s.substringAfter(">").trimStart()
            else -> { val nextTag = s.indexOf('<'); val text = cleanText(if (nextTag < 0) s else s.substring(0, nextTag)); if (text.isNotBlank()) nodes.add(HtmlNode.Text(text)); s = if (nextTag < 0) "" else s.substring(nextTag) }
        }
    }
    return nodes.ifEmpty { listOf(HtmlNode.Text(cleanText(html))) }
}

fun cleanText(s: String) = s.replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").trim()

@Composable
expect fun VideoPlayer(url: String)
