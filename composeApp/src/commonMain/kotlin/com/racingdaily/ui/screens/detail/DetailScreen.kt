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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
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
                is HtmlBlock.Text -> Text(block.text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp)
                is HtmlBlock.Bold -> Text(block.text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
                is HtmlBlock.Italic -> Text(block.text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp, fontStyle = FontStyle.Italic)
            }
        }
    }
}

sealed class HtmlBlock {
    data class Image(val src: String) : HtmlBlock()
    data class Text(val text: String) : HtmlBlock()
    data class Bold(val text: String) : HtmlBlock()
    data class Italic(val text: String) : HtmlBlock()
}

fun parseHtmlBlocks(html: String): List<HtmlBlock> {
    val blocks = mutableListOf<HtmlBlock>()
    // First extract images
    var remaining = html
    val imgRegex = Regex("""<img[^>]+src="([^"]+)"[^>]*>""")
    val imgMatches = imgRegex.findAll(remaining).toList()
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

    // Split by newlines and process
    val paragraphs = remaining.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
    var imgIndex = 0
    paragraphs.forEach { para ->
        val parts = para.split("{{IMG}}")
        parts.forEachIndexed { i, part ->
            if (i > 0 && imgIndex < imgMatches.size) {
                blocks.add(HtmlBlock.Image(imgMatches[imgIndex].groupValues[1]))
                imgIndex++
            }
            if (part.isNotBlank()) {
                // Process bold/italic markers
                parseInlineMarkers(part, blocks)
            }
        }
        // Any remaining images for this paragraph
        while (imgIndex < imgMatches.size && para.count { it == '{' } == 0) {
            blocks.add(HtmlBlock.Image(imgMatches[imgIndex].groupValues[1]))
            imgIndex++
        }
    }
    // Add any images that weren't in paragraphs
    while (imgIndex < imgMatches.size) {
        blocks.add(HtmlBlock.Image(imgMatches[imgIndex].groupValues[1]))
        imgIndex++
    }
    return blocks.ifEmpty { listOf(HtmlBlock.Text(stripAllTags(html))) }
}

fun parseInlineMarkers(text: String, blocks: MutableList<HtmlBlock>) {
    var remaining = text
    while (remaining.isNotEmpty()) {
        val boldStart = remaining.indexOf("{{B}}")
        val italicStart = remaining.indexOf("{{I}}")
        val nextMarker = listOf(boldStart, italicStart).filter { it >= 0 }.minOrNull()

        if (nextMarker == null) {
            blocks.add(HtmlBlock.Text(remaining))
            break
        }

        if (nextMarker > 0) {
            blocks.add(HtmlBlock.Text(remaining.substring(0, nextMarker)))
        }

        if (nextMarker == boldStart) {
            val end = remaining.indexOf("{{/B}}", nextMarker + 5)
            if (end >= 0) {
                blocks.add(HtmlBlock.Bold(remaining.substring(nextMarker + 5, end)))
                remaining = remaining.substring(end + 6)
            } else {
                remaining = remaining.substring(nextMarker + 5)
            }
        } else if (nextMarker == italicStart) {
            val end = remaining.indexOf("{{/I}}", nextMarker + 5)
            if (end >= 0) {
                blocks.add(HtmlBlock.Italic(remaining.substring(nextMarker + 5, end)))
                remaining = remaining.substring(end + 6)
            } else {
                remaining = remaining.substring(nextMarker + 5)
            }
        }
    }
}

fun stripAllTags(html: String): String {
    return html.replace(Regex("<[^>]+>"), "")
        .replace("&nbsp;", " ").replace("&amp;", "&")
        .replace("&lt;", "<").replace("&gt;", ">").trim()
}
