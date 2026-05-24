package com.racingdaily

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
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
            DetailScreen(articleId, { showDetail = false }, api)
        } else {
            Scaffold(
                bottomBar = { NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), tonalElevation = 0.dp) {
                    NavItem(Screen.HOME, "News", rememberNewsIcon(), currentScreen) { currentScreen = Screen.HOME }
                    NavItem(Screen.RACE, "Race", rememberRaceIcon(), currentScreen) { currentScreen = Screen.RACE }
                    NavItem(Screen.RANKINGS, "Rank", rememberRankIcon(), currentScreen) { currentScreen = Screen.RANKINGS }
                    NavItem(Screen.MORE, "More", rememberMoreIcon(), currentScreen) { currentScreen = Screen.MORE }
                } }
            ) { padding ->
                Box(Modifier.padding(padding)) {
                    when (currentScreen) {
                        Screen.HOME -> HomeScreen({ articleId = it; showDetail = true }, api)
                        Screen.RACE -> RaceScreen({ gp, ses -> }, { trackId -> }, api)
                        Screen.RANKINGS -> RankingScreen(api)
                        Screen.MORE -> MoreScreen({ cat, id -> champCategory = cat; champId = id; showChamp = true }, api)
                    }
                }
            }
        }
    }
}

@Composable
fun NavItem(screen: Screen, label: String, icon: ImageVector, current: Screen, onClick: () -> Unit) {
    val sel = current == screen
    NavigationBarItem(sel, onClick, icon = { Icon(icon, label, tint = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }, label = { Text(label, fontSize = 11.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) })
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

fun rememberNewsIcon() = vectorIcon("News") { moveTo(4f, 4f); lineTo(4f, 20f); lineTo(20f, 20f); lineTo(20f, 4f); lineTo(4f, 4f); close(); moveTo(6f, 6f); lineTo(18f, 6f); lineTo(18f, 18f); lineTo(6f, 18f); close(); moveTo(8f, 10f); lineTo(16f, 10f); lineTo(16f, 8f); lineTo(8f, 8f); close(); moveTo(8f, 14f); lineTo(14f, 14f); lineTo(14f, 12f); lineTo(8f, 12f); close() }
fun rememberRaceIcon() = vectorIcon("Race") { moveTo(12f, 2f); curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f); curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f); curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f); curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f); close(); moveTo(12f, 20f); curveTo(7.59f, 20f, 4f, 16.41f, 4f, 12f); curveTo(4f, 7.59f, 7.59f, 4f, 12f, 4f); curveTo(16.41f, 4f, 20f, 7.59f, 20f, 12f); curveTo(20f, 16.41f, 16.41f, 20f, 12f, 20f); close(); moveTo(12f, 6f); lineTo(9f, 17f); lineTo(15f, 12f); lineTo(12f, 6f); close() }
fun rememberRankIcon() = vectorIcon("Rank") { moveTo(6f, 10f); lineTo(6f, 20f); lineTo(10f, 20f); lineTo(10f, 10f); lineTo(6f, 10f); close(); moveTo(10f, 4f); lineTo(10f, 20f); lineTo(14f, 20f); lineTo(14f, 4f); lineTo(10f, 4f); close(); moveTo(14f, 8f); lineTo(14f, 20f); lineTo(18f, 20f); lineTo(18f, 8f); lineTo(14f, 8f); close() }
fun rememberMoreIcon() = vectorIcon("More") { moveTo(12f, 8f); curveTo(13.1f, 8f, 14f, 7.1f, 14f, 6f); curveTo(14f, 4.9f, 13.1f, 4f, 12f, 4f); curveTo(10.9f, 4f, 10f, 4.9f, 10f, 6f); curveTo(10f, 7.1f, 10.9f, 8f, 12f, 8f); close(); moveTo(12f, 14f); curveTo(13.1f, 14f, 14f, 13.1f, 14f, 12f); curveTo(14f, 10.9f, 13.1f, 10f, 12f, 10f); curveTo(10.9f, 10f, 10f, 10.9f, 10f, 12f); curveTo(10f, 13.1f, 10.9f, 14f, 12f, 14f); close(); moveTo(12f, 20f); curveTo(13.1f, 20f, 14f, 19.1f, 14f, 18f); curveTo(14f, 16.9f, 13.1f, 16f, 12f, 16f); curveTo(10.9f, 16f, 10f, 16.9f, 10f, 18f); curveTo(10f, 19.1f, 10.9f, 20f, 12f, 20f); close() }

fun vectorIcon(name: String, block: androidx.compose.ui.graphics.vector.ImageVector.Builder.() -> Unit) = ImageVector.Builder(name, 24.0.dp, 24.0.dp, 24f, 24f).apply { path(fill = SolidColor(Color.White), pathFillType = PathFillType.NonZero, block = block) }.build()
