package com.racingdaily.ui.screens.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flag
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
import com.racingdaily.data.model.ChampSeason
import com.racingdaily.data.model.ChampSub
import com.racingdaily.data.remote.ApiService
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassSurface
import com.racingdaily.ui.components.InfoPill
import com.racingdaily.ui.components.PreferenceGlassRow
import com.racingdaily.ui.components.ScreenHeader
import com.racingdaily.ui.components.SectionLabel

private data class ChampPreview(
    val category: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val season: ChampSeason?,
    val stations: List<ChampSub>,
    val error: String?
)

@Composable
fun MoreScreen(onChampClick: (String, Int) -> Unit, api: ApiService) {
    val isChinese = Locale.current.language.startsWith("zh")
    val appTitle = if (isChinese) "纯享赛车" else "PureRacing"
    val appSubtitle = if (isChinese) "每日赛车新闻与锦标赛数据" else "Daily racing news and championships"

    var reloadKey by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var custom by remember { mutableStateOf<ChampPreview?>(null) }
    var moto by remember { mutableStateOf<ChampPreview?>(null) }
    var tcr by remember { mutableStateOf<ChampPreview?>(null) }

    LaunchedEffect(reloadKey) {
        loading = true
        custom = loadChampPreview(
            category = "custom",
            title = if (isChinese) "自定义赛" else "Custom Series",
            subtitle = if (isChinese) "独立锦标赛赛历与分站" else "Independent calendar and rounds",
            icon = Icons.Rounded.EmojiEvents,
            seasonLoader = { api.getCustomSeason() },
            stationLoader = { api.getCustomSubstation().tmp },
            idOf = { it.custom_id }
        )
        moto = loadChampPreview(
            category = "motogp",
            title = "MotoGP",
            subtitle = if (isChinese) "赛历与分站成绩入口" else "Calendar and round results",
            icon = Icons.Rounded.SportsScore,
            seasonLoader = { api.getMotogpSeason() },
            stationLoader = { api.getMotogpSubstation().tmp },
            idOf = { it.motogp_id }
        )
        tcr = loadChampPreview(
            category = "tcr",
            title = "TCR",
            subtitle = if (isChinese) "区域系列赛分站入口" else "Regional series round results",
            icon = Icons.Rounded.Speed,
            seasonLoader = { api.getTcrSeason() },
            stationLoader = { api.getTcrSubstation().tmp },
            idOf = { it.tcr_id }
        )
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = if (isChinese) "更多" else "More",
            subtitle = if (isChinese) "锦标赛入口与应用信息" else "Championships and app info"
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
                    title = if (isChinese) "锦标赛" else "Championships",
                    subtitle = if (isChinese) "自定义赛 / MotoGP / TCR" else "Custom / MotoGP / TCR"
                )
            }

            if (loading && custom == null && moto == null && tcr == null) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            listOfNotNull(custom, moto, tcr).forEach { preview ->
                item {
                    ChampionshipSection(
                        preview = preview,
                        isChinese = isChinese,
                        onChampClick = onChampClick,
                        onRetry = { reloadKey++ }
                    )
                }
            }

            item {
                SectionLabel(
                    title = if (isChinese) "应用" else "App",
                    subtitle = if (isChinese) "纯享赛车 Android 与 Windows 客户端" else "PureRacing for Android and Windows"
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
                            if (isChinese) "关于本客户端" else "About this client",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            if (isChinese) {
                                "第三方资讯客户端，聚合已验证的公开赛历、排行榜与新闻接口。不包含登录、注册与评论功能。"
                            } else {
                                "A third-party news client that aggregates verified public race, ranking and news APIs. No login, registration or comments."
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
private fun ChampionshipSection(
    preview: ChampPreview,
    isChinese: Boolean,
    onChampClick: (String, Int) -> Unit,
    onRetry: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PreferenceGlassRow(
            title = preview.title,
            subtitle = preview.subtitle,
            icon = preview.icon,
            onClick = null,
            endContent = {
                val count = preview.stations.size
                if (count > 0) {
                    InfoPill(
                        label = if (isChinese) "${count} 站" else "$count rounds",
                        accent = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        if (preview.error != null) {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(preview.error, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    GlassButton(onRetry) {
                        Icon(Icons.Rounded.Refresh, null, tint = Color.White)
                        Text(if (isChinese) "重试" else "Retry", color = Color.White)
                    }
                }
            }
            return
        }

        val nextRace = preview.season?.tr_data
            ?.firstOrNull { row ->
                val status = row.lastOrNull()?.content.orEmpty()
                status.isBlank() || (!status.contains("完赛") && !status.contains("取消"))
            }
            ?: preview.season?.tr_data?.firstOrNull()

        if (nextRace != null) {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        if (isChinese) "赛历速览" else "Season snapshot",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        nextRace.getOrNull(1)?.content?.ifBlank { nextRace.firstOrNull()?.content.orEmpty() }.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val detail = listOfNotNull(
                        nextRace.getOrNull(0)?.content?.takeIf { it.isNotBlank() },
                        nextRace.getOrNull(2)?.content?.takeIf { it.isNotBlank() },
                        nextRace.lastOrNull()?.content?.takeIf { it.isNotBlank() }
                    ).joinToString(" · ")
                    if (detail.isNotBlank()) {
                        Text(
                            detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        val stations = preview.stations.take(8)
        if (stations.isNotEmpty()) {
            stations.forEach { station ->
                val id = when (preview.category) {
                    "motogp" -> station.motogp_id
                    "tcr" -> station.tcr_id
                    else -> station.custom_id
                }
                if (id > 0) {
                    PreferenceGlassRow(
                        title = station.season_name.ifBlank { preview.title },
                        subtitle = if (isChinese) "查看分站成绩" else "Open round standings",
                        icon = when (preview.category) {
                            "motogp" -> Icons.Rounded.Flag
                            "tcr" -> Icons.Rounded.Speed
                            else -> Icons.Rounded.EmojiEvents
                        },
                        onClick = { onChampClick(preview.category, id) }
                    )
                }
            }
            if (preview.stations.size > stations.size) {
                Text(
                    if (isChinese) {
                        "还有 ${preview.stations.size - stations.size} 站，可在分站页继续查看"
                    } else {
                        "${preview.stations.size - stations.size} more rounds available from station pages"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        } else if (preview.season != null) {
            Text(
                if (isChinese) "暂无分站列表" else "No station list available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(2.dp))
    }
}

private suspend fun loadChampPreview(
    category: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    seasonLoader: suspend () -> ChampSeason,
    stationLoader: suspend () -> List<ChampSub>,
    idOf: (ChampSub) -> Int
): ChampPreview {
    val seasonResult = runCatching { seasonLoader() }
    val stationResult = runCatching { stationLoader() }
    val stations = stationResult.getOrDefault(emptyList()).filter { idOf(it) > 0 }
    val error = when {
        seasonResult.isFailure && stationResult.isFailure ->
            seasonResult.exceptionOrNull()?.message ?: "无法加载锦标赛"
        else -> null
    }
    return ChampPreview(
        category = category,
        title = title,
        subtitle = subtitle,
        icon = icon,
        season = seasonResult.getOrNull(),
        stations = stations,
        error = error
    )
}
