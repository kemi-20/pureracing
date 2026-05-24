package com.racingdaily.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.racingdaily.data.model.NavTab
import com.racingdaily.data.model.NewsItem
import com.racingdaily.data.remote.ApiService

@Composable
fun HomeScreen(onArticleClick: (Int) -> Unit, api: ApiService) {
    var tabs by remember { mutableStateOf<List<NavTab>>(emptyList()) }
    var selectedTabId by remember { mutableIntStateOf(1) }
    var news by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(reloadKey) {
        runCatching { api.getNavTabs().navbar }
            .onSuccess { tabs = it }
            .onFailure { error = it.message ?: "Unable to load tabs" }
    }

    LaunchedEffect(selectedTabId, reloadKey) {
        loading = true
        error = null
        runCatching { api.getNewsList(selectedTabId) }
            .onSuccess { news = it.list }
            .onFailure { error = it.message ?: "Unable to load news" }
        loading = false
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Text("RacingDaily", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp))
        LazyRow(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tabs) { tab ->
                val sel = tab.id == selectedTabId
                FilterChip(sel, { selectedTabId = tab.id }, { Text(tab.name, fontSize = 12.sp, color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) })
            }
        }
        if (loading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
        else if (error != null) Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Button({ reloadKey++ }) { Text("Retry") }
            }
        }
        else LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
            items(news) { item ->
                ElevatedCard(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onArticleClick(item.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        item.covers.firstOrNull()?.path_url?.let { url ->
                            AsyncImage(url, null, Modifier.fillMaxWidth().height(160.dp), contentScale = ContentScale.Crop)
                        }
                        Column(Modifier.padding(12.dp)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${item.total_read} reads", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                item.tags.firstOrNull()?.let { SuggestionChip({}, { Text(it.name, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary) }) }
                            }
                        }
                    }
                }
            }
        }
    }
}
