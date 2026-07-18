package com.racingdaily

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.racingdaily.data.model.ChampSeason
import com.racingdaily.data.model.DriverInfoData
import com.racingdaily.data.model.DriverInfoTitle
import com.racingdaily.data.model.NewsItem
import com.racingdaily.data.model.RaceGp
import com.racingdaily.data.model.RaceSession
import com.racingdaily.data.model.RankingData
import com.racingdaily.data.model.TeamInfoData
import com.racingdaily.data.model.TeamCarInfo
import com.racingdaily.data.model.TeamDriverInfo
import com.racingdaily.data.model.TeamInfoTab
import com.racingdaily.data.model.TeamNamedInfo
import com.racingdaily.data.model.TeamPeopleData
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
import com.racingdaily.ui.components.TeamLogo
import com.racingdaily.ui.components.pureRacingBackground
import com.racingdaily.ui.screens.detail.DetailScreen
import com.racingdaily.ui.screens.home.HomeScreen
import com.racingdaily.ui.screens.more.MoreScreen
import com.racingdaily.ui.screens.race.RaceScreen
import com.racingdaily.ui.screens.rankings.RankingScreen
import com.racingdaily.ui.screens.search.SearchScreen
import com.racingdaily.ui.theme.RacingDailyTheme
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

enum class Screen { HOME, RACE, RANKINGS, MORE }

