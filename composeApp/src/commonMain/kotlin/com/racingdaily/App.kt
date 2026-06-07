package com.racingdaily

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.racingdaily.data.model.ChampSeason
import com.racingdaily.data.model.RaceGp
import com.racingdaily.data.model.RaceSession
import com.racingdaily.data.model.TeamInfoData
import com.racingdaily.data.model.TeamDriverInfo
import com.racingdaily.data.model.TeamWorkerInfo
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
    data class DriverDetail(val chpId: Int, val seasonId: Int, val driverId: Int, val name: String, val avatar: String, val teamLogo: String, val stats: JsonObject) : AppPage
    data class TeamDetail(val chpId: Int, val seasonId: Int, val teamId: Int, val name: String, val logo: String, val stats: JsonObject) : AppPage
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
                                onDriverClick = { chpId, seasonId, driverId, name, avatar, teamLogo, stats ->
                                    pageStack += AppPage.DriverDetail(chpId, seasonId, driverId, name, avatar, teamLogo, stats)
                                },
                                onTeamClick = { chpId, seasonId, teamId, name, logo, stats ->
                                    pageStack += AppPage.TeamDetail(chpId, seasonId, teamId, name, logo, stats)
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
                    is AppPage.TeamDetail -> AppPageOverlay(page) { TeamDetailScreen(page, goBack, api) }
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
    val profile = page.driverProfile()
    var selectedTab by rememberSaveable(page.driverId) { mutableStateOf("info") }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(profile.chineseName, "Driver profile", navigationIcon = {
            GlassIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Back", onBack)
        })
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            page.avatar,
                            null,
                            Modifier.size(96.dp).clip(CircleShape),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(profile.chineseName, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium)
                            Text(profile.englishName.ifBlank { "Driver ID ${page.driverId}" }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                listOf(profile.team, profile.number.takeIf { it.isNotBlank() }?.let { "#$it" })
                                    .filterNotNull()
                                    .filter { it.isNotBlank() }
                                    .joinToString(" "),
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        AsyncImage(page.teamLogo, null, Modifier.size(42.dp))
                    }
                }
            }
            item {
                ProfileTabs(
                    tabs = listOf("info" to "资料", "score" to "成绩", "news" to "新闻"),
                    selected = selectedTab,
                    onSelected = { selectedTab = it }
                )
            }
            when (selectedTab) {
                "info" -> item { InfoTable("基本资料", profile.infoRows) }
                "score" -> item { RankingStatsCard("本赛季数据", page.stats) }
                "news" -> item { EmptyProfileCard("新闻", "暂无相关新闻数据") }
            }
        }
    }
}

