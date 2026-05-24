package com.racingdaily

import androidx.compose.foundation.background
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
import com.racingdaily.data.remote.ApiService
import com.racingdaily.ui.screens.detail.DetailScreen
import com.racingdaily.ui.screens.home.HomeScreen
import com.racingdaily.ui.screens.more.MoreScreen
import com.racingdaily.ui.screens.race.RaceScreen
import com.racingdaily.ui.screens.rankings.RankingScreen
import com.racingdaily.ui.theme.RacingDailyTheme
import kotlinx.coroutines.launch

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

        if (showChamp) {
            ChampScreen(champCategory, champId, { showChamp = false }, api)
        } else if (showDetail) {
            DetailScreen(articleId, { showDetail = false })
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
                        Screen.RACE -> RaceScreen({ _, _ -> }, { _ -> }, api)
                        Screen.RANKINGS -> RankingScreen(api)
                        Screen.MORE -> MoreScreen({ cat, id -> champCategory = cat; champId = id; showChamp = true }, api)
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
    val scope = rememberCoroutineScope()
    LaunchedEffect(category, id) { scope.launch { runCatching { when (category) { "motogp" -> api.getMotogpDriver(id); "tcr" -> api.getTcrDriver(id); else -> api.getCustomDriver(id) } }.onSuccess { data = it }; loading = false } }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.padding(12.dp)) { TextButton(onBack) { Text("< Back", color = MaterialTheme.colorScheme.secondary) } }
        if (loading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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
