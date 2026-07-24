package com.racingdaily.ui.screens.rankings

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
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
import com.racingdaily.ui.components.SectionLabel
import com.racingdaily.ui.components.TeamLogo
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

@Composable
fun RankingScreen(
    api: ApiService,
    onDriverClick: (chpId: Int, seasonId: Int, driverId: Int, name: String, avatar: String, teamLogo: String, stats: JsonObject) -> Unit,
    onTeamClick: (chpId: Int, seasonId: Int, teamId: Int, name: String, logo: String, stats: JsonObject) -> Unit
) {
    var seasons by remember { mutableStateOf<List<RankingOption>>(emptyList()) }
    var selectedSeason by remember { mutableStateOf<RankingOption?>(null) }
    var isDriver by remember { mutableStateOf(true) }
    var data by remember { mutableStateOf<RankingData?>(null) }
    var loading by remember { mutableStateOf(true) }
    var selectedSubTab by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var hasPlayedPodiumShine by rememberSaveable { mutableStateOf(false) }
    val podiumShine = remember {
        Animatable(if (hasPlayedPodiumShine) StaticPodiumShine else InitialPodiumShine)
    }

    LaunchedEffect(reloadKey) {
        loading = true
        error = null
        runCatching { api.getRankingNav().list.firstOrNull()?.options.orEmpty() }
            .onSuccess {
                seasons = it
                selectedSeason = it.firstOrNull { option -> option.id == 2026 } ?: it.firstOrNull()
            }
            .onFailure { error = it.message ?: "无法加载赛季" }
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
            selectedSubTab = it.visibleRankingTabs().firstOrNull()?.tab_key.orEmpty()
        }.onFailure {
            error = it.message ?: "无法加载排行榜"
        }
        loading = false
    }

    LaunchedEffect(data, selectedSubTab, hasPlayedPodiumShine) {
        if (hasPlayedPodiumShine) {
            // Already played once in this process: stay on the static metallic frame.
            if (podiumShine.value != StaticPodiumShine) {
                podiumShine.snapTo(StaticPodiumShine)
            }
            return@LaunchedEffect
        }
        val podiumRows = data
            ?.visibleRankingTabs()
            ?.firstOrNull { it.tab_key == selectedSubTab }
            ?.list
            .orEmpty()
        if (podiumRows.isNotEmpty()) {
            // Sweep once, and finish exactly on the static metallic highlight.
            podiumShine.snapTo(InitialPodiumShine)
            podiumShine.animateTo(
                targetValue = StaticPodiumShine,
                animationSpec = tween(durationMillis = PodiumShineDurationMillis, easing = FastOutSlowInEasing)
            )
            hasPlayedPodiumShine = true
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("排行榜", selectedSeason?.name?.ifBlank { "锦标赛积分榜" } ?: "锦标赛积分榜")
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassChip("车手", selected = isDriver, onClick = { isDriver = true }, leadingIcon = Icons.Rounded.Person)
            GlassChip("车队", selected = !isDriver, onClick = { isDriver = false }, leadingIcon = Icons.Rounded.Groups)
        }
        LazyRow(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
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
        data?.visibleRankingTabs()?.let { tabs ->
            LazyRow(
                Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs) { tab ->
                    GlassChip(
                        label = tab.tab_name.cleanRankingLabel(),
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
                    item {
                        SectionLabel(
                            title = tab.tab_name.cleanRankingLabel(),
                            subtitle = remark.ifBlank { if (isDriver) "车手锦标赛" else "车队锦标赛" }
                        )
                    }
                    if (tab.list.isNotEmpty()) {
                        item {
                            RankingPodium(
                                rows = tab.list.take(3),
                                isDriver = isDriver,
                                chpId = selectedSeason?.chp_id ?: 0,
                                seasonId = selectedSeason?.id ?: 0,
                                shineOffset = podiumShine.value,
                                onDriverClick = onDriverClick,
                                onTeamClick = onTeamClick
                            )
                        }
                    }
                    itemsIndexed(tab.list.drop(3)) { index, row ->
                        RankingRow(
                            pos = index + 4,
                            row = row,
                            isDriver = isDriver,
                            chpId = selectedSeason?.chp_id ?: 0,
                            seasonId = selectedSeason?.id ?: 0,
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
                        Text("重试", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingPodium(
    rows: List<JsonObject>,
    isDriver: Boolean,
    chpId: Int,
    seasonId: Int,
    shineOffset: Float,
    onDriverClick: (chpId: Int, seasonId: Int, driverId: Int, name: String, avatar: String, teamLogo: String, stats: JsonObject) -> Unit,
    onTeamClick: (chpId: Int, seasonId: Int, teamId: Int, name: String, logo: String, stats: JsonObject) -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        listOf(1, 0, 2).forEach { sourceIndex ->
            rows.getOrNull(sourceIndex)?.let { row ->
                RankingPodiumEntry(
                    position = sourceIndex + 1,
                    row = row,
                    isDriver = isDriver,
                    chpId = chpId,
                    seasonId = seasonId,
                    shineOffset = shineOffset,
                    modifier = Modifier.weight(1f),
                    onDriverClick = onDriverClick,
                    onTeamClick = onTeamClick
                )
            }
        }
    }
}

@Composable
private fun RankingPodiumEntry(
    position: Int,
    row: JsonObject,
    isDriver: Boolean,
    chpId: Int,
    seasonId: Int,
    shineOffset: Float,
    modifier: Modifier,
    onDriverClick: (chpId: Int, seasonId: Int, driverId: Int, name: String, avatar: String, teamLogo: String, stats: JsonObject) -> Unit,
    onTeamClick: (chpId: Int, seasonId: Int, teamId: Int, name: String, logo: String, stats: JsonObject) -> Unit
) {
    val name = row.text("driver_abbr_chinese_name").ifBlank { row.text("team_abbr_chinese_name") }
    val avatar = row.text("driver_avatar").ifBlank { row.text("team_logo") }
    val teamLogo = row.text("team_logo")
    val driverId = row.intText("driver_id").ifZero { row.intText("drivers_id") }
    val teamId = row.intText("team_id")
    val metal = podiumMetal(position)
    val metalBrush = Brush.linearGradient(
        colors = listOf(metal.edge, metal.base, metal.light, metal.glint, metal.light, metal.base, metal.edge),
        start = Offset(shineOffset, -70f),
        end = Offset(shineOffset + 300f, 120f)
    )
    GlassSurface(
        modifier = modifier.padding(horizontal = 4.dp, vertical = if (position == 1) 0.dp else 10.dp),
        onClick = {
            if (isDriver && driverId > 0) {
                onDriverClick(chpId, seasonId, driverId, name, avatar, teamLogo, row)
            } else if (!isDriver && teamId > 0) {
                onTeamClick(chpId, seasonId, teamId, name, avatar, row)
            }
        },
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 14.dp)
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            MetallicText(
                text = "#$position",
                brush = metalBrush,
                shadow = metal.shadow,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            if (isDriver) {
                AsyncImage(
                    avatar,
                    null,
                    Modifier.size(if (position == 1) 72.dp else 58.dp).clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            } else {
                TeamLogo(
                    teamName = name,
                    url = teamLogo,
                    modifier = Modifier.size(if (position == 1) 72.dp else 58.dp)
                )
            }
            MetallicText(
                text = name,
                brush = metalBrush,
                shadow = metal.shadow,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            MetallicText(
                text = row.bestScoreText(),
                brush = metalBrush,
                shadow = metal.shadow,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RankingRow(
    pos: Int,
    row: JsonObject,
    isDriver: Boolean,
    chpId: Int,
    seasonId: Int,
    onDriverClick: (chpId: Int, seasonId: Int, driverId: Int, name: String, avatar: String, teamLogo: String, stats: JsonObject) -> Unit,
    onTeamClick: (chpId: Int, seasonId: Int, teamId: Int, name: String, logo: String, stats: JsonObject) -> Unit
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
        selected = false,
        onClick = {
            if (isDriver && driverId > 0) {
                onDriverClick(chpId, seasonId, driverId, name, avatar, teamLogo, row)
            } else if (!isDriver && teamId > 0) {
                onTeamClick(chpId, seasonId, teamId, name, avatar, row)
            }
        },
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$pos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(34.dp),
                fontWeight = FontWeight.Bold
            )
            if (isDriver) {
                if (avatar.isNotBlank()) {
                    AsyncImage(
                        avatar,
                        null,
                        Modifier.size(42.dp).clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(10.dp))
                }
            } else {
                TeamLogo(
                    teamName = name,
                    url = teamLogo,
                    modifier = Modifier.size(42.dp)
                )
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

private fun String.cleanRankingLabel(): String =
    replace("\\n", " ")
        .replace("\n", " ")
        .replace(Regex("\\s+"), " ")
        .trim()

private fun String.cleanRankingRemark(): String =
    replace("\\n", "\n")
        .lines()
        .joinToString("\n") { it.trim() }
        .trim()

private inline fun Int.ifZero(block: () -> Int): Int = if (this == 0) block() else this

private data class PodiumMetal(
    val edge: Color,
    val base: Color,
    val light: Color,
    val glint: Color,
    val shadow: Color
)

private fun podiumMetal(position: Int): PodiumMetal = when (position) {
    1 -> PodiumMetal(
        edge = Color(0xFF7A4A00),
        base = Color(0xFFD99B16),
        light = Color(0xFFFFE08A),
        glint = Color(0xFFFFFBDF),
        shadow = Color(0x88704800)
    )
    2 -> PodiumMetal(
        edge = Color(0xFF596473),
        base = Color(0xFFAEB8C6),
        light = Color(0xFFE7EDF4),
        glint = Color.White,
        shadow = Color(0x88616B78)
    )
    else -> PodiumMetal(
        edge = Color(0xFF71391F),
        base = Color(0xFFB96F46),
        light = Color(0xFFE6A77D),
        glint = Color(0xFFFFE4D1),
        shadow = Color(0x88603623)
    )
}

@Composable
private fun MetallicText(
    text: String,
    brush: Brush,
    shadow: Color,
    style: TextStyle,
    fontWeight: FontWeight,
    maxLines: Int,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        style = style.merge(
            TextStyle(
                brush = brush,
                fontWeight = fontWeight,
                shadow = Shadow(color = shadow, offset = Offset(0f, 1.5f), blurRadius = 2.5f)
            )
        ),
        maxLines = maxLines,
        overflow = overflow
    )
}

private fun RankingData.visibleRankingTabs() =
    list.filterNot {
        it.tab_key in setOf(
            "score_movements",
            "t_score_movements",
            "gp_q_avg_rank_percent",
            "gp_race_avg_rank_percent"
        ) ||
            it.tab_key.contains("score_trend") ||
            it.tab_name.cleanRankingLabel().let { name ->
                name.contains("积分走势") ||
                    name.contains("排位赛平均排名") ||
                    name.contains("正赛平均排名") ||
                    name.contains("排位赛 平均排名") ||
                    name.contains("正赛 平均排名")
            }
    }

private val RacingBlue = Color(0xFF58A6FF)
private const val InitialPodiumShine = -280f
private const val StaticPodiumShine = 90f
private const val PodiumShineDurationMillis = 2200
