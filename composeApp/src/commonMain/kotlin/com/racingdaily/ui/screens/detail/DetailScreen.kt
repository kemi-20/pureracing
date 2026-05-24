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
        else article?.let { a -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Text(a.title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp)); Text("${a.total_read} reads", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            a.covers.firstOrNull()?.let { Spacer(Modifier.height(12.dp)); AsyncImage(it.path_url, null, Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.FillWidth) }
            Spacer(Modifier.height(16.dp)); Text(a.content, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp); Spacer(Modifier.height(32.dp))
        } }
    }
}
