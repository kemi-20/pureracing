package com.racingdaily.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.racingdaily.data.model.NewsItem
import com.racingdaily.data.remote.ApiService
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassChip
import com.racingdaily.ui.components.GlassIconButton
import com.racingdaily.ui.components.GlassSurface
import com.racingdaily.ui.components.ScreenHeader
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onArticleClick: (NewsItem) -> Unit,
    api: ApiService
) {
    var query by rememberSaveable { mutableStateOf("") }
    var submittedQuery by rememberSaveable { mutableStateOf("") }
    var results by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(submittedQuery) {
        val text = submittedQuery.trim()
        if (text.isBlank()) {
            results = emptyList()
            error = null
            loading = false
            return@LaunchedEffect
        }

        loading = true
        error = null
        runCatching { api.searchNewsLocally(text) }
            .onSuccess { results = it }
            .onFailure { error = it.message ?: "Unable to search news" }
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "搜索",
            subtitle = "Search news",
            navigationIcon = {
                GlassIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Back", onBack)
            }
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("输入关键词") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { submittedQuery = query }
                )
            )
            GlassButton(
                onClick = { submittedQuery = query },
                modifier = Modifier.fillMaxWidth(),
                selected = true
            ) {
                Icon(Icons.Rounded.Search, null, tint = Color.White)
                Text("搜索", color = Color.White)
            }
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            error != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    GlassButton({ submittedQuery = query }) {
                        Icon(Icons.Rounded.Refresh, null, tint = Color.White)
                        Text("Retry", color = Color.White)
                    }
                }
            }
            submittedQuery.isBlank() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("搜索新闻标题和标签", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            results.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("没有找到相关新闻", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
            ) {
                items(results, key = { it.id }) { item ->
                    SearchResultCard(item, onArticleClick)
                }
            }
        }
    }
}

private suspend fun ApiService.searchNewsLocally(query: String): List<NewsItem> = coroutineScope {
    val tabs = getNavTabs().navbar.ifEmpty { return@coroutineScope emptyList() }
    val normalizedQuery = query.trim().lowercase()
    val requests = tabs.flatMap { tab ->
        (1..3).map { page ->
            async { runCatching { getNewsList(tab.id, page).list }.getOrDefault(emptyList()) }
        }
    }
    val candidates = requests.awaitAll().flatten()

    candidates
        .distinctBy { it.id }
        .filter { item ->
            item.title.lowercase().contains(normalizedQuery) ||
                item.tags.any { tag -> tag.name.lowercase().contains(normalizedQuery) }
        }
        .sortedWith(compareByDescending<NewsItem> { it.publish_time }.thenByDescending { it.total_read })
}

@Composable
private fun SearchResultCard(item: NewsItem, onArticleClick: (NewsItem) -> Unit) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onArticleClick(item) },
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val cover = item.covers.firstOrNull()?.path_url.orEmpty()
            if (cover.isNotBlank()) {
                AsyncImage(
                    cover,
                    contentDescription = null,
                    modifier = Modifier.width(118.dp).height(92.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(Modifier.weight(1f).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    item.tags.firstOrNull()?.let { tag ->
                        GlassChip(tag.name, selected = false, onClick = {})
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.FiberManualRecord,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(8.dp)
                        )
                        Text(
                            "${item.total_read} reads",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
