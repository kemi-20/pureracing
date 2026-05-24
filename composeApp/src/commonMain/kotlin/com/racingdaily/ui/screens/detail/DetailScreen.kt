package com.racingdaily.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
                HtmlContent(a.content)
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun HtmlContent(html: String) {
    val blocks = remember(html) { parseHtmlBlocks(html) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        blocks.forEach { block ->
            when (block) {
                is HtmlBlock.Image -> AsyncImage(block.src, null, Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).padding(vertical = 4.dp), contentScale = ContentScale.FillWidth)
                is HtmlBlock.Video -> VideoPlaceholder(block.src)
                is HtmlBlock.Text -> Text(block.text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp)
                is HtmlBlock.Bold -> Text(block.text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
                is HtmlBlock.Italic -> Text(block.text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp, fontStyle = FontStyle.Italic)
            }
        }
    }
}

@Composable
fun VideoPlaceholder(videoUrl: String) {
    val open = rememberOpenUrl()
    Surface(
        Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)).clickable { open(videoUrl) },
        color = Color.Black.copy(alpha = 0.3f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("▶", color = Color.White, fontSize = 36.sp)
            Text("Tap to play", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp))
        }
    }
}

sealed class HtmlBlock {
    data class Image(val src: String) : HtmlBlock()
    data class Video(val src: String) : HtmlBlock()
    data class Text(val text: String) : HtmlBlock()
    data class Bold(val text: String) : HtmlBlock()
    data class Italic(val text: String) : HtmlBlock()
}

fun parseHtmlBlocks(html: String): List<HtmlBlock> {
    val blocks = mutableListOf<HtmlBlock>()
    var remaining = html

    // Extract videos first (before img since video may contain source)
    val videoRegex = Regex("""<source\s+src="([^"]+)"[^>]*>""")
    val videoMatches = videoRegex.findAll(remaining).map { it.groupValues[1] }.toList()
    remaining = Regex("""<video[^>]*>.*?</video>""", RegexOption.IGNORE_CASE).replace(remaining) { m ->
        val idx = videoMatches.indexOfFirst { m.value.contains(it) }
        if (idx >= 0) "{{VIDEO}}" else ""
    }

    // Extract images
    val imgRegex = Regex("""<img[^>]+src="([^"]+)"[^>]*>""")
    val imgMatches = imgRegex.findAll(remaining).map { it.groupValues[1] }.toList()
    remaining = imgRegex.replace(remaining, "{{IMG}}")

    // Replace block tags with newlines
    remaining = remaining.replace(Regex("</?(p|div|h[1-6]|li|tr|br)[^>]*>", RegexOption.IGNORE_CASE), "\n")

    // Extract bold text
    remaining = Regex("<b>(.+?)</b>", RegexOption.IGNORE_CASE).replace(remaining) { "{{B}}${it.groupValues[1]}{{/B}}" }
    remaining = Regex("<strong>(.+?)</strong>", RegexOption.IGNORE_CASE).replace(remaining) { "{{B}}${it.groupValues[1]}{{/B}}" }

    // Extract italic text
    remaining = Regex("<i>(.+?)</i>", RegexOption.IGNORE_CASE).replace(remaining) { "{{I}}${it.groupValues[1]}{{/I}}" }
    remaining = Regex("<em>(.+?)</em>", RegexOption.IGNORE_CASE).replace(remaining) { "{{I}}${it.groupValues[1]}{{/I}}" }

    // Remove remaining tags
    remaining = remaining.replace(Regex("<[^>]+>"), "")

    // Decode entities
    remaining = remaining.replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<")
        .replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'")

    // Process placeholders in order
    var vidIdx = 0; var imgIdx = 0
    val pattern = Regex("""\{\{VIDEO\}\}|\{\{IMG\}\}""")
    var lastEnd = 0
    pattern.findAll(remaining).forEach { match ->
        if (match.range.first > lastEnd) {
            val text = remaining.substring(lastEnd, match.range.first).trim()
            if (text.isNotEmpty()) blocks.add(HtmlBlock.Text(text))
        }
        when (match.value) {
            "{{VIDEO}}" -> { if (vidIdx < videoMatches.size) { blocks.add(HtmlBlock.Video(videoMatches[vidIdx])); vidIdx++ } }
            "{{IMG}}" -> { if (imgIdx < imgMatches.size) { blocks.add(HtmlBlock.Image(imgMatches[imgIdx])); imgIdx++ } }
        }
        lastEnd = match.range.last + 1
    }
    if (lastEnd < remaining.length) {
        val text = remaining.substring(lastEnd).trim()
        if (text.isNotEmpty()) blocks.add(HtmlBlock.Text(text))
    }
    // Add any remaining media
    while (vidIdx < videoMatches.size) { blocks.add(HtmlBlock.Video(videoMatches[vidIdx])); vidIdx++ }
    while (imgIdx < imgMatches.size) { blocks.add(HtmlBlock.Image(imgMatches[imgIdx])); imgIdx++ }

    return blocks.ifEmpty { listOf(HtmlBlock.Text(remaining.trim())) }
}

@Composable
expect fun rememberOpenUrl(): (String) -> Unit
