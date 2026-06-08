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
import com.racingdaily.data.model.RankingData
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
import kotlinx.serialization.json.intOrNull

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
                    is AppPage.DriverDetail -> AppPageOverlay(page) { DriverDetailScreen(page, goBack, api) }
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
fun DriverDetailScreen(page: AppPage.DriverDetail, onBack: () -> Unit, api: ApiService) {
    val profile = page.driverProfile()
    var selectedTab by rememberSaveable(page.driverId) { mutableStateOf("info") }
    var seasonScores by remember(page.driverId) { mutableStateOf<List<DriverSeasonScore>>(emptyList()) }
    var scoreLoading by remember(page.driverId) { mutableStateOf(true) }
    var scoreError by remember(page.driverId) { mutableStateOf<String?>(null) }

    LaunchedEffect(page.driverId, page.chpId, page.seasonId) {
        scoreLoading = true
        scoreError = null
        runCatching {
            val firstSeason = profile.firstSeason().coerceAtLeast(1950)
            (firstSeason..page.seasonId).mapNotNull { seasonId ->
                runCatching {
                    api.getDriverRanking(page.chpId, seasonId).toDriverSeasonScore(seasonId, page.driverId)
                }.getOrNull()
            }.sortedByDescending { it.season }
        }.onSuccess {
            seasonScores = it
        }.onFailure {
            scoreError = it.message ?: "Unable to load season results"
        }
        scoreLoading = false
    }

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
                "score" -> item {
                    DriverSeasonScoresCard(
                        scores = seasonScores,
                        loading = scoreLoading,
                        error = scoreError,
                        fallbackStats = page.stats
                    )
                }
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
    val displayValue = value.ifBlank { "-" }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(92.dp)
        )
        Text(
            displayValue,
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
private fun DriverSeasonScoresCard(
    scores: List<DriverSeasonScore>,
    loading: Boolean,
    error: String?,
    fallbackStats: JsonObject
) {
    GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("近年来成绩", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text("F1", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            when {
                loading -> Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                error != null && scores.isEmpty() -> {
                    Text(error, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    DriverFallbackStats(fallbackStats)
                }
                scores.isEmpty() -> DriverFallbackStats(fallbackStats)
                else -> {
                    DriverSeasonScoreRow(
                        listOf("赛季", "名次", "总积分", "分冠", "领奖台", "杆位", "最快圈速"),
                        isHeader = true
                    )
                    scores.forEach { score ->
                        DriverSeasonScoreRow(
                            listOf(
                                score.season.toString(),
                                score.rank.asScoreText(),
                                score.totalScore.asScoreText(),
                                score.wins.asScoreText(),
                                score.podiums.asScoreText(),
                                score.poles.asScoreText(),
                                score.fastestLaps.asScoreText()
                            ),
                            isHeader = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverFallbackStats(stats: JsonObject) {
    val rows = stats.rankingStatRows()
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

@Composable
private fun DriverSeasonScoreRow(values: List<String>, isHeader: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        values.forEachIndexed { index, value ->
            Text(
                value,
                color = if (isHeader) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                style = if (isHeader) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyLarge,
                fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Bold,
                modifier = Modifier.weight(if (index == 2) 1.25f else 1f)
            )
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

private fun JsonObject.intValue(key: String): Int {
    val primitive = this[key] as? JsonPrimitive ?: return 0
    return primitive.intOrNull ?: primitive.content.toIntOrNull() ?: 0
}

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

private data class DriverSeasonScore(
    val season: Int,
    val rank: Int,
    val totalScore: Int,
    val wins: Int,
    val podiums: Int,
    val poles: Int,
    val fastestLaps: Int
)

private fun RankingData.toDriverSeasonScore(season: Int, driverId: Int): DriverSeasonScore? {
    fun rowFrom(tabKey: String): JsonObject? =
        list.firstOrNull { it.tab_key == tabKey }
            ?.list
            ?.firstOrNull { row -> row.intValue("driver_id").ifZero { row.intValue("drivers_id") } == driverId }

    val total = rowFrom("total_score") ?: return null
    val podium = rowFrom("gp_p_cnt")
    val pole = rowFrom("gp_pole_cnt")
    val fastest = rowFrom("gp_fl_cnt") ?: rowFrom("gp_fastlap_cnt")
    val wins = podium?.intValue("gp_p1_cnt") ?: total.intValue("gp_p1_cnt")
    val podiums = (podium?.intValue("gp_p1_cnt") ?: total.intValue("gp_p1_cnt")) +
        (podium?.intValue("gp_p2_cnt") ?: total.intValue("gp_p2_cnt")) +
        (podium?.intValue("gp_p3_cnt") ?: total.intValue("gp_p3_cnt"))

    return DriverSeasonScore(
        season = season,
        rank = total.intValue("display_order"),
        totalScore = total.intValue("total_score"),
        wins = wins,
        podiums = podiums,
        poles = pole?.intValue("gp_pole_cnt") ?: total.intValue("gp_pole_cnt"),
        fastestLaps = fastest?.intValue("gp_fl_cnt") ?: fastest?.intValue("gp_fastlap_cnt") ?: total.intValue("gp_fl_cnt")
    )
}

private fun Int.asScoreText(): String = if (this == 0) "0" else toString()

private inline fun Int.ifZero(block: () -> Int): Int = if (this == 0) block() else this

private data class DriverProfile(
    val chineseName: String,
    val englishName: String,
    val team: String,
    val number: String,
    val infoRows: List<Pair<String, String>>
)

private data class DriverProfileSeed(
    val chineseName: String,
    val englishName: String,
    val team: String,
    val number: String,
    val firstRace: String,
    val birthday: String,
    val height: String,
    val weight: String,
    val nationality: String,
    val birthplace: String,
    val residence: String = "",
    val age: String = "",
    val nickname: String = "",
    val zodiac: String = "",
    val tColor: String = "",
    val salary: String = "",
    val contract: String = "",
    val superLicense: String = "",
    val penaltyPoints: String = "",
    val instagram: String = ""
)

private fun AppPage.DriverDetail.driverProfile(): DriverProfile {
    val team = stats.text("team_abbr_chinese_name").ifBlank { stats.text("team_name") }
    val seed = driverProfileSeeds[driverId]
    if (seed != null) {
        return DriverProfile(
            chineseName = seed.chineseName,
            englishName = seed.englishName,
            team = seed.team,
            number = seed.number,
            infoRows = listOf(
                "英文名" to seed.englishName,
                "中文名" to seed.chineseName,
                "F1首赛" to seed.firstRace,
                "状态" to "现役",
                "昵称" to seed.nickname,
                "星座" to seed.zodiac,
                "年龄" to seed.age,
                "生日" to seed.birthday,
                "身高" to seed.height,
                "体重" to seed.weight,
                "车队" to seed.team,
                "车手号码" to seed.number,
                "T架颜色" to seed.tColor,
                "薪水" to seed.salary,
                "合同期" to seed.contract,
                "超级驾照分" to seed.superLicense,
                "一年罚分" to seed.penaltyPoints,
                "国籍" to seed.nationality,
                "出生地" to seed.birthplace,
                "居住地" to seed.residence,
                "INS" to seed.instagram
            )
        )
    }
    val teamName = team.ifBlank { teamNameById[stats.text("team_id")] ?: "" }
    return DriverProfile(
        chineseName = name,
        englishName = "",
        team = teamName,
        number = stats.text("number").ifBlank { stats.text("driver_number") },
        infoRows = listOf(
            "中文名" to name,
            "车队" to teamName,
            "车手ID" to "$driverId"
        )
    )
}

private fun DriverProfile.firstSeason(): Int =
    infoRows.firstOrNull { it.first == "F1首赛" }
        ?.second
        ?.take(4)
        ?.toIntOrNull()
        ?: 2019

private val teamNameById = mapOf(
    "79" to "奥迪",
    "80" to "法拉利",
    "81" to "梅赛德斯",
    "82" to "阿斯顿马丁",
    "83" to "迈凯伦",
    "84" to "威廉姆斯",
    "85" to "红牛",
    "86" to "哈斯",
    "87" to "红牛二队",
    "88" to "Alpine",
    "210212" to "凯迪拉克"
)

private val driverProfileSeeds = mapOf(
    110 to DriverProfileSeed(
        chineseName = "汉密尔顿",
        englishName = "Sir Lewis Carl Davidson Hamilton",
        team = "法拉利",
        number = "44",
        firstRace = "2007年澳大利亚站",
        birthday = "1985-01-07",
        height = "174cm",
        weight = "73kg",
        nationality = "英国",
        birthplace = "英国，斯蒂芙尼奇",
        residence = "摩纳哥",
        age = "41",
        nickname = "小汉，老汉，爵士，汉一圈",
        zodiac = "摩羯座",
        tColor = "绿色",
        salary = "6000万美元(2025)",
        contract = "2026-12-31",
        superLicense = "9",
        penaltyPoints = "3"
    ),
    210928 to DriverProfileSeed(
        chineseName = "安东内利",
        englishName = "Andrea Kimi Antonelli",
        team = "梅赛德斯",
        number = "12",
        firstRace = "2025年澳大利亚站",
        birthday = "2006-08-25",
        height = "172cm",
        weight = "70kg",
        nationality = "意大利",
        birthplace = "意大利博洛尼亚",
        age = "20",
        tColor = "绿色",
        salary = "200万美元(2025)",
        contract = "2026-12-31",
        superLicense = "7",
        penaltyPoints = "5"
    ),
    123 to DriverProfileSeed(
        chineseName = "拉塞尔",
        englishName = "George William Russell",
        team = "梅赛德斯",
        number = "63",
        firstRace = "2019年澳大利亚站",
        birthday = "1998-02-15",
        height = "185cm",
        weight = "70kg",
        nationality = "英国",
        birthplace = "英国，金士林",
        residence = "英国",
        age = "28",
        nickname = "皇帝",
        zodiac = "水瓶座",
        tColor = "黑色",
        salary = "1500万美元(2025)",
        contract = "2026-12-31",
        superLicense = "12",
        penaltyPoints = "0",
        instagram = "@georgerussell63"
    ),
    121 to DriverProfileSeed("勒克莱尔", "Charles Leclerc", "法拉利", "16", "2018年澳大利亚站", "1997-10-16", "180cm", "69kg", "摩纳哥", "摩纳哥蒙特卡洛"),
    210899 to DriverProfileSeed("皮亚斯特里", "Oscar Piastri", "迈凯伦", "81", "2023年巴林站", "2001-04-06", "178cm", "68kg", "澳大利亚", "澳大利亚墨尔本"),
    122 to DriverProfileSeed("诺里斯", "Lando Norris", "迈凯伦", "4", "2019年澳大利亚站", "1999-11-13", "170cm", "68kg", "英国", "英国布里斯托尔", residence = "摩纳哥"),
    116 to DriverProfileSeed("维斯塔潘", "Max Verstappen", "红牛", "1", "2015年澳大利亚站", "1997-09-30", "181cm", "72kg", "荷兰", "比利时哈瑟尔特", residence = "摩纳哥"),
    210919 to DriverProfileSeed("哈贾尔", "Isack Hadjar", "红牛", "6", "2025年澳大利亚站", "2004-09-28", "167cm", "", "法国", "法国巴黎"),
    210902 to DriverProfileSeed("劳森", "Liam Lawson", "红牛二队", "30", "2023年荷兰站", "2002-02-11", "174cm", "72kg", "新西兰", "新西兰黑斯廷斯"),
    120 to DriverProfileSeed("加斯利", "Pierre Gasly", "Alpine", "10", "2017年马来西亚站", "1996-02-07", "177cm", "70kg", "法国", "法国鲁昂"),
    210918 to DriverProfileSeed("贝尔曼", "Oliver Bearman", "哈斯", "87", "2024年沙特阿拉伯站", "2005-05-08", "184cm", "", "英国", "英国切姆斯福德"),
    210927 to DriverProfileSeed("科拉平托", "Franco Colapinto", "Alpine", "43", "2024年意大利站", "2003-05-27", "176cm", "", "阿根廷", "阿根廷皮拉尔"),
    210938 to DriverProfileSeed("林德布拉德", "Arvid Lindblad", "红牛二队", "41", "", "2007-08-08", "", "", "英国", "英国伦敦"),
    115 to DriverProfileSeed("塞恩斯", "Carlos Sainz", "威廉姆斯", "55", "2015年澳大利亚站", "1994-09-01", "178cm", "66kg", "西班牙", "西班牙马德里"),
    210848 to DriverProfileSeed("阿尔本", "Alexander Albon", "威廉姆斯", "23", "2019年澳大利亚站", "1996-03-23", "186cm", "74kg", "泰国", "英国伦敦"),
    117 to DriverProfileSeed("奥康", "Esteban Ocon", "哈斯", "31", "2016年比利时站", "1996-09-17", "186cm", "66kg", "法国", "法国埃夫勒"),
    210934 to DriverProfileSeed("博托莱托", "Gabriel Bortoleto", "奥迪", "5", "2025年澳大利亚站", "2004-10-14", "184cm", "", "巴西", "巴西圣保罗"),
    112 to DriverProfileSeed("佩雷兹", "Sergio Perez", "凯迪拉克", "11", "2011年澳大利亚站", "1990-01-26", "173cm", "63kg", "墨西哥", "墨西哥瓜达拉哈拉"),
    210807 to DriverProfileSeed("霍肯博格", "Nico Hulkenberg", "奥迪", "27", "2010年巴林站", "1987-08-19", "184cm", "74kg", "德国", "德国埃默里希"),
    109 to DriverProfileSeed("阿隆索", "Fernando Alonso", "阿斯顿马丁", "14", "2001年澳大利亚站", "1981-07-29", "171cm", "68kg", "西班牙", "西班牙奥维耶多"),
    114 to DriverProfileSeed("博塔斯", "Valtteri Bottas", "凯迪拉克", "77", "2013年澳大利亚站", "1989-08-28", "173cm", "69kg", "芬兰", "芬兰纳斯托拉"),
    119 to DriverProfileSeed("斯托尔", "Lance Stroll", "阿斯顿马丁", "18", "2017年澳大利亚站", "1998-10-29", "182cm", "70kg", "加拿大", "加拿大蒙特利尔")
)

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
