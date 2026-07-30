package com.racingdaily.ui.screens.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material.icons.rounded.TwoWheeler
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.racingdaily.data.model.ChampSub
import com.racingdaily.data.remote.ApiService
import com.racingdaily.platform.appVersionLabel
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassSurface
import com.racingdaily.ui.components.InfoPill
import com.racingdaily.ui.components.PreferenceGlassRow
import com.racingdaily.ui.components.ScreenHeader
import com.racingdaily.ui.components.SectionLabel
import com.racingdaily.ui.theme.ThemeMode
import com.racingdaily.util.runSuspendCatching
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

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
fun MoreScreen(
    onChampClick: (String, Int) -> Unit,
    api: ApiService,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val isChinese = Locale.current.language.startsWith("zh")
    val appTitle = if (isChinese) "纯享赛车" else "PureRacing"
    val appSubtitle = if (isChinese) "每日 F1 新闻" else "Daily F1 News"

    var reloadKey by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var series by remember { mutableStateOf<List<SeriesHub>>(emptyList()) }

    LaunchedEffect(reloadKey) {
        loading = true
        series = supervisorScope {
            listOf(
                async {
                    loadSeriesHub(
                        category = "motogp",
                        title = "MotoGP",
                        purpose = if (isChinese) "摩托车世界锦标赛" else "Motorcycle World Championship",
                        icon = Icons.Rounded.TwoWheeler,
                        stationLoader = { api.getMotogpSubstation().tmp },
                        idOf = { it.motogp_id }
                    )
                },
                async {
                    loadSeriesHub(
                        category = "tcr",
                        title = "TCR",
                        purpose = if (isChinese) "房车系列赛" else "Touring car series",
                        icon = Icons.Rounded.DirectionsCar,
                        stationLoader = { api.getTcrSubstation().tmp },
                        idOf = { it.tcr_id }
                    )
                },
                async {
                    loadSeriesHub(
                        category = "custom",
                        title = if (isChinese) "自定义赛" else "Custom Series",
                        purpose = if (isChinese) "独立赛事与赛季积分" else "Independent series and standings",
                        icon = Icons.Rounded.EmojiEvents,
                        stationLoader = { api.getCustomSubstation().tmp },
                        idOf = { it.custom_id }
                    )
                }
            ).awaitAll()
        }
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = if (isChinese) "更多" else "More",
            subtitle = if (isChinese) "个性化与赛事入口" else "Personalization and series"
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 108.dp)
        ) {
            item {
                SectionLabel(
                    title = if (isChinese) "外观" else "Appearance",
                    subtitle = if (isChinese) "即时切换，不中断当前浏览" else "Switch instantly without losing your place"
                )
            }
            item {
                ThemeModeSelector(
                    selected = themeMode,
                    isChinese = isChinese,
                    onSelected = onThemeModeChange
                )
            }

            item {
                SectionLabel(
                    title = if (isChinese) "其他赛事" else "More series",
                    subtitle = if (isChinese) "摩托车、房车与独立锦标赛" else "Motorcycle, touring car and independent series"
                )
            }

            if (loading && series.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                series.forEach { hub ->
                    item {
                        SeriesHubRow(
                            hub = hub,
                            isChinese = isChinese,
                            onOpen = {
                                if (hub.defaultId > 0) onChampClick(hub.category, hub.defaultId)
                            },
                            onRetry = { reloadKey++ }
                        )
                    }
                }
            }

            item {
                SectionLabel(title = if (isChinese) "关于" else "About")
            }
            item {
                PreferenceGlassRow(
                    title = appTitle,
                    subtitle = appSubtitle,
                    icon = Icons.Rounded.Info,
                    endContent = {
                        InfoPill(appVersionLabel, accent = MaterialTheme.colorScheme.primary)
                    }
                )
            }
            item {
                PreferenceGlassRow(
                    title = if (isChinese) "数据来源" else "Data source",
                    subtitle = if (isChinese) "每日赛车官方公开数据" else "Official RacingDaily public data",
                    icon = Icons.Rounded.CloudDone,
                    endContent = {
                        InfoPill(if (isChinese) "已连接" else "Online", accent = MaterialTheme.colorScheme.secondary)
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    isChinese: Boolean,
    onSelected: (ThemeMode) -> Unit
) {
    val choices = listOf(
        Triple(ThemeMode.SYSTEM, Icons.Rounded.SettingsBrightness, if (isChinese) "跟随系统" else "System"),
        Triple(ThemeMode.LIGHT, Icons.Rounded.LightMode, if (isChinese) "浅色" else "Light"),
        Triple(ThemeMode.DARK, Icons.Rounded.DarkMode, if (isChinese) "深色" else "Dark")
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        choices.forEach { (mode, icon, label) ->
            val active = mode == selected
            GlassButton(
                onClick = { onSelected(mode) },
                modifier = Modifier.weight(1f),
                selected = active,
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp))
                Text(text = label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
            }
        }
    }
}

@Composable
private fun SeriesHubRow(
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(hub.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        if (isChinese) "暂时无法读取赛事数据" else "Series data is unavailable",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                GlassButton(onRetry, selected = false) {
                    Icon(Icons.Rounded.Refresh, null)
                    Text(if (isChinese) "重试" else "Retry")
                }
            }
        }
        return
    }

    val subtitle = buildString {
        append(hub.purpose)
        hub.latestStationName?.takeIf { it.isNotBlank() }?.let {
            append(if (isChinese) " · " else " · ")
            append(it)
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
                    label = if (isChinese) "${hub.stationCount} 站" else "${hub.stationCount} rounds",
                    accent = MaterialTheme.colorScheme.secondary
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
    val result = runSuspendCatching { stationLoader() }
    val stations = result.getOrDefault(emptyList()).filter { idOf(it) > 0 }
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
