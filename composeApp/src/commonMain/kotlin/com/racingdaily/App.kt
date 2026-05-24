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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.racingdaily.data.model.ChampSeason
import com.racingdaily.data.model.TrackInfo
import com.racingdaily.data.remote.ApiService
import com.racingdaily.ui.components.GlassBackdropHost
import com.racingdaily.ui.components.GlassBottomBar
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassNavTab
import com.racingdaily.ui.components.GlassSurface
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
        GlassBackdropHost {
            var currentScreen by remember { mutableStateOf(Screen.HOME) }
            var articleId by remember { mutableIntStateOf(0) }
            var showDetail by remember { mutableStateOf(false) }
            var champCategory by remember { mutableStateOf("custom") }
            var champId by remember { mutableIntStateOf(0) }
            var showChamp by remember { mutableStateOf(false) }
            var trackId by remember { mutableIntStateOf(0) }
            var showTrack by remember { mutableStateOf(false) }

            when {
                showChamp -> ChampScreen(champCategory, champId, { showChamp = false }, api)
                showTrack -> TrackScreen(trackId, { showTrack = false }, api)
                showDetail -> DetailScreen(articleId, { showDetail = false }, api)
                else -> Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        GlassBottomBar(
                            tabs = listOf(
                                GlassNavTab(Screen.HOME, "N", "News"),
                                GlassNavTab(Screen.RACE, "R", "Race"),
                                GlassNavTab(Screen.RANKINGS, "P", "Rank"),
                                GlassNavTab(Screen.MORE, "+", "More")
                            ),
                            selected = currentScreen,
                            onSelected = { currentScreen = it }
                        )
                    }
                ) { padding ->
                    Box(Modifier.padding(padding)) {
                        when (currentScreen) {
                            Screen.HOME -> HomeScreen({ articleId = it; showDetail = true }, api)
                            Screen.RACE -> RaceScreen({ _, _ -> }, { trackId = it; showTrack = true }, api)
                            Screen.RANKINGS -> RankingScreen(api)
                            Screen.MORE -> MoreScreen(
                                { cat, id ->
                                    champCategory = cat
                                    champId = id
                                    showChamp = true
                                },
                                api
                            )
                        }
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

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GlassButton(onBack) { Text("< Back", color = Color.White) }
            Spacer(Modifier.width(12.dp))
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
                    GlassButton({ reloadKey++ }) { Text("Retry", color = Color.White) }
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
        Row(Modifier.padding(12.dp)) {
            GlassButton(onBack) { Text("< Back", color = Color.White) }
        }
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (error != null) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    GlassButton({ reloadKey++ }) { Text("Retry", color = Color.White) }
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

private fun JsonObject.text(key: String): String =
    (this[key] as? JsonPrimitive)?.contentOrNull.orEmpty()
