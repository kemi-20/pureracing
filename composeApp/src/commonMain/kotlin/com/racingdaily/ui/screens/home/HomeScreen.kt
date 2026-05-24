package com.racingdaily.ui.screens.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.racingdaily.data.model.NavTab
import com.racingdaily.data.model.NewsItem
import com.racingdaily.data.remote.ApiService
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassChip
import com.racingdaily.ui.components.GlassSurface

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

    Column(Modifier.fillMaxSize()) {
        Text(
            "RacingDaily",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp)
        )
        LazyRow(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabs) { tab ->
                GlassChip(
                    label = tab.name,
                    selected = tab.id == selectedTabId,
                    onClick = { selectedTabId = tab.id }
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
                    GlassButton({ reloadKey++ }) { Text("Retry", color = Color.White) }
                }
            }
            else -> LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(news) { item ->
                    GlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onArticleClick(item.id) }
                    ) {
                        Column {
                            item.covers.firstOrNull()?.path_url?.let { url ->
                                AsyncImage(url, null, Modifier.fillMaxWidth().height(160.dp), contentScale = ContentScale.Crop)
                            }
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${item.total_read} reads",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    item.tags.firstOrNull()?.let {
                                        GlassChip(it.name, selected = true, onClick = {})
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
