package com.racingdaily.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
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
import coil3.compose.AsyncImage
import com.racingdaily.data.model.NavTab
import com.racingdaily.data.model.NewsItem
import com.racingdaily.data.remote.ApiService
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassChip
import com.racingdaily.ui.components.GlassIconButton
import com.racingdaily.ui.components.GlassSurface
import com.racingdaily.ui.components.InfoPill
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
    val appTitle = if (isChinese) "纯享赛车" else "PureRacing"
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
        LazyRow(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tabs) { tab ->
                GlassChip(
                    label = tab.name,
                    selected = tab.id == selectedTabId,
                    onClick = { onSelectedTabIdChange(tab.id) }
                )
            }
        }

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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 104.dp)
                ) {
                    itemsIndexed(news, key = { _, item -> item.id }) { index, item ->
                        NewsGlassCard(
                            item = item,
                            featured = index == 0,
                            onArticleClick = onArticleClick
                        )
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
        }
    }
}

@Composable
private fun NewsGlassCard(
    item: NewsItem,
    featured: Boolean,
    onArticleClick: (NewsItem) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (featured) 24.dp else 20.dp),
        onClick = { onArticleClick(item) },
        contentPadding = PaddingValues(0.dp)
    ) {
        val cover = item.covers.firstOrNull()?.path_url.orEmpty()
        if (featured) {
            Column {
                if (cover.isNotBlank()) {
                    AsyncImage(
                        cover,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                        contentScale = ContentScale.Crop
                    )
                }
                NewsCardContent(item, titleLines = 3, featured = true, modifier = Modifier.padding(18.dp))
            }
        } else {
            Row(
                Modifier.fillMaxWidth().height(134.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (cover.isNotBlank()) {
                    AsyncImage(
                        cover,
                        contentDescription = null,
                        modifier = Modifier.width(132.dp).fillMaxHeight(),
                        contentScale = ContentScale.Crop
                    )
                }
                NewsCardContent(
                    item = item,
                    titleLines = 2,
                    featured = false,
                    modifier = Modifier.weight(1f).padding(horizontal = 15.dp, vertical = 13.dp)
                )
            }
        }
    }
}

@Composable
private fun NewsCardContent(
    item: NewsItem,
    titleLines: Int,
    featured: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(if (featured) 11.dp else 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            if (item.istop == 1) {
                InfoPill("Pinned", accent = MaterialTheme.colorScheme.primary)
            }
            item.tags.firstOrNull()?.let { tag -> InfoPill(tag.name) }
        }
        if (featured) {
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .width(4.dp)
                        .height(48.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(999.dp))
                )
                Text(
                    item.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = titleLines,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                item.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = titleLines,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Visibility, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                Text(item.total_read.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                item.publish_time.toNewsDateLabel(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun Long.toNewsDateLabel(): String {
    if (this <= 0) return "News"
    val seconds = if (this > 10_000_000_000L) this / 1000L else this
    val localDays = (seconds + 8L * 60L * 60L) / 86_400L
    val (year, month, day) = civilDateFromEpochDays(localDays)
    return "$year-${month.twoDigits()}-${day.twoDigits()}"
}

private fun civilDateFromEpochDays(epochDays: Long): Triple<Int, Int, Int> {
    val z = epochDays + 719_468L
    val era = if (z >= 0) z / 146_097L else (z - 146_096L) / 146_097L
    val doe = z - era * 146_097L
    val yoe = (doe - doe / 1_460L + doe / 36_524L - doe / 146_096L) / 365L
    val y = yoe + era * 400L
    val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
    val mp = (5L * doy + 2L) / 153L
    val day = (doy - (153L * mp + 2L) / 5L + 1L).toInt()
    val month = (mp + if (mp < 10L) 3L else -9L).toInt()
    val year = (y + if (month <= 2) 1L else 0L).toInt()
    return Triple(year, month, day)
}

private fun Int.twoDigits(): String = if (this < 10) "0$this" else toString()
