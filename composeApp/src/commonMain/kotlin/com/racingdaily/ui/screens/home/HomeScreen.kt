package com.racingdaily.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.racingdaily.data.model.NavTab
import com.racingdaily.data.model.NewsItem
import com.racingdaily.data.remote.ApiService
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassChip
import com.racingdaily.ui.components.GlassIconButton
import com.racingdaily.ui.components.GlassSurface
import com.racingdaily.ui.components.ScreenHeader
import kotlinx.coroutines.flow.collect

@Composable
fun HomeScreen(
    onArticleClick: (NewsItem) -> Unit,
    onSearchClick: () -> Unit,
    listState: LazyListState,
    selectedTabId: Int,
    onSelectedTabIdChange: (Int) -> Unit,
    api: ApiService
) {
    var tabs by remember { mutableStateOf<List<NavTab>>(emptyList()) }
    var news by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadingMore by remember { mutableStateOf(false) }
    var nextPage by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    var loadMoreError by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var loadMoreRetryKey by remember { mutableIntStateOf(0) }
    val isChinese = Locale.current.language.startsWith("zh")
    val appTitle = if (isChinese) "纯净赛车" else "PureRacing"
    val appSubtitle = if (isChinese) "每日 F1 新闻" else "Daily F1 News"

    LaunchedEffect(reloadKey) {
        runCatching { api.getNavTabs().navbar }
            .onSuccess { tabs = it }
            .onFailure { error = it.message ?: "Unable to load tabs" }
    }

    LaunchedEffect(selectedTabId, reloadKey) {
        loading = true
        error = null
        loadMoreError = null
        loadMoreRetryKey = 0
        nextPage = 0
        runCatching { api.getNewsList(selectedTabId, page = 1) }
            .onSuccess {
                news = it.list
                nextPage = it.next_page
            }
            .onFailure { error = it.message ?: "Unable to load news" }
        loading = false
    }

    LaunchedEffect(selectedTabId, reloadKey) {
        snapshotFlow {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val totalItems = listState.layoutInfo.totalItemsCount
            val nearEnd = lastVisibleIndex >= totalItems - 4
            if (nearEnd && nextPage > 0 && !loading && !loadingMore && error == null && loadMoreError == null) {
                "${selectedTabId}:${nextPage}:${loadMoreRetryKey}"
            } else {
                ""
            }
        }.collect { requestKey ->
            if (requestKey.isBlank()) return@collect
            val pageToLoad = requestKey.substringAfter(':').substringBefore(':').toIntOrNull() ?: return@collect
            loadingMore = true
            loadMoreError = null
            runCatching { api.getNewsList(selectedTabId, page = pageToLoad) }
                .onSuccess { data ->
                    val existingIds = news.mapTo(mutableSetOf()) { it.id }
                    news = news + data.list.filter { it.id !in existingIds }
                    nextPage = data.next_page
                }
                .onFailure { loadMoreError = it.message ?: "Unable to load more news" }
            loadingMore = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = appTitle,
            subtitle = appSubtitle,
            actions = {
                GlassIconButton(Icons.Rounded.Search, "Search", onSearchClick)
            }
        )
        val tabsOverlayInitialOffset = 78.dp

        Box(Modifier.fillMaxSize()) {
            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                error != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(error.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        GlassButton({ reloadKey++ }) {
                            Icon(Icons.Rounded.Refresh, null, tint = Color.White)
                            Text("Retry", color = Color.White)
                        }
                    }
                }
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = tabsOverlayInitialOffset, bottom = 96.dp)
                ) {
                    items(news, key = { it.id }) { item ->
                        NewsGlassCard(item, onArticleClick)
                    }
                    if (loadingMore) {
                        item(key = "loading-more") {
                            Box(Modifier.fillMaxWidth().padding(vertical = 18.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                            }
                        }
                    } else if (loadMoreError != null) {
                        item(key = "load-more-error") {
                            GlassButton(
                                onClick = {
                                    loadMoreError = null
                                    loadMoreRetryKey++
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.Refresh, null, tint = Color.White)
                                Text("Retry loading more", color = Color.White)
                            }
                        }
                    }
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.16f))
                    .padding(vertical = 8.dp)
                    .zIndex(1f)
            ) {
                LazyRow(
                    Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tabs) { tab ->
                        GlassChip(
                            label = tab.name,
                            selected = tab.id == selectedTabId,
                            onClick = { onSelectedTabIdChange(tab.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsGlassCard(item: NewsItem, onArticleClick: (NewsItem) -> Unit) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onArticleClick(item) },
        contentPadding = PaddingValues(0.dp)
    ) {
        Column {
            val cover = item.covers.firstOrNull()?.path_url.orEmpty()
            if (cover.isNotBlank()) {
                AsyncImage(
                    cover,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(186.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (item.istop == 1) {
                        GlassChip("Pinned", selected = true, onClick = {})
                    }
                    item.tags.firstOrNull()?.let { tag ->
                        GlassChip(tag.name, selected = false, onClick = {})
                    }
                }
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.FiberManualRecord, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(9.dp))
                        Text(
                            "${item.total_read} reads",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        if (item.publish_time > 0) item.publish_time.toString() else "News",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
