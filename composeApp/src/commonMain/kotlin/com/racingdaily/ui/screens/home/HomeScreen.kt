package com.racingdaily.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.racingdaily.data.model.Cover
import com.racingdaily.data.model.NavTab
import com.racingdaily.data.model.NewsItem
import com.racingdaily.data.remote.ApiService
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassChip
import com.racingdaily.ui.components.GlassIconButton
import com.racingdaily.ui.components.GlassMaterial
import com.racingdaily.ui.components.GlassSurface
import com.racingdaily.ui.components.InfoPill
import com.racingdaily.ui.components.ScreenHeader
import com.racingdaily.ui.components.newsCardReveal
import com.racingdaily.util.runSuspendCatching
import kotlinx.coroutines.flow.collect

@Composable
fun HomeScreen(
    onArticleClick: (NewsItem) -> Unit,
    onSearchClick: () -> Unit,
    listState: LazyListState,
    selectedTabId: Int,
    onSelectedTabIdChange: (Int) -> Unit,
    isArticleRead: (Int) -> Boolean,
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
        runSuspendCatching { api.getNavTabs(forceRefresh = reloadKey > 0).navbar }
            .onSuccess { tabs = it }
            .onFailure {
                if (news.isEmpty()) error = it.message ?: "无法加载新闻分类"
            }
    }

    LaunchedEffect(selectedTabId, reloadKey) {
        loading = true
        error = null
        loadMoreError = null
        loadMoreRetryKey = 0
        nextPage = 0
        runSuspendCatching { api.getNewsList(selectedTabId, page = 1, forceRefresh = reloadKey > 0) }
            .onSuccess {
                news = it.list
                nextPage = it.next_page
                error = null
            }
            .onFailure { error = it.message ?: "无法加载新闻" }
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
            runSuspendCatching { api.getNewsList(selectedTabId, page = pageToLoad) }
                .onSuccess { data ->
                    val existingIds = news.mapTo(mutableSetOf()) { it.id }
                    news = news + data.list.filter { it.id !in existingIds }
                    nextPage = data.next_page
                }
                .onFailure { loadMoreError = it.message ?: "无法加载更多新闻" }
            loadingMore = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = appTitle,
            subtitle = appSubtitle,
            actions = {
                GlassIconButton(Icons.Rounded.Search, "搜索", onSearchClick)
            }
        )
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
                            Text("重试", color = Color.White)
                        }
                    }
                }
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 82.dp, bottom = 104.dp)
                ) {
                    itemsIndexed(news, key = { index, item -> "${item.id}:$index" }) { index, item ->
                        NewsGlassCard(
                            item = item,
                            featured = index == 0,
                            isRead = isArticleRead(item.id),
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
                                Text("重试加载更多", color = Color.White)
                            }
                        }
                    }
                }
            }

            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .zIndex(2f)
                    .padding(vertical = 5.dp),
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(tabs, key = { index, tab -> "${tab.id}:$index" }) { _, tab ->
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

@Composable
private fun NewsGlassCard(
    item: NewsItem,
    featured: Boolean,
    isRead: Boolean,
    onArticleClick: (NewsItem) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        shape = RoundedCornerShape(if (featured) 24.dp else 18.dp),
        material = if (featured) GlassMaterial.FLOATING else GlassMaterial.THIN,
        onClick = { onArticleClick(item) },
        contentPadding = PaddingValues(0.dp),
        isLazyListItem = true
    ) {
        val cover = item.covers.firstOrNull()
        val hasCover = cover?.hasImageSource() == true
        Box(Modifier.fillMaxWidth().newsCardReveal(item.id)) {
            if (featured && hasCover) {
                Box(Modifier.fillMaxWidth().aspectRatio(1.36f)) {
                    NewsCoverImage(
                        cover = checkNotNull(cover),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    0.48f to Color.Black.copy(alpha = 0.08f),
                                    1f to Color.Black.copy(alpha = 0.84f)
                                )
                            )
                    )
                    NewsBadges(
                        item = item,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp)
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 17.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            item.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = if (isRead) Color(0xFFB8BCC5) else Color.White,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                        NewsMetadata(item = item, onImage = true)
                    }
                }
            } else if (featured) {
                Column {
                    NewsCardContent(
                        item = item,
                        titleLines = 3,
                        featured = true,
                        isRead = isRead,
                        showBadges = true,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 17.dp)
                    )
                }
            } else {
                Row(
                    Modifier.fillMaxWidth().height(132.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasCover) {
                        NewsCoverImage(
                            cover = checkNotNull(cover),
                            modifier = Modifier.width(138.dp).fillMaxHeight(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    NewsCardContent(
                        item = item,
                        titleLines = 2,
                        featured = false,
                        isRead = isRead,
                        showBadges = true,
                        modifier = Modifier.weight(1f).padding(horizontal = 15.dp, vertical = 13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NewsCoverImage(
    cover: Cover,
    modifier: Modifier,
    contentScale: ContentScale
) {
    val sources = remember(cover.path_url, cover.path) { cover.imageSources() }
    var sourceIndex by remember(sources) { mutableIntStateOf(0) }
    Box(
        modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        sources.getOrNull(sourceIndex)?.let { source ->
            AsyncImage(
                model = source,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                onError = {
                    if (sourceIndex < sources.lastIndex) sourceIndex++
                }
            )
        }
    }
}

private fun Cover.hasImageSource(): Boolean = path_url.isNotBlank() || path.isNotBlank()

private fun Cover.imageSources(): List<String> {
    val primary = path_url.trim().toAbsoluteNewsImageUrl()
    val alternate = path.trim().toAbsoluteNewsImageUrl()
    val baseSources = listOf(primary, alternate).filter { it.isNotBlank() }.distinct()
    val retrySource = baseSources.firstOrNull()?.let { source ->
        source + if ('?' in source) "&pureracing_retry=1" else "?pureracing_retry=1"
    }
    return if (retrySource == null) baseSources else baseSources + retrySource
}

private fun String.toAbsoluteNewsImageUrl(): String = when {
    isBlank() -> ""
    startsWith("http://") || startsWith("https://") -> this
    startsWith("/") -> "https://oss.static.romielf.com$this"
    else -> "https://oss.static.romielf.com/$this"
}

@Composable
private fun NewsCardContent(
    item: NewsItem,
    titleLines: Int,
    featured: Boolean,
    isRead: Boolean,
    showBadges: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(if (featured) 11.dp else 8.dp)) {
        if (showBadges) {
            NewsBadges(item)
        }
        if (featured) {
            Text(
                item.title,
                style = MaterialTheme.typography.titleLarge,
                color = if (isRead) readTitleColor() else MaterialTheme.colorScheme.onSurface,
                maxLines = titleLines,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                item.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isRead) readTitleColor() else MaterialTheme.colorScheme.onSurface,
                maxLines = titleLines,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
        }
        NewsMetadata(item = item)
    }
}

@Composable
private fun readTitleColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)

@Composable
private fun NewsMetadata(item: NewsItem, onImage: Boolean = false) {
    val contentColor = if (onImage) Color.White.copy(alpha = 0.84f) else MaterialTheme.colorScheme.onSurfaceVariant
    val dateLabel = remember(item.publish_time) { item.publish_time.toNewsDateLabel() }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Visibility, null, tint = contentColor, modifier = Modifier.size(15.dp))
            Text(
                item.total_read.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
        Text(
            dateLabel,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

@Composable
private fun NewsBadges(item: NewsItem, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        if (item.istop == 1) {
            InfoPill("置顶", accent = MaterialTheme.colorScheme.primary)
        }
        item.tags.firstOrNull()?.let { tag -> InfoPill(tag.name) }
    }
}

private fun Long.toNewsDateLabel(): String {
    if (this <= 0) return "新闻"
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
