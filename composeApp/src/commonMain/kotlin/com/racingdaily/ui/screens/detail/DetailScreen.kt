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
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${a.total_read} reads", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (a.user_name.isNotEmpty()) Text(a.user_name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    if (a.temotime.isNotEmpty()) Text(a.temotime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Extract and show images from HTML content
                val images = extractImages(a.content)
                val text = parseHtml(a.content)

                if (images.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    images.take(3).forEach { url ->
                        AsyncImage(url, null, Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).padding(vertical = 4.dp), contentScale = ContentScale.FillWidth)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp)
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

fun extractImages(html: String): List<String> {
    return Regex("""<img[^>]+src="([^"]+)"""").findAll(html).map { it.groupValues[1] }.toList()
}

fun parseHtml(html: String): String {
    var result = html
    // Replace block elements with newlines
    result = result.replace(Regex("</?(p|div|h[1-6]|li|tr|br)[^>]*>", RegexOption.IGNORE_CASE), "\n")
    // Remove remaining tags
    result = result.replace(Regex("<[^>]+>"), "")
    // Decode entities
    result = result.replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<")
        .replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'")
    // Collapse multiple blank lines
    result = result.replace(Regex("\n{3,}"), "\n\n").trim()
    return result
}