@Composable
fun TeamDetailScreen(page: AppPage.TeamDetail, onBack: () -> Unit, api: ApiService) {
    var teamInfo by remember(page.teamId) { mutableStateOf<TeamInfoData?>(null) }
    var loading by remember(page.teamId) { mutableStateOf(true) }
    var error by remember(page.teamId) { mutableStateOf<String?>(null) }
    var selectedTab by rememberSaveable(page.teamId) { mutableStateOf("info") }

    LaunchedEffect(page.chpId, page.seasonId, page.teamId) {
        loading = true
        error = null
        runCatching { api.getTeamInfo(page.chpId, page.teamId, page.seasonId) }
            .onSuccess { teamInfo = it }
            .onFailure { error = it.message ?: "Unable to load team profile" }
        loading = false
    }

    val title = teamInfo?.chinese_name?.ifBlank { teamInfo?.name.orEmpty() }?.ifBlank { page.name } ?: page.name
    val subtitle = teamInfo?.name?.ifBlank { "Team profile" } ?: "Team profile"

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title, subtitle, navigationIcon = {
            GlassIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Back", onBack)
        })
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(teamInfo?.logo?.ifBlank { page.logo } ?: page.logo, null, Modifier.size(74.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall)
                            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                            teamInfo?.first_entry?.takeIf { it.isNotBlank() }?.let {
                                Text(it, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            teamInfo?.photo?.takeIf { it.isNotBlank() }?.let { photo ->
                item {
                    GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(8.dp)) {
                        AsyncImage(photo, null, Modifier.fillMaxWidth().height(148.dp), contentScale = ContentScale.Crop)
                    }
                }
            }
            item {
                val tabs = teamInfo?.tab?.map { it.value to it.text }?.takeIf { it.isNotEmpty() }
                    ?: listOf("info" to "资料", "score" to "成绩", "car" to "车", "news" to "新闻")
                ProfileTabs(tabs, selectedTab, onSelected = { selectedTab = it })
            }
            if (loading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (selectedTab == "info") {
                item {
                    InfoTable("基本资料", teamInfo.teamInfoRows(page))
                }
                teamInfo?.drivers?.driver?.takeIf { it.isNotEmpty() }?.let { drivers ->
                    item { TeamDriversSection("车手", drivers) }
                }
                teamInfo?.drivers?.worker?.takeIf { it.isNotEmpty() }?.let { workers ->
                    item { TeamWorkersSection("主要成员", workers) }
                }
                item { TeamNamedRow("测试车手", teamInfo?.drivers?.test?.map { it.name.ifBlank { it.addr_chinese_name } }.orEmpty()) }
                item { TeamNamedRow("青训车手", teamInfo?.drivers?.youth?.map { it.name.ifBlank { it.addr_chinese_name } }.orEmpty()) }
                error?.let { item { EmptyProfileCard("接口提示", it) } }
            } else if (selectedTab == "score") {
                item { RankingStatsCard("本赛季数据", page.stats) }
            } else if (selectedTab == "car") {
                val cars = teamInfo?.car.orEmpty()
                if (cars.isEmpty()) {
                    item { EmptyProfileCard("车", "暂无赛车数据") }
                } else {
                    items(cars.size) { index ->
                        val car = cars[index]
                        GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("${car.season_id} ${car.chassis}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(car.power_unit, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                car.photo.firstOrNull()?.let { photo ->
                                    AsyncImage(photo, null, Modifier.fillMaxWidth().height(150.dp), contentScale = ContentScale.Crop)
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == "news") {
                item { EmptyProfileCard("新闻", "暂无相关新闻数据") }
            }
        }
    }
}

@Composable
private fun ProfileTabs(
    tabs: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit
) {
    LazyRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tabs.size) { index ->
            val (value, text) = tabs[index]
            GlassChip(text, selected = selected == value, onClick = { onSelected(value) })
        }
    }
}

@Composable
private fun InfoTable(title: String, rows: List<Pair<String, String>>) {
    GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            rows.forEach { (label, value) ->
                InfoRow(label, value)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(92.dp)
        )
        Text(
            value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (value.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TeamDriversSection(title: String, drivers: List<TeamDriverInfo>) {
    GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            drivers.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { driver ->
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(driver.avatar, null, Modifier.size(62.dp), contentScale = ContentScale.Fit)
                            Text("#${driver.number}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(driver.addr_chinese_name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun TeamWorkersSection(title: String, workers: List<TeamWorkerInfo>) {
    GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            workers.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { worker ->
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(worker.avatar, null, Modifier.size(62.dp), contentScale = ContentScale.Fit)
                            Text(worker.position, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(worker.addr_chinese_name, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun TeamNamedRow(title: String, names: List<String>) {
    InfoTable(title, listOf(title to names.filter { it.isNotBlank() }.joinToString("、")))
}

@Composable
private fun EmptyProfileCard(title: String, message: String) {
    GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
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

private data class DriverProfile(
    val chineseName: String,
    val englishName: String,
    val team: String,
    val number: String,
    val infoRows: List<Pair<String, String>>
)

private fun AppPage.DriverDetail.driverProfile(): DriverProfile {
    val team = stats.text("team_abbr_chinese_name").ifBlank { stats.text("team_name") }
    return when (driverId) {
        210928 -> DriverProfile(
            chineseName = "安东内利",
            englishName = "Andrea Kimi Antonelli",
            team = "梅赛德斯",
            number = "12",
            infoRows = listOf(
                "英文名" to "Andrea Kimi Antonelli",
                "中文名" to "安东内利",
                "F1首赛" to "2025年澳大利亚站",
                "状态" to "现役",
                "昵称" to "",
                "星座" to "",
                "年龄" to "20",
                "生日" to "2006-08-25",
                "身高" to "172cm",
                "体重" to "70kg",
                "车队" to "梅赛德斯",
                "车手号码" to "12",
                "T架颜色" to "绿色",
                "薪水" to "200万美元(2025)",
                "合同期" to "2026-12-31",
                "超级驾照分" to "7",
                "一年罚分" to "5",
                "国籍" to "意大利",
                "出生地" to "意大利博洛尼亚",
                "居住地" to ""
            )
        )
        else -> DriverProfile(
            chineseName = name,
            englishName = "",
            team = team,
            number = stats.text("number").ifBlank { stats.text("driver_number") },
            infoRows = listOf(
                "中文名" to name,
                "车队" to team,
                "车手ID" to "$driverId"
            )
        )
    }
}

private fun TeamInfoData?.teamInfoRows(page: AppPage.TeamDetail): List<Pair<String, String>> {
    if (this == null) {
        return listOf(
            "中文名" to page.name,
            "车队ID" to "${page.teamId}"
        )
    }
    return listOf(
        "英文名" to name,
        "中文名" to chinese_name,
        "F1首赛" to first_entry,
        "历史名称" to history,
        "车队类型" to fleet_type,
        "动力单元" to power_unit.ifBlank { supplier },
        "底盘" to chassis
    ).filter { (_, value) -> value.isNotBlank() }
}
