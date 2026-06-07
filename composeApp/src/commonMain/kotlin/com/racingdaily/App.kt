package com.racingdaily

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.racingdaily.data.model.ChampSeason
import com.racingdaily.data.model.RaceGp
import com.racingdaily.data.model.RaceSession
import com.racingdaily.data.model.TrackInfo
import com.racingdaily.data.remote.ApiService
import com.racingdaily.platform.BackHandler
import com.racingdaily.ui.components.GlassBackdropHost
import com.racingdaily.ui.components.GlassBottomBar
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassChip
import com.racingdaily.ui.components.GlassIconButton
import com.racingdaily.ui.components.GlassNavTab
import com.racingdaily.ui.components.GlassSurface
import com.racingdaily.ui.components.ScreenHeader
import com.racingdaily.ui.components.pureRacingBackground
import com.racingdaily.ui.screens.detail.DetailScreen
import com.racingdaily.ui.screens.home.HomeScreen
import com.racingdaily.ui.screens.more.MoreScreen
import com.racingdaily.ui.screens.race.RaceScreen
import com.racingdaily.ui.screens.rankings.RankingScreen
import com.racingdaily.ui.theme.RacingDailyTheme
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

enum class Screen { HOME, RACE, RANKINGS, MORE }

sealed interface AppPage {
    data class Article(val id: Int, val title: String, val url: String) : AppPage
    data class Track(val id: Int) : AppPage
    data class Championship(val category: String, val id: Int) : AppPage
    data class RaceDetail(val gp: RaceGp) : AppPage
    data class DriverDetail(val chpId: Int, val driverId: Int, val name: String, val avatar: String, val teamLogo: String, val stats: JsonObject) : AppPage
    data class TeamDetail(val chpId: Int, val teamId: Int, val name: String, val logo: String, val stats: JsonObject) : AppPage
}

@Composable
fun App(api: ApiService) {
    RacingDailyTheme {
        GlassBackdropHost {
            var currentScreen by rememberSaveable { mutableStateOf(Screen.HOME) }
            var homeSelectedTabId by rememberSaveable { mutableIntStateOf(1) }
            val homeListState = rememberLazyListState()
            val pageStack = remember { mutableStateListOf<AppPage>() }
            val goBack = remember(pageStack) { { if (pageStack.isNotEmpty()) pageStack.removeAt(pageStack.lastIndex) } }

            BackHandler(enabled = pageStack.isNotEmpty(), onBack = goBack)

            Box(Modifier.fillMaxSize()) {
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        GlassBottomBar(
                            tabs = listOf(
                                GlassNavTab(Screen.HOME, Icons.AutoMirrored.Rounded.Article, "News"),
                                GlassNavTab(Screen.RACE, Icons.Rounded.CalendarMonth, "Race"),
                                GlassNavTab(Screen.RANKINGS, Icons.Rounded.EmojiEvents, "Rank"),
                                GlassNavTab(Screen.MORE, Icons.Rounded.MoreHoriz, "More")
                            ),
                            selected = currentScreen,
                            onSelected = { currentScreen = it }
                        )
                    }
                ) {
                    Box(Modifier.fillMaxSize()) {
                        when (currentScreen) {
                            Screen.HOME -> HomeScreen(
                                onArticleClick = { item ->
                                    pageStack += AppPage.Article(item.id, item.title, item.http_url)
                                },
                                listState = homeListState,
                                selectedTabId = homeSelectedTabId,
                                onSelectedTabIdChange = { homeSelectedTabId = it },
                                api = api
                            )
                            Screen.RACE -> RaceScreen(
                                onRaceClick = { pageStack += AppPage.RaceDetail(it) },
                                onTrackClick = { pageStack += AppPage.Track(it) },
                                api = api
                            )
                            Screen.RANKINGS -> RankingScreen(
                                api = api,
                                onDriverClick = { chpId, driverId, name, avatar, teamLogo, stats ->
                                    pageStack += AppPage.DriverDetail(chpId, driverId, name, avatar, teamLogo, stats)
                                },
                                onTeamClick = { chpId, teamId, name, logo, stats ->
                                    pageStack += AppPage.TeamDetail(chpId, teamId, name, logo, stats)
                                }
                            )
                            Screen.MORE -> MoreScreen(
                                { cat, id ->
                                    pageStack += AppPage.Championship(cat, id)
                                },
                                api
                            )
                        }
                    }
                }

                when (val page = pageStack.lastOrNull()) {
                    is AppPage.Championship -> AppPageOverlay(page) { ChampScreen(page.category, page.id, goBack, api) }
                    is AppPage.Track -> AppPageOverlay(page) { TrackScreen(page.id, goBack, api) }
                    is AppPage.Article -> AppPageOverlay(page) { DetailScreen(page.id, page.title, page.url, goBack, api) }
                    is AppPage.RaceDetail -> AppPageOverlay(page) { RaceDetailScreen(page.gp, goBack) }
                    is AppPage.DriverDetail -> AppPageOverlay(page) { DriverDetailScreen(page, goBack) }
                    is AppPage.TeamDetail -> AppPageOverlay(page) { TeamDetailScreen(page, goBack) }
                    null -> Unit
                }
            }
        }
    }
}

