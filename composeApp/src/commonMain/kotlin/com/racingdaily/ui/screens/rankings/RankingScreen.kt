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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.racingdaily.data.model.RankingData
import com.racingdaily.data.model.RankingOption
import com.racingdaily.data.remote.ApiService
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassChip
import com.racingdaily.ui.components.GlassSurface
import com.racingdaily.ui.components.ScreenHeader
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

@Composable
fun RankingScreen(
    api: ApiService,
    onDriverClick: (chpId: Int, driverId: Int, name: String, avatar: String, teamLogo: String) -> Unit,
    onTeamClick: (chpId: Int, teamId: Int, name: String, logo: String) -> Unit
) {
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
        ScreenHeader("Rankings", selectedSeason?.name?.ifBlank { "Championship standings" } ?: "Championship standings")
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassChip("Driver", selected = isDriver, onClick = { isDriver = true }, leadingIcon = Icons.Rounded.Person)
            GlassChip("Constructor", selected = !isDriver, onClick = { isDriver = false }, leadingIcon = Icons.Rounded.Groups)
        }
        LazyRow(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs) { tab ->
                    GlassChip(
                        label = tab.tab_name.replace("\n", " "),
                        selected = tab.tab_key == selectedSubTab,
                        onClick = { selectedSubTab = tab.tab_key }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            val tab = tabs.find { it.tab_key == selectedSubTab }
            if (tab != null) {
                val remark = tab.remark.cleanRankingRemark()
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    if (remark.isNotBlank()) {
                        item {
                            Text(remark, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    itemsIndexed(tab.list) { index, row ->
                        RankingRow(
                            pos = index + 1,
                            row = row,
                            isDriver = isDriver,
                            chpId = selectedSeason?.chp_id ?: 0,
                            onDriverClick = onDriverClick,
                            onTeamClick = onTeamClick
                        )
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
                    GlassButton({ reloadKey++ }) {
                        Icon(Icons.Rounded.Refresh, null, tint = Color.White)
                        Text("Retry", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingRow(
    pos: Int,
    row: JsonObject,
    isDriver: Boolean,
    chpId: Int,
    onDriverClick: (chpId: Int, driverId: Int, name: String, avatar: String, teamLogo: String) -> Unit,
    onTeamClick: (chpId: Int, teamId: Int, name: String, logo: String) -> Unit
) {
    val name = row.text("driver_abbr_chinese_name").ifBlank { row.text("team_abbr_chinese_name") }
    val team = row.text("team_name").ifBlank { row.text("team_abbr_chinese_name") }
    val pts = row.bestScoreText()
    val avatar = row.text("driver_avatar").ifBlank { row.text("team_logo") }
    val teamLogo = row.text("team_logo")
    val driverId = row.intText("driver_id").ifZero { row.intText("drivers_id") }
    val teamId = row.intText("team_id")

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        selected = pos <= 3,
        onClick = {
            if (isDriver && driverId > 0) {
                onDriverClick(chpId, driverId, name, avatar, teamLogo)
            } else if (!isDriver && teamId > 0) {
                onTeamClick(chpId, teamId, name, avatar)
            }
        },
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$pos",
                style = MaterialTheme.typography.titleMedium,
                color = if (pos <= 3) RacingYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(34.dp),
                fontWeight = FontWeight.Bold
            )
            if (avatar.isNotBlank()) {
                AsyncImage(avatar, null, Modifier.size(42.dp).clip(CircleShape), contentScale = ContentScale.Fit)
                Spacer(Modifier.width(10.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                if (team.isNotEmpty() && team != name) {
                    Text(team, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(pts, style = MaterialTheme.typography.titleMedium, color = RacingBlue, fontWeight = FontWeight.Bold)
        }
    }
}

private fun JsonObject.text(key: String): String =
    (this[key] as? JsonPrimitive)?.contentOrNull.orEmpty()

private fun JsonObject.intText(key: String): Int {
    val primitive = this[key] as? JsonPrimitive ?: return 0
    return primitive.intOrNull ?: primitive.content.toIntOrNull() ?: 0
}

private fun JsonObject.bestScoreText(): String {
    val keys = listOf(
        "total_score", "points", "gp_p1_cnt", "gp_pole_cnt", "gp_fastlap_cnt",
        "gp_q_avg_rank_percent", "gp_race_avg_rank_percent", "use_time", "display_order"
    )
    keys.forEach { key ->
        val value = text(key)
        if (value.isNotBlank()) return value
    }
    return "-"
}

private fun String.cleanRankingRemark(): String =
    replace("\\n", "\n")
        .lines()
        .joinToString("\n") { it.trim() }
        .trim()

private inline fun Int.ifZero(block: () -> Int): Int = if (this == 0) block() else this

private val RacingYellow = Color(0xFFD29922)
private val RacingBlue = Color(0xFF58A6FF)
