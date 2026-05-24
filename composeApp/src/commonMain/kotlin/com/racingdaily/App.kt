package com.racingdaily

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.racingdaily.data.model.ChampSeason
import com.racingdaily.data.model.TrackInfo
import com.racingdaily.data.remote.ApiService
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

@Composable
fun App(api: ApiService) {
    RacingDailyTheme {
        var currentScreen by remember { mutableStateOf(Screen.HOME) }
        var articleId by remember { mutableIntStateOf(0) }
        var showDetail by remember { mutableStateOf(false) }
        var champCategory by remember { mutableStateOf("custom") }
        var champId by remember { mutableIntStateOf(0) }
        var showChamp by remember { mutableStateOf(false) }
        var trackId by remember { mutableIntStateOf(0) }
        var showTrack by remember { mutableStateOf(false) }

        if (showChamp) {
            ChampScreen(champCategory, champId, { showChamp = false }, api)
        } else if (showTrack) {
            TrackScreen(trackId, { showTrack = false }, api)
        } else if (showDetail) {
            DetailScreen(articleId, { showDetail = false }, api)
        } else {
            Scaffold(
                bottomBar = {
                    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), tonalElevation = 2.dp) {
                        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            listOf(Screen.HOME to "📰\nNews", Screen.RACE to "🏁\nRace", Screen.RANKINGS to "🏆\nRank", Screen.MORE to "⋮\nMore").forEach { (s, t) ->
                                val sel = currentScreen == s
                                TextButton({ currentScreen = s }) {
                                    Text(t, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            ) { padding ->
                Box(Modifier.padding(padding)) {
                    when (currentScreen) {
                        Screen.HOME -> HomeScreen({ articleId = it; showDetail = true }, api)
                        Screen.RACE -> RaceScreen({ _, _ -> }, { trackId = it; showTrack = true }, api)
                        Screen.RANKINGS -> RankingScreen(api)
                        Screen.MORE -> MoreScreen({ cat, id -> champCategory = cat; champId = id; showChamp = true }, api)
                    }
                }
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
            error = it.message ?: "Unable to load track"
        }
        loading = false
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onBack) { Text("< Back", color = MaterialTheme.colorScheme.secondary) }
            Text(
                track?.chinese_name?.ifBlank { track?.name.orEmpty() } ?: "Track",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
        }
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Button({ reloadKey++ }) { Text("Retry") }
                }
            }
            else -> LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                item {
                    track?.map_img?.takeIf { it.isNotBlank() }?.let {
                        AsyncImage(it, null, Modifier.fillMaxWidth().height(220.dp))
                        Spacer(Modifier.height(12.dp))
                    }
                    Text(track?.name.orEmpty(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        listOf(track?.country, track?.location).filterNot { it.isNullOrBlank() }.joinToString(" / "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("History", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                }
                items(history.take(20).size) { index ->
                    val row = history[index]
                    Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp), color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.padding(12.dp)) { TextButton(onBack) { Text("< Back", color = MaterialTheme.colorScheme.secondary) } }
        if (loading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else if (error != null) Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Button({ reloadKey++ }) { Text("Retry") }
            }
        }
        else data?.let { d ->
            Column(Modifier.padding(16.dp)) {
                Text(d.remark, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                d.tr_data.forEach { row ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        row.forEach { cell ->
                            when (cell.type) { 1 -> AsyncImage(cell.content, null, Modifier.size(24.dp)); else -> Text(cell.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

private fun JsonObject.text(key: String): String =
    (this[key] as? JsonPrimitive)?.contentOrNull.orEmpty()