@Composable
private fun AppPageOverlay(pageKey: AppPage, content: @Composable () -> Unit) {
    var readyToShow by remember(pageKey) { mutableStateOf(false) }

    LaunchedEffect(pageKey) {
        withFrameNanos { }
        readyToShow = true
    }

    Box(
        Modifier
            .fillMaxSize()
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = if (readyToShow) 1f else 0f }
                .pureRacingBackground()
        ) {
            content()
        }
    }
}

@Composable
fun TrackScreen(trackId: Int, onBack: () -> Unit, api: ApiService) {
    var track by remember { mutableStateOf<TrackInfo?>(null) }
    var history by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(trackId, reloadKey) {
        loading = true
        error = null
        runCatching {
            track = api.getTrackInfo(trackId).track
            history = api.getTrackScore(trackId).history
        }.onFailure {
            error = it.message ?: "Unable to load track"
        }
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = track?.chinese_name?.ifBlank { track?.name.orEmpty() } ?: "Track",
            subtitle = listOf(track?.country, track?.location).filterNot { it.isNullOrBlank() }.joinToString(" / "),
            navigationIcon = {
                GlassIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Back", onBack)
            }
        )
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    track?.map_img?.takeIf { it.isNotBlank() }?.let {
                        GlassSurface(Modifier.fillMaxWidth()) {
                            AsyncImage(it, null, Modifier.fillMaxWidth().height(220.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    GlassSurface(
                        Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column {
                            Text(
                                track?.name.orEmpty(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                listOf(track?.country, track?.location).filterNot { it.isNullOrBlank() }.joinToString(" / "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("History", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                }
                items(history.take(20).size) { index ->
                    val row = history[index]
                    GlassSurface(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${row.text("season_id")} ${row.text("gp_name")}", color = MaterialTheme.colorScheme.onSurface)
                                Text(row.text("driver_name"), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(row.text("score").ifBlank { row.text("fast_lap") }, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChampScreen(category: String, id: Int, onBack: () -> Unit, api: ApiService) {
    var data by remember { mutableStateOf<ChampSeason?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(category, id, reloadKey) {
        loading = true
        error = null
        runCatching {
            when (category) {
                "motogp" -> api.getMotogpDriver(id)
                "tcr" -> api.getTcrDriver(id)
                else -> api.getCustomDriver(id)
            }
        }.onSuccess {
            data = it
        }.onFailure {
            error = it.message ?: "Unable to load championship"
        }
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Championship", "Season table", navigationIcon = {
            GlassIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Back", onBack)
        })
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (error != null) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    GlassButton({ reloadKey++ }) {
                        Icon(Icons.Rounded.Refresh, null, tint = Color.White)
                        Text("Retry", color = Color.White)
                    }
                }
            }
        } else {
            data?.let { d ->
                Column(Modifier.padding(16.dp)) {
                    Text(d.remark, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    d.tr_data.forEach { row ->
                        GlassSurface(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                row.forEach { cell ->
                                    when (cell.type) {
                                        1 -> AsyncImage(cell.content, null, Modifier.size(24.dp))
                                        else -> Text(
                                            cell.content,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
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

@Composable
fun RaceDetailScreen(gp: RaceGp, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(gp.gp_name.ifBlank { "Race" }, gp.track_name, navigationIcon = {
            GlassIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Back", onBack)
        })
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(gp.gp_logo, null, Modifier.size(58.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(gp.race_time_detail, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                            Text(gp.track_name, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        gp.weather?.let { weather ->
                            GlassChip("${weather.temp}C", true, onClick = {}, leadingIcon = Icons.Rounded.Public)
                        }
                    }
                }
            }
            items(gp.session.size) { index ->
                SessionCard(gp.session[index])
            }
        }
    }
}

@Composable
private fun SessionCard(session: RaceSession) {
    GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(session.session_name.joinToString(" / "), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                    Text(session.hour.joinToString(" / "), color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                }
                GlassChip(session.statusText(), selected = session.race_status == 1, onClick = {})
            }
            if (session.race_result.isEmpty()) {
                Text("No results yet", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            } else {
                session.race_result.take(10).forEach { result ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("${result.rank}", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp))
                        AsyncImage(result.team_logo, null, Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(result.dr_name, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        Text(
                            result.gap.ifBlank { result.score_p.takeIf { it > 0 }?.let { "$it pts" }.orEmpty() },
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DriverDetailScreen(page: AppPage.DriverDetail, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(page.name, "车手详情", navigationIcon = {
            GlassIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Back", onBack)
        })
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(page.avatar, null, Modifier.size(76.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(page.name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium)
                            Text("车手 ID ${page.driverId}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        AsyncImage(page.teamLogo, null, Modifier.size(42.dp))
                    }
                }
            }
            item {
                RankingStatsCard("本赛季数据", page.stats)
            }
        }
    }
}

@Composable
fun TeamDetailScreen(page: AppPage.TeamDetail, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(page.name, "车队详情", navigationIcon = {
            GlassIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Back", onBack)
        })
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(page.logo, null, Modifier.size(74.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(page.name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium)
                            Text("车队 ID ${page.teamId}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item {
                RankingStatsCard("本赛季数据", page.stats)
            }
        }
    }
}

@Composable
private fun RankingStatsCard(title: String, stats: JsonObject) {
    val rows = stats.rankingStatRows()
    GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Column {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            if (rows.isEmpty()) {
                Text("暂无可展示数据", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            } else {
                rows.forEach { (label, value) ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(value, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

private fun RaceSession.statusText(): String = when (race_status) {
    1 -> "Done"
    2 -> "Live"
    3 -> "Upcoming"
    else -> "Status $race_status"
}

private fun JsonObject.text(key: String): String =
    (this[key] as? JsonPrimitive)?.contentOrNull.orEmpty()

private fun JsonObject.rankingStatRows(): List<Pair<String, String>> {
    val preferredKeys = listOf(
        "display_order",
        "total_score",
        "gp_p1_cnt",
        "gp_p2_cnt",
        "gp_p3_cnt",
        "gp_pole_cnt",
        "gp_fastlap_cnt",
        "gp_fl_cnt",
        "gp_leadlap_cnt",
        "gp_start_cnt",
        "gp_end_cnt",
        "gp_start_end_percent",
        "gp_q_avg_rank_percent",
        "gp_race_avg_rank_percent",
        "use_time",
        "gp_name"
    )
    val rows = preferredKeys.mapNotNull { key ->
        val value = text(key)
        if (value.isBlank()) null else key.labelForRankingStat() to value
    }
    if (rows.isNotEmpty()) return rows

    return entries
        .filterNot { (key, _) -> key.isHiddenRankingStatKey() }
        .mapNotNull { (key, _) ->
            val value = text(key)
            if (value.isBlank()) null else key.labelForRankingStat() to value
        }
}

private fun String.labelForRankingStat(): String = when (this) {
    "display_order" -> "排名"
    "total_score" -> "积分"
    "gp_p1_cnt" -> "冠军"
    "gp_p2_cnt" -> "亚军"
    "gp_p3_cnt" -> "季军"
    "gp_pole_cnt" -> "杆位"
    "gp_fastlap_cnt", "gp_fl_cnt" -> "最快圈"
    "gp_leadlap_cnt" -> "领跑圈数"
    "gp_start_cnt" -> "出赛"
    "gp_end_cnt" -> "完赛"
    "gp_start_end_percent" -> "完赛率"
    "gp_q_avg_rank_percent" -> "排位平均排名"
    "gp_race_avg_rank_percent" -> "正赛平均排名"
    "use_time" -> "用时"
    "gp_name" -> "分站"
    else -> replace("_", " ")
}

private fun String.isHiddenRankingStatKey(): Boolean =
    this in setOf(
        "id",
        "driver_id",
        "drivers_id",
        "driver_avatar",
        "driver_abbr_chinese_name",
        "team_id",
        "team_logo",
        "team_name",
        "team_abbr_chinese_name",
        "page_show_color",
        "site_point"
    ) || contains("display_order") || endsWith("_trend") || endsWith("_format")
