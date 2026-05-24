package com.racingdaily.ui.screens.rankings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.racingdaily.data.model.RankingData
import com.racingdaily.data.model.RankingOption
import com.racingdaily.data.remote.ApiService
import kotlinx.serialization.json.JsonObject

@Composable
fun RankingScreen(api: ApiService) {
    var seasons by remember { mutableStateOf<List<RankingOption>>(emptyList()) }
    var selectedSeason by remember { mutableStateOf<RankingOption?>(null) }
    var isDriver by remember { mutableStateOf(true) }
    var data by remember { mutableStateOf<RankingData?>(null) }
    var loading by remember { mutableStateOf(true) }
    var selectedSubTab by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(reloadKey) {
        loading = true
        error = null
        runCatching { api.getRankingNav().list.firstOrNull()?.options.orEmpty() }
            .onSuccess {
                seasons = it
                selectedSeason = it.firstOrNull { option -> option.id == 2026 } ?: it.firstOrNull()
            }
            .onFailure { error = it.message ?: "Unable to load seasons" }
        if (selectedSeason == null) loading = false
    }

    LaunchedEffect(selectedSeason, isDriver, reloadKey) {
        val season = selectedSeason ?: return@LaunchedEffect
        loading = true
        error = null
        runCatching {
            if (isDriver) api.getDriverRanking(season.chp_id, season.id) else api.getTeamRanking(season.chp_id, season.id)
        }.onSuccess {
            data = it
            selectedSubTab = it.list.firstOrNull()?.tab_key.orEmpty()
        }.onFailure {
            error = it.message ?: "Unable to load rankings"
        }
        loading = false
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Text("Rankings", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { listOf("Driver" to true, "Constructor" to false).forEach { (label, d) -> FilterChip(d == isDriver, { isDriver = d }, { Text(label, fontSize = 13.sp, color = if (d == isDriver) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }); Spacer(Modifier.width(8.dp)) } }
        LazyRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) { items(seasons) { s -> val sel = s.id == (selectedSeason?.id ?: -1); FilterChip(sel, { selectedSeason = s }, { Text(s.name, fontSize = 11.sp, color = if (sel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant) }) } }
        data?.list?.let { tabs ->
            LazyRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) { items(tabs) { t -> val sel = t.tab_key == selectedSubTab; SuggestionChip({ selectedSubTab = t.tab_key }, { Text(t.tab_name, fontSize = 10.sp, color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }) } }
            Spacer(Modifier.height(8.dp))
            val tab = tabs.find { it.tab_key == selectedSubTab }
            if (tab != null) LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                itemsIndexed(tab.list) { i, row -> RankingRow(i + 1, row) }
            }
        }
        if (loading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
        else if (error != null) Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Button({ reloadKey++ }) { Text("Retry") }
            }
        }
    }
}

@Composable
fun RankingRow(pos: Int, row: JsonObject) {
    val name = row["driver_abbr_chinese_name"]?.toString()?.removeSurrounding("\"") ?: row["team_abbr_chinese_name"]?.toString()?.removeSurrounding("\"") ?: ""
    val team = row["team_name"]?.toString()?.removeSurrounding("\"") ?: ""
    val pts = row["total_score"]?.toString() ?: row["points"]?.toString() ?: ""
    val avatar = row["driver_avatar"]?.toString()?.removeSurrounding("\"") ?: row["team_logo"]?.toString()?.removeSurrounding("\"")
    Surface(Modifier.fillMaxWidth().padding(vertical = 3.dp), shape = RoundedCornerShape(8.dp), color = if (pos <= 3) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.Transparent) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("$pos", style = MaterialTheme.typography.titleMedium, color = if (pos <= 3) RacingYellow else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(32.dp))
            if (avatar != null) { AsyncImage(avatar, null, Modifier.size(32.dp).clip(CircleShape), contentScale = ContentScale.Fit); Spacer(Modifier.width(8.dp)) }
            Column(Modifier.weight(1f)) { Text(name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface); if (team.isNotEmpty()) Text(team, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text(pts, style = MaterialTheme.typography.titleMedium, color = RacingBlue, fontWeight = FontWeight.Bold)
        }
    }
}

private val RacingYellow = Color(0xFFD29922)
private val RacingBlue = Color(0xFF58A6FF)
