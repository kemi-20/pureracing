package com.racingdaily.ui.screens.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SportsScore
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.racingdaily.data.model.ChampSub
import com.racingdaily.data.remote.ApiService
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassSurface
import com.racingdaily.ui.components.InfoPill
import com.racingdaily.ui.components.PreferenceGlassRow
import com.racingdaily.ui.components.ScreenHeader
import com.racingdaily.ui.components.SectionLabel

private data class SeriesHub(
    val category: String,
    val title: String,
    val purpose: String,
    val icon: ImageVector,
    val latestStationName: String?,
    val stationCount: Int,
    val defaultId: Int,
    val error: String?
)

@Composable
fun MoreScreen(onChampClick: (String, Int) -> Unit, api: ApiService) {
    val isChinese = Locale.current.language.startsWith("zh")
    val appTitle = if (isChinese) "纯享赛车" else "PureRacing"
    val appSubtitle = if (isChinese) "每日 F1 新闻 · 赛事 · 排名" else "Daily F1 news, races and standings"

    var reloadKey by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var series by remember { mutableStateOf<List<SeriesHub>>(emptyList()) }

    LaunchedEffect(reloadKey) {
        loading = true
        series = listOf(
            loadSeriesHub(
                category = "custom",
                title = if (isChinese) "自定义赛" else "Custom Series",
                purpose = if (isChinese) "查看独立系列的分站成绩" else "Open independent series round results",
                icon = Icons.Rounded.EmojiEvents,
                stationLoader = { api.getCustomSubstation().tmp },
                idOf = { it.custom_id }
            ),
            loadSeriesHub(
                category = "motogp",
                title = "MotoGP",
                purpose = if (isChinese) "摩托车世锦赛分站成绩" else "Motorcycle World Championship rounds",
                icon = Icons.Rounded.SportsScore,
                stationLoader = { api.getMotogpSubstation().tmp },
                idOf = { it.motogp_id }
            ),
            loadSeriesHub(
                category = "tcr",
                title = "TCR",
                purpose = if (isChinese) "房车系列赛分站成绩" else "Touring car series round results",
                icon = Icons.Rounded.Speed,
                stationLoader = { api.getTcrSubstation().tmp },
                idOf = { it.tcr_id }
            )
        )
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = if (isChinese) "更多" else "More",
            subtitle = if (isChinese) "快捷入口与应用信息" else "Shortcuts and app info"
        )
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                SectionLabel(
                    title = if (isChinese) "系列入口" else "Series",
                    subtitle = if (isChinese) "只保留有用入口，不堆完整赛果" else "Useful shortcuts only, no bulk results"
                )
            }

            if (loading && series.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            series.forEach { hub ->
                item {
                    SeriesHubCard(
                        hub = hub,
                        isChinese = isChinese,
                        onOpen = {
                            if (hub.defaultId > 0) onChampClick(hub.category, hub.defaultId)
                        },
                        onRetry = { reloadKey++ }
                    )
                }
            }

            item {
                SectionLabel(
                    title = if (isChinese) "应用" else "App",
                    subtitle = if (isChinese) "版本与说明" else "Version and notes"
                )
            }
            item {
                PreferenceGlassRow(
                    title = appTitle,
                    subtitle = appSubtitle,
                    icon = Icons.Rounded.Info,
                    onClick = null,
                    endContent = {
                        InfoPill("1.2", accent = MaterialTheme.colorScheme.primary)
                    }
                )
            }
            item {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (isChinese) "这个页面做什么" else "What this page is for",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            if (isChinese) {
                                "更多页只放常用入口：MotoGP、TCR 和自定义赛。完整 F1 新闻、赛历和积分榜请用底部导航。"
                            } else {
                                "More is only for useful shortcuts: MotoGP, TCR and Custom. Use the bottom tabs for F1 news, calendar and standings."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeriesHubCard(
    hub: SeriesHub,
    isChinese: Boolean,
    onOpen: () -> Unit,
    onRetry: () -> Unit
) {
    if (hub.error != null) {
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(hub.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(hub.error, color = MaterialTheme.colorScheme.onSurfaceVariant)
                GlassButton(onRetry) {
                    Icon(Icons.Rounded.Refresh, null, tint = Color.White)
                    Text(if (isChinese) "重试" else "Retry", color = Color.White)
                }
            }
        }
        return
    }

    val latest = hub.latestStationName
    val subtitle = buildString {
        append(hub.purpose)
        if (!latest.isNullOrBlank()) {
            append(if (isChinese) " · 最近：" else " · Latest: ")
            append(latest)
        }
    }

    PreferenceGlassRow(
        title = hub.title,
        subtitle = subtitle,
        icon = hub.icon,
        onClick = if (hub.defaultId > 0) onOpen else null,
        endContent = {
            if (hub.stationCount > 0) {
                InfoPill(
                    label = if (isChinese) "${hub.stationCount} 站" else "${hub.stationCount}",
                    accent = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

private suspend fun loadSeriesHub(
    category: String,
    title: String,
    purpose: String,
    icon: ImageVector,
    stationLoader: suspend () -> List<ChampSub>,
    idOf: (ChampSub) -> Int
): SeriesHub {
    val result = runCatching { stationLoader() }
    val stations = result.getOrDefault(emptyList()).filter { idOf(it) > 0 }
    // Substation APIs usually return newest first; keep only that signal on More.
    val latest = stations.firstOrNull()
    return SeriesHub(
        category = category,
        title = title,
        purpose = purpose,
        icon = icon,
        latestStationName = latest?.season_name?.takeIf { it.isNotBlank() },
        stationCount = stations.size,
        defaultId = latest?.let(idOf) ?: 0,
        error = result.exceptionOrNull()?.message
    )
}
