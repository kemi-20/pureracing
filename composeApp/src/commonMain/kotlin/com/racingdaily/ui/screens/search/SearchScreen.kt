package com.racingdaily.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.racingdaily.data.model.NewsItem
import com.racingdaily.data.remote.ApiService
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassIconButton
import com.racingdaily.ui.components.GlassMaterial
import com.racingdaily.ui.components.GlassSurface
import com.racingdaily.ui.components.InfoPill
import com.racingdaily.ui.components.SectionLabel
import com.racingdaily.ui.components.ScreenHeader
import com.racingdaily.ui.components.newsCardReveal
import com.racingdaily.ui.screens.home.NewsCoverImage
import com.racingdaily.util.runSuspendCatching
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

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
    var searchReloadKey by remember { mutableIntStateOf(0) }

    fun submitSearch() {
        val text = query.trim()
        if (submittedQuery == text) {
            searchReloadKey++
        } else {
            submittedQuery = text
        }
    }

    LaunchedEffect(submittedQuery, searchReloadKey) {
        val text = submittedQuery.trim()
        if (text.isBlank()) {
            results = emptyList()
            error = null
            loading = false
            return@LaunchedEffect
        }

        loading = true
        error = null
        runSuspendCatching { api.searchNewsLocally(text) }
            .onSuccess { results = it }
            .onFailure { error = it.message ?: "无法搜索新闻" }
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "搜索",
            subtitle = "搜索新闻",
            navigationIcon = {
                GlassIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "返回", onBack)
            }
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                GlassSurface(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    material = GlassMaterial.CHROME,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { submitSearch() }
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Row(
                                Modifier.fillMaxWidth().height(56.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                    if (query.isBlank()) Text("输入关键词", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    innerTextField()
                                }
                                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                    if (query.isNotBlank()) {
                                        GlassIconButton(
                                            icon = Icons.Rounded.Close,
                                            contentDescription = "清空",
                                            onClick = { query = "" },
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
                GlassIconButton(
                    icon = Icons.Rounded.Search,
                    contentDescription = "搜索",
                    onClick = { submitSearch() },
                    selected = true
                )
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
                    GlassButton({ submitSearch() }) {
                        Icon(Icons.Rounded.Refresh, null, tint = Color.White)
                        Text("重试", color = Color.White)
                    }
                }
            }
            submittedQuery.isBlank() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.Search, null, modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.secondary)
                    Text("搜索新闻标题和标签", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                }
            }
            results.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.Search, null, modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("没有找到相关新闻", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
            ) {
                item {
                    SectionLabel("搜索结果", "共 ${results.size} 篇文章")
                }
                itemsIndexed(results, key = { index, item -> "${item.id}:$index" }) { index, item ->
                    SearchResultCard(item, "search|${item.id}|$index", onArticleClick)
                }
            }
        }
    }
}
private suspend fun ApiService.searchNewsLocally(query: String): List<NewsItem> = coroutineScope {
    val tabs = getNavTabs().navbar.ifEmpty { return@coroutineScope emptyList() }
    val normalizedQuery = query.trim().lowercase()
    val requestLimit = Semaphore(6)
    val requests = tabs.flatMap { tab ->
        (1..3).map { page ->
            async {
                requestLimit.withPermit {
                    runSuspendCatching { getNewsList(tab.id, page).list }.getOrDefault(emptyList())
                }
            }
        }
    }
    val candidates = requests.awaitAll().flatten()

    candidates
        .distinctBy { item ->
            if (item.id > 0) "id:${item.id}" else "fallback:${item.title}:${item.publish_time}"
        }
        .filter { item ->
            item.title.lowercase().contains(normalizedQuery) ||
                item.tags.any { tag -> tag.name.lowercase().contains(normalizedQuery) }
        }
        .sortedWith(compareByDescending<NewsItem> { it.publish_time }.thenByDescending { it.total_read })
}

@Composable
private fun SearchResultCard(
    item: NewsItem,
    revealKey: String,
    onArticleClick: (NewsItem) -> Unit
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        material = GlassMaterial.THIN,
        onClick = { onArticleClick(item) },
        contentPadding = PaddingValues(0.dp),
        isLazyListItem = true
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(116.dp)
                .newsCardReveal(revealKey),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val cover = item.covers.firstOrNull()
            if (cover != null && (cover.path_url.isNotBlank() || cover.path.isNotBlank())) {
                NewsCoverImage(
                    cover = cover,
                    modifier = Modifier.width(132.dp).fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )
            }
            Column(Modifier.weight(1f).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    item.tags.firstOrNull()?.let { tag -> InfoPill(tag.name) }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Visibility,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            "${item.total_read} 次阅读",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
