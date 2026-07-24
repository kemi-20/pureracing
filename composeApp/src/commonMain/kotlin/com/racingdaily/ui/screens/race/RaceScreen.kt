package com.racingdaily.ui.screens.race

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Timer
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.racingdaily.resources.*
import com.racingdaily.data.model.RaceGp
import com.racingdaily.data.model.RaceSession
import com.racingdaily.data.remote.ApiService
import com.racingdaily.platform.LocalDateTimeParts
import com.racingdaily.platform.currentLocalDateTimeParts
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.LightweightSurface
import com.racingdaily.ui.components.ScreenHeader
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun RaceScreen(onRaceClick: (RaceGp) -> Unit, onTrackClick: (Int) -> Unit, api: ApiService) {
    var races by remember { mutableStateOf<List<RaceGp>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var didAutoScroll by remember(reloadKey) { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(reloadKey) {
        loading = true
        error = null
        runCatching { api.getRaceSchedule() }
            .onSuccess { payload ->
                races = payload.filter { gp ->
                    gp.gp_id.isNotBlank() || gp.gp_name.isNotBlank() || gp.race_time.isNotBlank()
                }
            }
            .onFailure { error = it.message ?: "无法加载赛历" }
        loading = false
    }

    LaunchedEffect(loading, error, races) {
        if (!loading && error == null && races.isNotEmpty() && !didAutoScroll) {
            didAutoScroll = true
            val targetIndex = runCatching { races.nearestRaceIndex() }
                .getOrDefault(0)
                .coerceIn(0, races.lastIndex)
            // Delay one frame so LazyColumn has real item metrics before scrolling.
            runCatching { listState.scrollToItem(targetIndex) }
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("赛事", "F1 赛程与比赛结果")
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
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
            races.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("暂无赛事数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                itemsIndexed(
                    races,
                    key = { index, gp ->
                        "${index}|${gp.gp_id}|${gp.race_time}|${gp.session.firstOrNull()?.session_id ?: 0}"
                    }
                ) { _, gp ->
                    RaceGlassCard(gp, onRaceClick, onTrackClick)
                }
            }
        }
    }
}

private fun List<RaceGp>.nearestRaceIndex(): Int {
    if (isEmpty()) return 0

    val liveIndex = indexOfFirst { gp -> gp.session.any { it.race_status == 2 } }
    if (liveIndex >= 0) return liveIndex

    val now = runCatching { currentLocalDateTimeParts().toSortableMinutes() }.getOrNull()
    if (now != null) {
        val nextTimedRace = mapIndexedNotNull { index, gp ->
            gp.nextSessionMinutesAfter(now)?.let { index to it }
        }.minByOrNull { it.second }
        if (nextTimedRace != null) return nextTimedRace.first
    }

    val upcomingIndex = indexOfFirst { gp -> gp.session.any { it.race_status != 1 } }
    if (upcomingIndex >= 0) return upcomingIndex

    return lastIndex.coerceAtLeast(0)
}

private fun RaceGp.nextSessionMinutesAfter(now: Long): Long? {
    val date = race_time.parseRaceDate() ?: return null
    val sessionTimes = session
        .flatMap { it.hour }
        .mapNotNull { it.parseRaceHour() }
    val candidates =
        if (sessionTimes.isEmpty()) {
            listOf(date.toSortableMinutes(0, 0))
        } else {
            sessionTimes.map { (hour, minute) -> date.toSortableMinutes(hour, minute) }
        }
    return candidates.filter { it > now }.minOrNull()
}

private data class RaceDate(val year: Int, val month: Int, val day: Int) {
    fun toSortableMinutes(hour: Int, minute: Int): Long =
        (((year * 100L + month) * 100L + day) * 24L + hour) * 60L + minute
}

private fun LocalDateTimeParts.toSortableMinutes(): Long =
    RaceDate(year, month, day).toSortableMinutes(hour, minute)

private fun String.parseRaceDate(): RaceDate? {
    val parts = split("-")
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    return RaceDate(year, month, day)
}

private fun String.parseRaceHour(): Pair<Int, Int>? {
    val normalized = trim()
    val parts = normalized.split(":")
    if (parts.size < 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].take(2).toIntOrNull() ?: return null
    return hour to minute
}

@Composable
private fun RaceGlassCard(gp: RaceGp, onRaceClick: (RaceGp) -> Unit, onTrackClick: (Int) -> Unit) {
    // Critical Android stability: never stack real Backdrop glass inside Race list items.
    LightweightSurface(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        onClick = { onRaceClick(gp) }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RaceFlag(gp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        gp.gp_name.ifBlank { gp.race_time_detail.ifBlank { "赛事" } },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (gp.race_time_detail.isNotBlank()) {
                        Text(
                            gp.race_time_detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (gp.track_name.isNotBlank()) {
                        Text(
                            gp.track_name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                SoftPill(
                    label = gp.chp_name.ifBlank { "F1" },
                    accent = MaterialTheme.colorScheme.primary,
                    leadingIcon = Icons.Rounded.Flag
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (gp.track_id > 0) {
                    SoftActionChip(
                        label = "赛道",
                        onClick = { onTrackClick(gp.track_id) },
                        leadingIcon = Icons.Rounded.Route
                    )
                }
                gp.weather?.temp?.takeIf { it.isNotBlank() }?.let { temp ->
                    SoftPill(label = "${temp}C")
                }
            }
            if (gp.session.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    gp.session.forEach { session ->
                        RaceSessionTile(session)
                    }
                }
            }
        }
    }
}

@Composable
private fun RaceSessionTile(session: RaceSession) {
    val accent = when (session.race_status) {
        2 -> MaterialTheme.colorScheme.primary
        1 -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.secondary
    }
    val status = when (session.race_status) {
        2 -> "直播中"
        1 -> "已结束"
        else -> "未开始"
    }
    val shape = RoundedCornerShape(15.dp)
    Column(
        Modifier
            .width(132.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.22f), shape)
            .border(1.dp, accent.copy(alpha = 0.28f), shape)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Rounded.Timer, null, modifier = Modifier.size(15.dp), tint = accent)
            Text(
                status,
                color = accent,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            session.session_name.firstOrNull().orEmpty().ifBlank { "分节" },
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            session.hour.joinToString(" / ").ifBlank { "时间待定" },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RaceFlag(gp: RaceGp) {
    val modifier = Modifier
        .width(68.dp)
        .height(51.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.18f))

    // Prefer high-res bundled SVG flags; fall back to API logo only when no mapping exists.
    val localFlag = gp.localFlagResource()
    if (localFlag != null) {
        Image(
            painter = painterResource(localFlag),
            contentDescription = gp.gp_name,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
        return
    }

    val logo = gp.gp_logo.takeIf { it.isNotBlank() } ?: gp.chp_logo
    if (logo.isNotBlank()) {
        AsyncImage(
            model = logo,
            contentDescription = gp.gp_name,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(modifier, contentAlignment = Alignment.Center) {
            Icon(
                Icons.Rounded.Flag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun RaceGp.localFlagResource(): DrawableResource? {
    val identity = "$gp_name $track_name".lowercase()
    return when {
        identity.containsAny("australia", "melbourne", "澳大利亚") -> Res.drawable.flag_au
        identity.containsAny("china", "chinese", "shanghai", "中国", "上海") -> Res.drawable.flag_cn
        identity.containsAny("japan", "japanese", "suzuka", "日本", "铃鹿") -> Res.drawable.flag_jp
        identity.containsAny("bahrain", "sakhir", "巴林") -> Res.drawable.flag_bh
        identity.containsAny("saudi", "jeddah", "沙特", "吉达") -> Res.drawable.flag_sa
        identity.containsAny("emilia", "imola", "italian", "monza", "意大利", "伊莫拉", "蒙扎") -> Res.drawable.flag_it
        identity.containsAny("monaco", "monte carlo", "摩纳哥") -> Res.drawable.flag_mc
        identity.containsAny("spain", "spanish", "barcelona", "madrid", "西班牙", "巴塞罗那", "马德里") -> Res.drawable.flag_es
        identity.containsAny("canada", "canadian", "montreal", "加拿大", "蒙特利尔") -> Res.drawable.flag_ca
        identity.containsAny("austria", "austrian", "spielberg", "奥地利") -> Res.drawable.flag_at
        identity.containsAny("britain", "british", "silverstone", "united kingdom", "英国", "银石") -> Res.drawable.flag_gb
        identity.containsAny("belgium", "belgian", "spa-francorchamps", "spa ", "比利时", "斯帕") -> Res.drawable.flag_be
        identity.containsAny("hungary", "hungarian", "budapest", "匈牙利", "布达佩斯") -> Res.drawable.flag_hu
        identity.containsAny("netherlands", "dutch", "zandvoort", "荷兰", "赞德沃特") -> Res.drawable.flag_nl
        identity.containsAny("azerbaijan", "baku", "阿塞拜疆", "巴库") -> Res.drawable.flag_az
        identity.containsAny("singapore", "marina bay", "新加坡", "滨海湾") -> Res.drawable.flag_sg
        identity.containsAny("mexico", "mexican", "墨西哥") -> Res.drawable.flag_mx
        identity.containsAny("brazil", "brazilian", "sao paulo", "interlagos", "巴西", "圣保罗") -> Res.drawable.flag_br
        identity.containsAny("qatar", "lusail", "卡塔尔", "卢赛尔", "罗塞尔") -> Res.drawable.flag_qa
        identity.containsAny("abu dhabi", "yas marina", "united arab emirates", "阿布扎比", "亚斯码头") -> Res.drawable.flag_ae
        identity.containsAny("miami", "las vegas", "austin", "united states", "american", "美国", "迈阿密", "拉斯维加斯", "奥斯汀", "美洲赛道") -> Res.drawable.flag_us
        identity.containsAny("france", "french", "paul ricard", "法国") -> Res.drawable.flag_fr
        identity.containsAny("germany", "german", "hockenheim", "nurburgring", "德国") -> Res.drawable.flag_de
        identity.containsAny("malaysia", "sepang", "马来西亚", "雪邦") -> Res.drawable.flag_my
        identity.containsAny("turkey", "turkish", "istanbul", "土耳其", "伊斯坦布尔") -> Res.drawable.flag_tr
        identity.containsAny("russia", "russian", "sochi", "俄罗斯", "索契") -> Res.drawable.flag_ru
        identity.containsAny("south africa", "kyalami", "南非") -> Res.drawable.flag_za
        else -> null
    }
}

private fun String.containsAny(vararg candidates: String): Boolean = candidates.any(::contains)

@Composable
private fun SoftPill(
    label: String,
    accent: Color = MaterialTheme.colorScheme.secondary,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = Modifier
            .background(accent.copy(alpha = 0.12f), shape)
            .border(1.dp, accent.copy(alpha = 0.22f), shape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(14.dp), tint = accent)
        }
        Text(
            label,
            color = accent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SoftActionChip(
    label: String,
    onClick: () -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    val shape = RoundedCornerShape(999.dp)
    val accent = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.22f), shape)
            .border(1.dp, accent.copy(alpha = 0.18f), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(15.dp), tint = accent)
        }
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