sealed interface AppPage {
    data object Search : AppPage
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
                                GlassNavTab(Screen.HOME, Icons.AutoMirrored.Rounded.Article, "新闻"),
                                GlassNavTab(Screen.RACE, Icons.Rounded.CalendarMonth, "赛事"),
                                GlassNavTab(Screen.RANKINGS, Icons.Rounded.EmojiEvents, "排名"),
                                GlassNavTab(Screen.MORE, Icons.Rounded.MoreHoriz, "更多")
                            ),
                            selected = currentScreen,
                            onSelected = { currentScreen = it }
                        )
                    }
                ) {
                    Box(Modifier.fillMaxSize()) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                val forward = targetState.ordinal > initialState.ordinal
                                val enterOffset: (Int) -> Int = { width -> if (forward) width / 18 else -width / 18 }
                                val exitOffset: (Int) -> Int = { width -> if (forward) -width / 18 else width / 18 }
                                (fadeIn(spring(dampingRatio = 1f, stiffness = 620f)) +
                                    scaleIn(
                                        animationSpec = spring(dampingRatio = 1f, stiffness = 520f),
                                        initialScale = 0.99f
                                    ) + slideInHorizontally(
                                    animationSpec = spring(dampingRatio = 1f, stiffness = 460f),
                                    initialOffsetX = enterOffset
                                )) togetherWith (fadeOut(spring(dampingRatio = 1f, stiffness = 680f)) +
                                    scaleOut(
                                        animationSpec = spring(dampingRatio = 1f, stiffness = 560f),
                                        targetScale = 0.99f
                                    ) + slideOutHorizontally(
                                    animationSpec = spring(dampingRatio = 1f, stiffness = 480f),
                                    targetOffsetX = exitOffset
                                ))
                            },
                            label = "Main screen transition"
                        ) { screen ->
                            when (screen) {
                                Screen.HOME -> HomeScreen(
                                    onArticleClick = { item ->
                                        pageStack += AppPage.Article(item.id, item.title, item.http_url)
                                    },
                                    onSearchClick = {
                                        pageStack += AppPage.Search
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
                }

                PageStackHost(pageStack.toList()) { page, pageVisible ->
                    when (page) {
                        is AppPage.Search -> SearchScreen(
                            onBack = goBack,
                            onArticleClick = { item ->
                                pageStack += AppPage.Article(item.id, item.title, item.http_url)
                            },
                            api = api
                        )
                        is AppPage.Championship -> ChampScreen(page.category, page.id, goBack, api)
                        is AppPage.Track -> TrackScreen(page.id, goBack, api)
                        is AppPage.Article -> DetailScreen(
                            articleId = page.id,
                            initialTitle = page.title,
                            initialUrl = page.url,
                            onBack = goBack,
                            api = api,
                            pageVisible = pageVisible
                        )
                        is AppPage.RaceDetail -> RaceDetailScreen(page.gp, goBack)
                        is AppPage.DriverDetail -> DriverDetailScreen(page, goBack, api) { item ->
                            pageStack += AppPage.Article(item.id, item.title, item.http_url)
                        }
                        is AppPage.TeamDetail -> TeamDetailScreen(page, goBack, api) { item ->
                            pageStack += AppPage.Article(item.id, item.title, item.http_url)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageStackHost(
    pages: List<AppPage>,
    content: @Composable (AppPage, Boolean) -> Unit
) {
    val renderedPages = remember { mutableStateListOf<AppPage>() }

    LaunchedEffect(pages) {
        pages.forEach { page ->
            if (page !in renderedPages) renderedPages += page
        }
    }

    renderedPages.forEachIndexed { index, page ->
        AppPageOverlay(
            pageKey = page,
            visible = page in pages,
            modifier = Modifier.zIndex(index + 1f),
            onHidden = { renderedPages.remove(page) }
        ) {
            content(page, page in pages)
        }
    }
}

@Composable
private fun AppPageOverlay(
    pageKey: AppPage,
    visible: Boolean,
    modifier: Modifier = Modifier,
    onHidden: () -> Unit,
    content: @Composable () -> Unit
) {
    val transitionState = remember(pageKey) { MutableTransitionState(false) }

    LaunchedEffect(visible) {
        transitionState.targetState = visible
    }

    LaunchedEffect(transitionState.isIdle, transitionState.currentState, transitionState.targetState) {
        if (transitionState.isIdle && !transitionState.currentState && !transitionState.targetState) {
            onHidden()
        }
    }

    AnimatedVisibility(
        visibleState = transitionState,
        modifier = modifier.fillMaxSize(),
        enter = fadeIn(spring(dampingRatio = 1f, stiffness = 600f)) +
            scaleIn(
                animationSpec = spring(dampingRatio = 1f, stiffness = 430f),
                initialScale = 0.98f
            ) + slideInHorizontally(
                animationSpec = spring(dampingRatio = 0.9f, stiffness = 430f)
            ) { it / 6 },
        exit = fadeOut(spring(dampingRatio = 1f, stiffness = 680f)) +
            scaleOut(
                animationSpec = spring(dampingRatio = 1f, stiffness = 500f),
                targetScale = 0.985f
            ) + slideOutHorizontally(
                animationSpec = spring(dampingRatio = 1f, stiffness = 500f)
            ) { it / 6 }
    ) {
        Box(
            Modifier
                .fillMaxSize()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .graphicsLayer { alpha = 1f }
                    .pureRacingBackground()
            ) {
                content()
            }
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
            error = it.message ?: "无法加载赛道信息"
        }
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = track?.chinese_name?.ifBlank { track?.name.orEmpty() } ?: "赛道",
            subtitle = listOf(track?.country, track?.location).filterNot { it.isNullOrBlank() }.joinToString(" / "),
            navigationIcon = {
                GlassIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "返回", onBack)
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
                        Text("重试", color = Color.White)
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
                    Text("历史纪录", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
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
            error = it.message ?: "无法加载锦标赛"
        }
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("锦标赛", "赛季积分榜", navigationIcon = {
            GlassIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "返回", onBack)
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
                        Text("重试", color = Color.White)
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
        ScreenHeader(gp.gp_name.ifBlank { "赛事" }, gp.track_name, navigationIcon = {
            GlassIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "返回", onBack)
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
                Text("暂无比赛结果", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            } else {
                session.race_result.take(10).forEach { result ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("${result.rank}", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp))
                        AsyncImage(result.team_logo, null, Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(result.dr_name, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        Text(
                            result.gap.ifBlank { result.score_p.takeIf { it > 0 }?.let { "$it 分" }.orEmpty() },
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
fun DriverDetailScreen(
    page: AppPage.DriverDetail,
    onBack: () -> Unit,
    api: ApiService,
    onArticleClick: (NewsItem) -> Unit
) {
    var selectedTab by rememberSaveable(page.driverId) { mutableStateOf("info") }
    var driverInfo by remember(page.driverId) { mutableStateOf<DriverInfoData?>(null) }
    var photos by remember(page.driverId) { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember(page.driverId) { mutableStateOf(true) }
    var error by remember(page.driverId) { mutableStateOf<String?>(null) }
    var seasonScores by remember(page.driverId) { mutableStateOf<List<DriverSeasonScore>>(emptyList()) }
    var scoreLoading by remember(page.driverId) { mutableStateOf(true) }
    var scoreError by remember(page.driverId) { mutableStateOf<String?>(null) }

    LaunchedEffect(page.driverId, page.chpId, page.seasonId) {
        loading = true
        error = null
        runCatching {
            val info = api.getDriverInfo(page.chpId, page.driverId, page.seasonId)
            val photoData = runCatching { api.getDriverPhoto(page.chpId, page.driverId) }.getOrNull()
            info to photoData?.img.orEmpty()
        }.onSuccess { (info, loadedPhotos) ->
            driverInfo = info
            photos = loadedPhotos
            if (selectedTab !in info.visibleTabs().map { it.first }) {
                selectedTab = info.visibleTabs().firstOrNull()?.first ?: "info"
            }
        }.onFailure {
            error = it.message ?: "无法加载车手资料"
        }
        loading = false
    }

    LaunchedEffect(page.driverId, page.chpId, page.seasonId) {
        scoreLoading = true
        scoreError = null
        runCatching {
            val seasons = api.getRankingNav()
                .list
                .flatMap { it.options }
                .map { it.id }
                .filter { it in 1950..page.seasonId }
                .distinct()
                .sorted()
            seasons.mapNotNull { seasonId ->
                runCatching {
                    api.getDriverRanking(page.chpId, seasonId).toDriverSeasonScore(seasonId, page.driverId)
                }.getOrNull()
            }.sortedByDescending { it.season }
        }.onSuccess {
            seasonScores = it
        }.onFailure {
            scoreError = it.message ?: "无法加载历年成绩"
        }
        scoreLoading = false
    }

    val title = driverInfo?.title
    val displayName = title?.addr_chinese_name?.ifBlank { page.name } ?: page.name
    val englishName = title?.name.orEmpty()
    val teamName = title?.chinese_name?.ifBlank { page.stats.text("team_abbr_chinese_name") } ?: page.stats.text("team_abbr_chinese_name")
    val number = title?.number?.ifBlank { page.stats.text("number") } ?: page.stats.text("number")
    val avatar = title?.avatar?.ifBlank { page.avatar } ?: page.avatar
    val tabs = driverInfo?.visibleTabs()?.takeIf { it.isNotEmpty() } ?: listOf("info" to "资料", "score" to "成绩", "news" to "新闻")

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(displayName, "车手资料", navigationIcon = {
            GlassIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "返回", onBack)
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
                            avatar,
                            null,
                            Modifier.size(96.dp).clip(CircleShape),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(displayName, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium)
                            Text(englishName.ifBlank { "车手资料" }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                listOf(teamName, number.takeIf { it.isNotBlank() }?.let { "#$it" })
                                    .filterNotNull()
                                    .filter { it.isNotBlank() }
                                    .joinToString(" "),
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        AsyncImage(title?.nationality_img?.ifBlank { page.teamLogo } ?: page.teamLogo, null, Modifier.size(42.dp))
                    }
                }
            }
            item {
                ProfileTabs(tabs = tabs, selected = selectedTab, onSelected = { selectedTab = it })
            }
            if (loading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            error?.let {
                item { EmptyProfileCard("接口提示", it) }
            }
            when (selectedTab) {
                "info" -> {
                    item { InfoTable("基本资料", title.driverInfoRows(page)) }
                    title?.helmet?.takeIf { it.isNotBlank() }?.let { helmet ->
                        item { DriverImageStrip("头盔", listOf(helmet)) }
                    }
                    photos.takeIf { it.isNotEmpty() }?.let {
                        item { DriverImageStrip("照片", it.take(6)) }
                    }
                }
                "score" -> item {
                    DriverSeasonScoresCard(
                        scores = seasonScores,
                        loading = scoreLoading,
                        error = scoreError,
                        fallbackStats = page.stats
                    )
                }
                "news" -> {
                    val news = driverInfo?.list.orEmpty()
                    if (news.isEmpty()) {
                        item { EmptyProfileCard("新闻", "暂无相关新闻数据") }
                    } else {
                        items(news.size) { index ->
                            DriverNewsCard(news[index], onArticleClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamDetailScreen(
    page: AppPage.TeamDetail,
    onBack: () -> Unit,
    api: ApiService,
    onArticleClick: (NewsItem) -> Unit
) {
    var teamInfo by remember(page.teamId) { mutableStateOf<TeamInfoData?>(null) }
    var teamNews by remember(page.teamId) { mutableStateOf<List<NewsItem>>(emptyList()) }
    var seasonScores by remember(page.teamId) { mutableStateOf<List<TeamSeasonScore>>(emptyList()) }
    var loading by remember(page.teamId) { mutableStateOf(true) }
    var error by remember(page.teamId) { mutableStateOf<String?>(null) }
    var scoreLoading by remember(page.teamId) { mutableStateOf(true) }
    var scoreError by remember(page.teamId) { mutableStateOf<String?>(null) }
    var selectedTab by rememberSaveable(page.teamId) { mutableStateOf("info") }

    LaunchedEffect(page.chpId, page.seasonId, page.teamId) {
        loading = true
        error = null
        runCatching {
            val officialInfo = runCatching { api.getTeamInfo(page.chpId, page.teamId, page.seasonId) }
            val info = officialInfo.getOrNull()
                ?.takeIf { it.hasUsableOfficialTeamInfo() }
                ?: page.cadillacFallbackTeamInfo()
            val news = teamNewsTagIds[page.teamId]
                ?.let { tagId -> runCatching { api.getNewsList(tagId) }.getOrNull()?.list }
                .orEmpty()
            info to (news to officialInfo.exceptionOrNull())
        }
            .onSuccess { (info, newsAndError) ->
                val (news, officialError) = newsAndError
                teamInfo = info
                teamNews = news
                error = if (info == null) {
                    officialError?.message ?: "无法加载车队资料"
                } else {
                    officialError?.message?.takeIf { page.cadillacFallbackTeamInfo() != null }
                }
            }
            .onFailure { error = it.message ?: "无法加载车队资料" }
        loading = false
    }

    LaunchedEffect(page.chpId, page.seasonId, page.teamId) {
        scoreLoading = true
        scoreError = null
        runCatching {
            val seasons = api.getRankingNav()
                .list
                .flatMap { it.options }
                .map { it.id }
                .filter { it in 1950..page.seasonId }
                .distinct()
                .sorted()
            seasons.mapNotNull { seasonId ->
                runCatching {
                    api.getTeamRanking(page.chpId, seasonId).toTeamSeasonScore(seasonId, page.teamId)
                }.getOrNull()
            }.sortedByDescending { it.season }
        }.onSuccess {
            seasonScores = it
        }.onFailure {
            scoreError = it.message ?: "无法加载车队历年成绩"
        }
        scoreLoading = false
    }

    val title = teamInfo?.chinese_name?.ifBlank { teamInfo?.name.orEmpty() }?.ifBlank { page.name } ?: page.name
    val subtitle = teamInfo?.name?.ifBlank { "车队资料" } ?: "车队资料"

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title, subtitle, navigationIcon = {
            GlassIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "返回", onBack)
        })
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TeamLogo(
                            teamName = title,
                            url = teamInfo?.logo?.ifBlank { page.logo } ?: page.logo,
                            modifier = Modifier.size(74.dp)
                        )
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
                    val isCadillac = page.teamId == cadillacTeamId
                    GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
                        AsyncImage(
                            photo,
                            null,
                            Modifier
                                .fillMaxWidth()
                                .height(168.dp)
                                .padding(if (isCadillac) 16.dp else 0.dp),
                            contentScale = if (isCadillac) ContentScale.Fit else ContentScale.Crop,
                            alignment = Alignment.Center
                        )
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
                item {
                    TeamSeasonScoresCard(
                        scores = seasonScores,
                        loading = scoreLoading,
                        error = scoreError,
                        fallbackStats = page.stats
                    )
                }
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
                if (teamNews.isEmpty()) {
                    item { EmptyProfileCard("新闻", "暂无相关新闻数据") }
                } else {
                    items(teamNews.size) { index ->
                        DriverNewsCard(teamNews[index], onArticleClick)
                    }
                }
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
private fun DriverImageStrip(title: String, images: List<String>) {
    GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(images.size) { index ->
                    AsyncImage(
                        images[index],
                        null,
                        Modifier.width(132.dp).height(92.dp).clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverNewsCard(item: NewsItem, onArticleClick: (NewsItem) -> Unit) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onArticleClick(item) },
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val cover = item.covers.firstOrNull()?.path_url.orEmpty()
            if (cover.isNotBlank()) {
                AsyncImage(
                    cover,
                    null,
                    Modifier.width(112.dp).height(86.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(Modifier.weight(1f).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    item.tags.firstOrNull()?.name?.takeIf { it.isNotBlank() }?.let {
                        Text(it, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelMedium)
                    }
                    Text("${item.total_read} 次阅读", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
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
private fun TeamSeasonScoresCard(
    scores: List<TeamSeasonScore>,
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
                    DriverSeasonScoreRow(listOf("赛季", "名次", "总积分"), isHeader = true)
                    scores.forEach { score ->
                        DriverSeasonScoreRow(
                            listOf(
                                score.season.toString(),
                                score.rank.asScoreText(),
                                score.totalScore.asScoreText()
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
    1 -> "已结束"
    2 -> "直播中"
    3 -> "未开始"
    else -> "状态 $race_status"
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

private data class TeamSeasonScore(
    val season: Int,
    val rank: Int,
    val totalScore: Int
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

private fun RankingData.toTeamSeasonScore(season: Int, teamId: Int): TeamSeasonScore? {
    val total = list.firstOrNull { it.tab_key == "total_score" }
        ?.list
        ?.firstOrNull { row -> row.intValue("team_id") == teamId }
        ?: return null
    return TeamSeasonScore(
        season = season,
        rank = total.intValue("display_order"),
        totalScore = total.intValue("total_score")
    )
}

private fun Int.asScoreText(): String = if (this == 0) "0" else toString()

private inline fun Int.ifZero(block: () -> Int): Int = if (this == 0) block() else this

private fun DriverInfoData.visibleTabs(): List<Pair<String, String>> =
    title.tab.mapNotNull { tab ->
        val value = tab.value.ifBlank { return@mapNotNull null }
        val text = tab.text.ifBlank { value }
        value to text
    }

private fun DriverInfoTitle?.driverInfoRows(page: AppPage.DriverDetail): List<Pair<String, String>> {
    if (this == null) {
        return listOf(
            "中文名" to page.name,
            "车队" to page.stats.text("team_abbr_chinese_name").ifBlank { page.stats.text("team_name") }
        )
    }
    return listOf(
        "英文名" to name,
        "中文名" to addr_chinese_name,
        "状态" to status.driverStatusText(),
        "年龄" to birthday,
        "身高" to height.withUnit("cm"),
        "车队" to chinese_name,
        "车手号码" to number,
        "国籍" to nationality
    )
}

private fun String.withUnit(unit: String): String =
    if (isBlank() || endsWith(unit)) this else "$this$unit"

private fun Int.driverStatusText(): String = when (this) {
    1 -> "现役"
    2 -> "退役"
    else -> ""
}

private val teamNewsTagIds = mapOf(
    79 to 32,
    80 to 10,
    81 to 7,
    82 to 33,
    83 to 27,
    84 to 35,
    85 to 26,
    86 to 34,
    87 to 30,
    88 to 31,
    210212 to 234
)

private const val cadillacTeamId = 210212
private const val cadillacLogo = "https://oss.static.romielf.com/icon/F1/2026/cadillac.png"
private const val cadillacCarPhoto = "https://media.formula1.com/image/upload/c_lfill%2Cw_3392/q_auto/v1740000001/common/f1/2026/cadillac/2026cadillaccarright.webp"

private fun TeamInfoData.hasUsableOfficialTeamInfo(): Boolean =
    id > 0 || name.isNotBlank() || chinese_name.isNotBlank() || drivers.driver.isNotEmpty() || car.isNotEmpty()

private fun AppPage.TeamDetail.cadillacFallbackTeamInfo(): TeamInfoData? {
    if (teamId != cadillacTeamId) return null

    return TeamInfoData(
        id = cadillacTeamId,
        name = "Cadillac Formula 1 Team",
        chinese_name = "凯迪拉克F1车队",
        logo = logo.ifBlank { cadillacLogo },
        address = "英国，银石",
        factory = "Silverstone, United Kingdom",
        first_entry = "2026",
        fleet_type = "厂队（通用汽车 / TWG Motorsports）",
        supplier = "Ferrari",
        chassis = "MAC-26",
        power_unit = "Ferrari",
        information = "凯迪拉克作为通用汽车和 TWG Motorsports 支持的新车队，于 2026 赛季加入 F1。",
        photo = cadillacCarPhoto,
        tab = listOf(
            TeamInfoTab("资料", "info"),
            TeamInfoTab("成绩", "score"),
            TeamInfoTab("车", "car"),
            TeamInfoTab("新闻", "news")
        ),
        drivers = TeamPeopleData(
            driver = listOf(
                TeamDriverInfo(
                    driver_id = 112,
                    avatar = "https://oss.static.romielf.com/uploads/20260305/e496321652185f37bf75000d9b95ca17.png",
                    addr_chinese_name = "佩雷兹",
                    number = "11"
                ),
                TeamDriverInfo(
                    driver_id = 114,
                    avatar = "https://oss.static.romielf.com/uploads/20260305/5462fe4f0aae630fdda6eb1ce98528d0.png",
                    addr_chinese_name = "博塔斯",
                    number = "77"
                )
            ),
            worker = listOf(
                TeamWorkerInfo(addr_chinese_name = "格雷姆·劳登", position = "车队领队"),
                TeamWorkerInfo(addr_chinese_name = "尼克·切斯特", position = "技术负责人")
            ),
            test = listOf(
                TeamNamedInfo(name = "周冠宇", position = "储备车手"),
                TeamNamedInfo(name = "科尔顿·赫塔", position = "测试车手")
            )
        ),
        car = listOf(
            TeamCarInfo(
                season_id = 2026,
                chassis = "MAC-26",
                power_unit = "Ferrari",
                photo = listOf(cadillacCarPhoto),
                is_now = 1
            )
        )
    )
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
        "基地" to address,
        "总部地址" to factory,
        "历史名称" to history,
        "车队类型" to fleet_type,
        "预算" to budget,
        "动力单元" to power_unit.ifBlank { supplier },
        "底盘" to chassis,
        "风洞时间" to wind_tunnel,
        "简介" to information.ifBlank { teamIntroFallback(page.teamId, chinese_name.ifBlank { page.name }) }
    ).filter { (_, value) -> value.isNotBlank() }
}

private fun teamIntroFallback(teamId: Int, fallbackName: String): String = when (teamId) {
    79 -> "奥迪车队以欣维尔为基地，由索伯体系过渡而来，并在 2026 赛季以奥迪厂队身份参加 F1。"
    80 -> "法拉利是 F1 历史最悠久的车队，总部位于马拉内罗，是唯一从世界锦标赛创立初期延续至今的参赛车队。"
    81 -> "梅赛德斯AMG马石油F1车队以布拉克利和布里克斯沃斯为核心基地，长期以厂队身份研发底盘和动力单元。"
    82 -> "阿斯顿马丁F1车队以银石为基地，依托阿斯顿马丁品牌和阿美等合作伙伴持续扩建技术设施。"
    83 -> "迈凯伦F1车队总部位于沃金，是 F1 传统强队之一，长期以独立车队身份参与顶级方程式竞争。"
    84 -> "威廉姆斯F1车队以格罗夫为基地，是 F1 经典英国车队之一，拥有深厚的工程传统和冠军历史。"
    85 -> "红牛车队以米尔顿凯恩斯为基地，是现代 F1 最具竞争力的车队之一，以空气动力学和整体运营能力著称。"
    86 -> "哈斯F1车队是美国背景的 F1 车队，运营体系横跨美国、英国和意大利，并与法拉利保持技术合作。"
    87 -> "红牛二队以法恩扎为基地，是红牛体系内培养和使用年轻车手的重要 F1 车队。"
    88 -> "Alpine F1车队以恩斯通为底盘基地，代表雷诺集团旗下 Alpine 品牌参加 F1。"
    cadillacTeamId -> "凯迪拉克作为通用汽车和 TWG Motorsports 支持的新车队，于 2026 赛季加入 F1。"
    else -> fallbackName.takeIf { it.isNotBlank() }?.let { "$it 是当前 F1 参赛车队之一。" }.orEmpty()
}
