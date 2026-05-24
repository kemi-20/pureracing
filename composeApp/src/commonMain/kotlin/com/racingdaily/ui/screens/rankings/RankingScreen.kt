package com.racingdaily.ui.screens.rankings

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.racingdaily.data.model.RankingData
import com.racingdaily.data.model.RankingOption
import com.racingdaily.data.remote.ApiService
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassChip
import com.racingdaily.ui.components.GlassSurface
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

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

    Column(Modifier.fillMaxSize()) {
        Text(
            "Rankings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassChip("Driver", selected = isDriver, onClick = { isDriver = true })
            GlassChip("Constructor", selected = !isDriver, onClick = { isDriver = false })
        }
        LazyRow(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(seasons) { season ->
                GlassChip(
                    label = season.name,
                    selected = season.id == (selectedSeason?.id ?: -1),
                    onClick = { selectedSeason = season }
                )
            }
        }
        data?.list?.let { tabs ->
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(tabs) { tab ->
                    GlassChip(
                        label = tab.tab_name,
                        selected = tab.tab_key == selectedSubTab,
                        onClick = { selectedSubTab = tab.tab_key }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            val tab = tabs.find { it.tab_key == selectedSubTab }
            if (tab != null) {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(tab.list) { index, row ->
                        RankingRow(index + 1, row)
                    }
                }
            }
        }
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (error != null) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    GlassButton({ reloadKey++ }) { Text("Retry", color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun RankingRow(pos: Int, row: JsonObject) {
    val name = row.text("driver_abbr_chinese_name").ifBlank { row.text("team_abbr_chinese_name") }
    val team = row.text("team_name")
    val pts = row.text("total_score").ifBlank { row.text("points") }
    val avatar = row.text("driver_avatar").ifBlank { row.text("team_logo") }

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        selected = pos <= 3,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$pos",
                style = MaterialTheme.typography.titleMedium,
                color = if (pos <= 3) RacingYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(32.dp)
            )
            if (avatar.isNotBlank()) {
                AsyncImage(avatar, null, Modifier.size(32.dp).clip(CircleShape), contentScale = ContentScale.Fit)
                Spacer(Modifier.width(8.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                if (team.isNotEmpty()) {
                    Text(team, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(pts, style = MaterialTheme.typography.titleMedium, color = RacingBlue, fontWeight = FontWeight.Bold)
        }
    }
}

private fun JsonObject.text(key: String): String =
    (this[key] as? JsonPrimitive)?.contentOrNull.orEmpty()

private val RacingYellow = Color(0xFFD29922)
private val RacingBlue = Color(0xFF58A6FF)
