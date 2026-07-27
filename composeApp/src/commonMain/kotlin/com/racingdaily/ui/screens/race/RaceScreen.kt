package com.racingdaily.ui.screens.race

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.racingdaily.data.model.F1CalendarEvent
import com.racingdaily.data.model.RaceGp
import com.racingdaily.data.model.RaceListItem
import com.racingdaily.data.model.RaceSession
import com.racingdaily.data.model.RankingData
import com.racingdaily.data.model.SessionResult
import com.racingdaily.data.model.StationItem
import com.racingdaily.data.model.StationNavItem
import com.racingdaily.data.model.StationScoreItem
import com.racingdaily.data.remote.ApiService
import com.racingdaily.platform.LocalDateTimeParts
import com.racingdaily.platform.currentLocalDateTimeParts
import com.racingdaily.platform.raceCalendarUtcToLocal
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassMaterial
import com.racingdaily.ui.components.GlassSurface
import com.racingdaily.ui.components.HighResolutionFlag
import com.racingdaily.ui.components.ScreenHeader
import com.racingdaily.ui.components.newsCardReveal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

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
        runCatching {
            val forceRefresh = reloadKey > 0
            val currentYear = currentLocalDateTimeParts().year
            val loaded = supervisorScope {
                val calendar = async {
                    runCatching {
                        api.getF1Calendar(seasonId = currentYear, forceRefresh = forceRefresh)
                    }.getOrDefault(emptyList())
                }
                val stations = async {
                    runCatching {
                        api.getStationList(chpId = 6, seasonId = currentYear, forceRefresh = forceRefresh).tmp
                    }.getOrDefault(emptyList())
                }
                val season = async {
                    runCatching {
                        api.getRaceList(chpId = 6, seasonId = currentYear, forceRefresh = forceRefresh)
                    }.getOrDefault(emptyList())
                }
                val ranking = async {
                    runCatching {
                        api.getDriverRanking(chpId = 6, seasonId = currentYear, forceRefresh = forceRefresh)
                    }.getOrNull()
                }
                CalendarLoadResult(
                    events = calendar.await(),
                    stations = stations.await(),
                    season = season.await(),
                    ranking = ranking.await()
                )
            }
            val calendarEvents = loaded.events
            val fallbackSchedule = if (calendarEvents.isEmpty()) {
                api.getRaceSchedule(forceRefresh = forceRefresh)
            } else {
                emptyList()
            }
            val completedStations = loaded.stations
            val seasonList = loaded.season
            val ranking = loaded.ranking
            val seasonGpIds = resolveSeasonGpIds(fallbackSchedule, completedStations, seasonList)
            val calendar = calendarEvents.toRaceSchedule(seasonList, seasonGpIds)
            val schedule = calendar?.schedule ?: fallbackSchedule
            val displayedSeason = calendar?.season ?: seasonList
            val historicalSessions = api.loadHistoricalSessions(
                schedule,
                displayedSeason,
                seasonGpIds,
                forceRefresh
            )
            buildSeasonRaceSchedule(
                schedule,
                completedStations,
                displayedSeason,
                ranking,
                currentYear,
                historicalSessions,
                seasonGpIds
            )
        }
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
            val targetIndex = runCatching { races.nearestRaceIndex() }
                .getOrDefault(0)
                .coerceIn(0, races.lastIndex)
            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .first { itemCount -> itemCount > targetIndex }
            listState.scrollToItem(targetIndex)
            didAutoScroll = true
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
                val focusedIndex = runCatching { races.nearestRaceIndex() }.getOrDefault(0)
                itemsIndexed(
                    races,
                    key = { index, gp ->
                        "${index}|${gp.gp_id}|${gp.race_time}|${gp.session.firstOrNull()?.session_id ?: 0}"
                    }
                ) { index, gp ->
                    RaceGlassCard(
                        gp = gp,
                        round = index + 1,
                        focused = index == focusedIndex,
                        onRaceClick = onRaceClick,
                        onTrackClick = onTrackClick
                    )
                }
            }
        }
    }
}

private data class CalendarLoadResult(
    val events: List<F1CalendarEvent>,
    val stations: List<StationItem>,
    val season: List<RaceListItem>,
    val ranking: RankingData?
)

private data class CalendarRaceSchedule(
    val schedule: List<RaceGp>,
    val season: List<RaceListItem>
)

private data class LocalCalendarSession(
    val event: F1CalendarEvent,
    val start: LocalDateTimeParts,
    val end: LocalDateTimeParts
)

private fun List<F1CalendarEvent>.toRaceSchedule(
    seasonList: List<RaceListItem>,
    seasonGpIds: Map<String, String>
): CalendarRaceSchedule? {
    if (isEmpty()) return null
    val now = currentLocalDateTimeParts()
    val activeSeason = seasonList.filter { it.status != 4 }
    val grouped = groupBy { it.raceName() }
        .entries
        .sortedWith(
            compareBy<Map.Entry<String, List<F1CalendarEvent>>> {
                it.value.minOfOrNull { event -> event.calendarRound() } ?: Int.MAX_VALUE
            }.thenBy { entry -> entry.value.minOfOrNull { event -> event.startUtc }.orEmpty() }
        )
    val schedule = mutableListOf<RaceGp>()
    val season = mutableListOf<RaceListItem>()

    grouped.forEachIndexed { index, (raceName, events) ->
        val seasonItem = activeSeason.firstOrNull { it.gp_name == raceName }
            ?: activeSeason.getOrNull(index)
        val localSessions = events.mapNotNull { event ->
            val start = raceCalendarUtcToLocal(event.startUtc) ?: return@mapNotNull null
            val end = raceCalendarUtcToLocal(event.endUtc) ?: start
            LocalCalendarSession(event, start, end)
        }.sortedBy { it.start.toSortableMinutes() }
        if (localSessions.isEmpty()) return@forEachIndexed

        val displayName = seasonItem?.gp_name.orEmpty().ifBlank { raceName }
        val gpId = seasonItem?.let { seasonGpIds[it.gp_name.normalizedRaceName()] }.orEmpty()
        val raceStatus = when {
            localSessions.any { now.isBetween(it.start, it.end) } -> 2
            localSessions.all { it.end.toSortableMinutes() <= now.toSortableMinutes() } -> 1
            else -> 3
        }
        val first = localSessions.first().start
        val last = localSessions.last().start
        val range = first.toCalendarDateLabel() + " ~ " + last.toCalendarDateLabel()
        season += (seasonItem ?: RaceListItem(
            gp_name = displayName,
            track_name = events.firstOrNull()?.location.orEmpty()
        )).copy(
            status = raceStatus,
            status_name = when (raceStatus) {
                1 -> "完赛"
                2 -> "进行中"
                else -> "未赛"
            },
            time = range
        )

        localSessions.groupBy { it.start.toIsoDate() }
            .entries
            .sortedBy { it.key }
            .forEach { (date, daySessions) ->
                schedule += RaceGp(
                    race_time = date,
                    race_time_detail = daySessions.first().start.toCalendarDateLabel(),
                    gp_id = gpId,
                    gp_name = displayName,
                    chp_name = "F1",
                    track_name = seasonItem?.track_name.orEmpty()
                        .ifBlank { events.firstOrNull()?.location.orEmpty() },
                    track_id = seasonItem?.track_id ?: 0,
                    session = daySessions.map { it.toRaceSession(now) }
                )
            }
    }
    return CalendarRaceSchedule(schedule = schedule, season = season)
}

private fun LocalCalendarSession.toRaceSession(now: LocalDateTimeParts): RaceSession {
    val sessionName = event.sessionName()
    return RaceSession(
        session_id = event.uid.hashCode() and Int.MAX_VALUE,
        session_name = listOf(sessionName),
        session_type = when (sessionName) {
            "一练" -> 1
            "二练" -> 2
            "三练" -> 3
            "排位赛", "冲刺排位" -> 4
            "正赛" -> 5
            else -> 6
        },
        hour = listOf(start.toClockLabel()),
        race_status = when {
            now.isBetween(start, end) -> 2
            end.toSortableMinutes() <= now.toSortableMinutes() -> 1
            else -> 3
        }
    )
}

private fun F1CalendarEvent.raceName(): String =
    summary.substringAfterLast('(').substringBeforeLast(')').unescapeCalendarText()
        .ifBlank { summary.unescapeCalendarText() }

private fun F1CalendarEvent.sessionName(): String =
    summary.substringAfter("F1:").substringBeforeLast(" (").trim().unescapeCalendarText()
        .let { name ->
            when (name) {
                "第1次练习赛" -> "一练"
                "第2次练习赛" -> "二练"
                "第3次练习赛" -> "三练"
                "大奖赛" -> "正赛"
                else -> name
            }
        }

private fun F1CalendarEvent.calendarRound(): Int =
    uid.substringAfter("#GP", "").substringBefore('_').toIntOrNull() ?: Int.MAX_VALUE

private fun String.unescapeCalendarText(): String =
    replace("\\,", ",").replace("\\;", ";").replace("\\n", "\n").replace("\\\\", "\\")

private fun LocalDateTimeParts.toIsoDate(): String =
    "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

private fun LocalDateTimeParts.toCalendarDateLabel(): String =
    "${month.toString().padStart(2, '0')}月${day.toString().padStart(2, '0')}日"

private fun LocalDateTimeParts.toClockLabel(): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

private fun LocalDateTimeParts.isBetween(
    start: LocalDateTimeParts,
    end: LocalDateTimeParts
): Boolean = toSortableMinutes() in start.toSortableMinutes() until end.toSortableMinutes()

private fun buildSeasonRaceSchedule(
    schedule: List<RaceGp>,
    stations: List<StationItem>,
    seasonList: List<RaceListItem>,
    ranking: RankingData?,
    seasonYear: Int,
    historicalSessions: Map<String, List<RaceSession>>,
    seasonGpIds: Map<String, String>
): List<RaceGp> {
    val historicalResults = ranking.historicalRaceResults()
    val scheduleByGp = schedule
        .groupBy { it.gp_id.trim() }
        .filterKeys { it.isNotBlank() && it != "0" }

    // Prefer official full-season list so early completed races are never dropped.
    if (seasonList.isNotEmpty()) {
        val stationByName = stations.associateBy { it.chinese_name.normalizedRaceName() }
        return seasonList.map { seasonItem ->
            val station = stationByName[seasonItem.gp_name.normalizedRaceName()]
            val gpId = station?.gp_id?.takeIf { it > 0 }?.toString()
                ?: scheduleByGp.keys.firstOrNull { key ->
                    scheduleByGp[key].orEmpty().any { day -> day.gp_name.sameRaceNameAs(seasonItem.gp_name) }
                }
                ?: seasonGpIds[seasonItem.gp_name.normalizedRaceName()].orEmpty()
            val days = when {
                gpId.isNotBlank() -> scheduleByGp[gpId].orEmpty()
                else -> schedule.filter { it.gp_name.sameRaceNameAs(seasonItem.gp_name) }
            }
            days.mergedRaceCard(
                fallbackName = seasonItem.gp_name,
                fallbackTrackName = seasonItem.track_name,
                fallbackTrackId = seasonItem.track_id,
                fallbackGpId = gpId,
                seasonItem = seasonItem,
                station = station,
                seasonYear = seasonYear,
                historicalResults = historicalResults,
                historicalSessions = historicalSessions[gpId].orEmpty()
            )
        }
    }

    // Fallback: rolling schedule + missing station entries.
    val scheduledIds = schedule.mapTo(mutableSetOf()) { it.gp_id }
    val mergedSchedule = schedule
        .groupBy { it.gp_id.ifBlank { it.gp_name } }
        .map { (_, days) ->
            days.mergedRaceCard(
                fallbackName = days.firstOrNull()?.gp_name.orEmpty(),
                fallbackTrackName = days.firstOrNull()?.track_name.orEmpty(),
                fallbackTrackId = days.firstOrNull()?.track_id ?: 0,
                fallbackGpId = days.firstOrNull()?.gp_id.orEmpty(),
                seasonItem = null,
                station = null,
                seasonYear = seasonYear,
                historicalResults = historicalResults,
                historicalSessions = historicalSessions[days.firstOrNull()?.gp_id.orEmpty()].orEmpty()
            )
        }
    val missing = stations
        .asSequence()
        .filter { it.gp_id > 0 && it.gp_id.toString() !in scheduledIds }
        .sortedBy { it.number.toIntOrNull() ?: Int.MAX_VALUE }
        .map { station ->
            val details = seasonList.firstOrNull { it.gp_name.sameRaceNameAs(station.chinese_name) }
            emptyList<RaceGp>().mergedRaceCard(
                fallbackName = station.chinese_name,
                fallbackTrackName = details?.track_name.orEmpty(),
                fallbackTrackId = details?.track_id ?: 0,
                fallbackGpId = station.gp_id.toString(),
                seasonItem = details,
                station = station,
                seasonYear = seasonYear,
                historicalResults = historicalResults,
                historicalSessions = historicalSessions[station.gp_id.toString()].orEmpty()
            )
        }
        .toList()
    return missing + mergedSchedule
}

private fun List<RaceGp>.mergedRaceCard(
    fallbackName: String,
    fallbackTrackName: String,
    fallbackTrackId: Int,
    fallbackGpId: String,
    seasonItem: RaceListItem?,
    station: StationItem?,
    seasonYear: Int,
    historicalResults: Map<String, List<SessionResult>>,
    historicalSessions: List<RaceSession>
): RaceGp {
    val orderedDays = sortedBy { it.race_time }
    val sample = orderedDays.firstOrNull()
    val gpId = fallbackGpId.ifBlank { sample?.gp_id.orEmpty() }
    val status = seasonItem?.statusName().orEmpty()
        .ifBlank {
            when {
                orderedDays.any { day -> day.session.any { it.race_status == 2 } } -> "进行中"
                orderedDays.any { day -> day.session.any { it.race_status != 1 && it.race_status != 4 } } -> "未赛"
                orderedDays.isNotEmpty() -> "完赛"
                else -> "已结束"
            }
        }
    val sessionsFromSchedule = orderedDays.flatMap { day ->
        day.session.map { session ->
            val dayLabel = day.race_time_detail.substringAfter(" ").ifBlank {
                day.race_time_detail.ifBlank { day.race_time }
            }
            val hours = session.hour.map { hour ->
                if (dayLabel.isBlank() || hour.contains(dayLabel)) hour else "$dayLabel $hour"
            }.ifEmpty {
                listOf(dayLabel.ifBlank { "时间待定" })
            }
            session.copy(hour = hours)
        }
    }
    val raceResults = historicalResults[gpId].orEmpty().ifEmpty {
        historicalResults.entries.firstOrNull { (key, _) ->
            key.sameRaceNameAs(fallbackName) || key.sameRaceNameAs(sample?.gp_name.orEmpty())
        }?.value.orEmpty()
    }
    val sessions = when {
        sessionsFromSchedule.isNotEmpty() -> {
            if (raceResults.isEmpty()) {
                sessionsFromSchedule
            } else {
                var attached = false
                val mapped = sessionsFromSchedule.map { session ->
                    val isRaceSession = session.session_type == 5 ||
                        session.session_name.any { name -> name.contains("正赛") }
                    if (!attached && isRaceSession) {
                        attached = true
                        if (session.race_result.isEmpty()) {
                            session.copy(race_result = raceResults, race_status = 1)
                        } else {
                            session
                        }
                    } else {
                        session
                    }
                }
                if (attached) {
                    mapped
                } else {
                    mapped + RaceSession(
                        session_id = -1,
                        session_name = listOf("正赛成绩"),
                        session_type = 5,
                        hour = listOf(
                            seasonItem?.time?.substringAfter("~")?.trim().orEmpty().ifBlank { "完赛" }
                        ),
                        race_status = 1,
                        race_result = raceResults
                    )
                }
            }
        }
        historicalSessions.isNotEmpty() -> historicalSessions
        raceResults.isNotEmpty() -> listOf(
            RaceSession(
                session_id = -1,
                session_name = listOf("正赛"),
                session_type = 5,
                hour = listOf(seasonItem?.time.orEmpty().ifBlank { "完赛" }),
                race_status = 1,
                race_result = raceResults
            )
        )
        seasonItem != null || station != null -> listOf(
            RaceSession(
                session_id = 0,
                session_name = listOf(status.ifBlank { "赛程" }),
                session_type = 0,
                hour = listOf(seasonItem?.time.orEmpty().ifBlank { "详见分站" }),
                race_status = when (seasonItem?.status) {
                    1 -> 1
                    4 -> 4
                    else -> 0
                },
                race_result = emptyList()
            )
        )
        else -> emptyList()
    }

    val detail = seasonItem?.time?.let { range ->
        if (status.isBlank()) range else "$range · $status"
    }.orEmpty().ifBlank {
        orderedDays.map { it.race_time_detail }.filter { it.isNotBlank() }.distinct()
            .joinToString(" / ")
            .ifBlank {
                station?.number?.toIntOrNull()?.let { "第 $it 站 · $status" }.orEmpty()
            }
    }

    return RaceGp(
        race_time = orderedDays.firstOrNull()?.race_time
            ?: seasonItem?.time?.firstDateOfRace(seasonYear).orEmpty(),
        race_time_detail = detail,
        gp_id = gpId,
        gp_name = sample?.gp_name?.takeIf { it.isNotBlank() } ?: fallbackName,
        chp_name = sample?.chp_name?.ifBlank { "F1" } ?: "F1",
        chp_logo = sample?.chp_logo.orEmpty(),
        gp_logo = sample?.gp_logo.orEmpty(),
        track_name = sample?.track_name?.ifBlank { fallbackTrackName } ?: fallbackTrackName,
        track_id = (sample?.track_id ?: 0).takeIf { it > 0 } ?: fallbackTrackId,
        weather = sample?.weather,
        session = sessions
    )
}

private val stationSessionKeys = setOf(
    "fp1cj", "fp2cj", "fp3cj", "ccpws", "ccpwscj", "ccscj", "pwscj", "zscj"
)

private fun resolveSeasonGpIds(
    schedule: List<RaceGp>,
    stations: List<StationItem>,
    seasonList: List<RaceListItem>
): Map<String, String> {
    val known = linkedMapOf<String, String>()
    schedule.forEach { gp ->
        if (gp.gp_id.isNotBlank() && gp.gp_id != "0" && gp.gp_name.isNotBlank()) {
            known[gp.gp_name.normalizedRaceName()] = gp.gp_id
        }
    }
    stations.forEach { station ->
        if (station.gp_id > 0) known[station.chinese_name.normalizedRaceName()] = station.gp_id.toString()
    }
    val baseGpId = stations.mapNotNull { station ->
        val round = station.number.toIntOrNull() ?: return@mapNotNull null
        station.gp_id.takeIf { it > 0 }?.minus(round - 1)
    }.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

    return seasonList.mapIndexedNotNull { index, item ->
        val key = item.gp_name.normalizedRaceName()
        val gpId = known[key] ?: baseGpId?.plus(index)?.toString() ?: return@mapIndexedNotNull null
        key to gpId
    }.toMap()
}

private suspend fun ApiService.loadHistoricalSessions(
    schedule: List<RaceGp>,
    seasonList: List<RaceListItem>,
    seasonGpIds: Map<String, String>,
    forceRefresh: Boolean = false
): Map<String, List<RaceSession>> {
    val scheduledIds = schedule.mapTo(mutableSetOf()) { it.gp_id }
    val completedRaces = seasonList.mapNotNull { seasonItem ->
        val gpId = seasonGpIds[seasonItem.gp_name.normalizedRaceName()]?.toIntOrNull()
            ?: return@mapNotNull null
        if (gpId.toString() in scheduledIds || seasonItem.status !in setOf(1, 4)) return@mapNotNull null
        seasonItem to gpId
    }
    val requestGate = Semaphore(6)

    return supervisorScope {
        completedRaces.map { (seasonItem, gpId) ->
            async {
                requestGate.withPermit {
                    try {
                        val navigation = getStationRank(gpId, forceRefresh = forceRefresh).navbar
                            .filter { it.key_name in stationSessionKeys }
                            .sortedBy { it.sessionOrder() }
                        val raceNavigation = navigation.firstOrNull { it.key_name == "zscj" }
                        val podium = if (raceNavigation != null && seasonItem.status != 4) {
                            try {
                                getStationScore(gpId, raceNavigation.id, forceRefresh = forceRefresh)
                                    .sortedBy { it.display_order }
                                    .take(3)
                            } catch (error: CancellationException) {
                                throw error
                            } catch (_: Throwable) {
                                emptyList()
                            }
                        } else {
                            emptyList()
                        }
                        val sessions = navigation.map { nav ->
                            nav.toRaceSession(
                                scores = if (nav.id == raceNavigation?.id) podium else emptyList(),
                                raceRange = seasonItem.time,
                                raceStatus = seasonItem.status
                            )
                        }
                        if (sessions.isEmpty()) null else gpId.toString() to sessions
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Throwable) {
                        null
                    }
                }
            }
        }.awaitAll()
            .filterNotNull()
            .toMap(linkedMapOf())
    }
}

private fun StationNavItem.sessionOrder(): Int = when (key_name) {
    "fp1cj" -> 10
    "ccpws" -> 20
    "fp2cj" -> 20
    "ccpwscj", "ccscj" -> 30
    "fp3cj" -> 30
    "pwscj" -> 40
    "zscj" -> 50
    else -> 100
}

private fun StationNavItem.toRaceSession(
    scores: List<StationScoreItem>,
    raceRange: String,
    raceStatus: Int
): RaceSession = RaceSession(
    session_id = scores.firstOrNull()?.gp_session_id ?: id,
    session_name = listOf(
        when (key_name) {
            "fp1cj" -> "一练"
            "fp2cj" -> "二练"
            "fp3cj" -> "三练"
            "ccpws" -> "冲刺排位"
            "ccpwscj", "ccscj" -> "冲刺赛"
            "pwscj" -> "排位赛"
            "zscj" -> "正赛"
            else -> name.removeSuffix("成绩")
        }
    ),
    session_type = when (key_name) {
        "fp1cj" -> 1
        "fp2cj" -> 2
        "fp3cj" -> 3
        "pwscj", "ccpws" -> 4
        "zscj" -> 5
        else -> 6
    },
    hour = listOf(raceRange.sessionDateLabel(key_name)),
    race_status = if (raceStatus == 4) 4 else 1,
    result_type_id = id,
    race_result = scores.sortedBy { it.display_order }.map { it.toSessionResult() }
)

internal fun StationScoreItem.toSessionResult(): SessionResult = SessionResult(
    rank = rank?.toIntOrNull() ?: display_order,
    driverid = driver_id.toString(),
    dr_name = driver_abbr_chinese_name,
    teamid = team_id.toString(),
    team_logo = team_logo,
    gap = gap.orEmpty().ifBlank { fast_lap_speed.orEmpty() },
    score_p = point ?: 0,
    is_fast = is_fast ?: 0
)

private fun String.sessionDateLabel(key: String): String {
    val firstDay = substringBefore("~").trim()
    val lastDay = substringAfter("~", firstDay).trim()
    return when (key) {
        "fp1cj", "fp2cj", "ccpws" -> firstDay
        "zscj" -> lastDay
        else -> lastDay.previousDayLabel().ifBlank { firstDay }
    }
}

private fun String.previousDayLabel(): String {
    val month = substringBefore("月").filter(Char::isDigit).toIntOrNull() ?: return ""
    val day = substringAfter("月").substringBefore("日").filter(Char::isDigit).toIntOrNull() ?: return ""
    if (day <= 1) return ""
    return "${month.toString().padStart(2, '0')}月${(day - 1).toString().padStart(2, '0')}日"
}

private fun RankingData?.historicalRaceResults(): Map<String, List<SessionResult>> {
    val tabs = this?.list.orEmpty()
    val trendTab = tabs.firstOrNull { it.tab_key == "gp_p_trend" } ?: return emptyMap()
    val scoreTab = tabs.firstOrNull { it.tab_key == "gp_score_trend" }
    val profileTab = tabs.firstOrNull { it.tab_key == "total_score" }

    data class DriverRow(
        val name: String,
        val driverId: String,
        val teamId: String,
        val teamLogo: String,
        val ranks: Map<String, Int>
    )

    fun JsonObject.text(key: String): String =
        (this[key] as? JsonPrimitive)?.contentOrNull.orEmpty()

    fun JsonObject.intValue(key: String): Int =
        text(key).toIntOrNull()
            ?: (this[key] as? JsonPrimitive)?.contentOrNull?.toIntOrNull()
            ?: 0

    val profiles = profileTab?.list.orEmpty().associateBy { row ->
        row.text("driver_abbr_chinese_name").ifBlank { row.text("driver_name") }
    }

    val drivers = trendTab.list.mapNotNull { row ->
        val ranks = linkedMapOf<String, Int>()
        row["site_rank"]?.jsonArray?.forEach { item ->
            val obj = item.jsonObject
            val gpId = obj.text("gp_id")
            val order = obj.intValue("display_order")
            if (gpId.isNotBlank() && order > 0) ranks[gpId] = order
            val gpName = obj.text("gp_chinese_name")
            if (gpName.isNotBlank() && order > 0) ranks[gpName] = order
        }
        if (ranks.isEmpty()) return@mapNotNull null
        val name = row.text("driver_abbr_chinese_name").ifBlank { row.text("driver_name") }
        val profile = profiles[name]
        DriverRow(
            name = name,
            driverId = profile?.text("driver_id").orEmpty().ifBlank { row.text("driver_id") },
            teamId = profile?.text("team_id").orEmpty().ifBlank { row.text("team_id") },
            teamLogo = profile?.text("team_logo").orEmpty().ifBlank { row.text("team_logo") },
            ranks = ranks
        )
    }
    if (drivers.isEmpty()) return emptyMap()

    val pointsByDriverGp = mutableMapOf<String, MutableMap<String, Int>>()
    scoreTab?.list?.forEach { row ->
        val name = row.text("driver_abbr_chinese_name").ifBlank { row.text("driver_name") }
        val points = linkedMapOf<String, Int>()
        var previous = 0
        row["site_point"]?.jsonArray?.forEach { item ->
            val obj = item.jsonObject
            val gpId = obj.text("gp_id")
            val total = obj.intValue("total_point")
            val delta = (total - previous).coerceAtLeast(0)
            previous = total
            if (gpId.isNotBlank()) points[gpId] = delta
            val gpName = obj.text("gp_chinese_name")
            if (gpName.isNotBlank()) points[gpName] = delta
        }
        pointsByDriverGp[name] = points
    }

    val allGpKeys = drivers.flatMap { it.ranks.keys }.toSet()
    val out = linkedMapOf<String, List<SessionResult>>()
    allGpKeys.forEach { gpKey ->
        val ranked = drivers.mapNotNull { driver ->
            val rank = driver.ranks[gpKey] ?: return@mapNotNull null
            val points = pointsByDriverGp[driver.name]?.get(gpKey) ?: 0
            rank to SessionResult(
                rank = rank,
                driverid = driver.driverId,
                dr_name = driver.name,
                teamid = driver.teamId,
                team_logo = driver.teamLogo,
                gap = "",
                score_p = points,
                is_fast = 0
            )
        }.sortedBy { it.first }.map { it.second }
        if (ranked.isNotEmpty()) out[gpKey] = ranked
    }
    return out
}

private fun String.sameRaceNameAs(other: String): Boolean =
    normalizedRaceName() == other.normalizedRaceName()

private fun String.normalizedRaceName(): String =
    trim()
        .removeSuffix("（取消）")
        .removeSuffix("(取消)")
        .removeSuffix("大奖赛")
        .replace(" ", "")
        .lowercase()

private fun RaceListItem.statusName(): String =
    status_name.ifBlank {
        when (status) {
            1 -> "完赛"
            4 -> "取消"
            else -> ""
        }
    }

private fun String.firstDateOfRace(year: Int): String? {
    val first = substringBefore("~").trim()
    val parts = first.split("月", "日")
    if (parts.size < 2) return null
    val month = parts[0].filter(Char::isDigit).toIntOrNull() ?: return null
    val day = parts[1].filter(Char::isDigit).toIntOrNull() ?: return null
    if (month !in 1..12 || day !in 1..31) return null
    return "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}

private fun List<RaceGp>.nearestRaceIndex(): Int {
    if (isEmpty()) return 0

    val liveIndex = indexOfFirst { gp -> gp.session.any { it.race_status == 2 } }
    if (liveIndex >= 0) return liveIndex

    val localNow = runCatching { currentLocalDateTimeParts() }.getOrNull()
    if (localNow != null) {
        val activeWeekendIndex = indexOfFirst { gp ->
            gp.containsDate(localNow) && gp.session.any { it.race_status != 1 && it.race_status != 4 }
        }
        if (activeWeekendIndex >= 0) return activeWeekendIndex

        val now = localNow.toSortableMinutes()
        val nextTimedRace = mapIndexedNotNull { index, gp ->
            gp.nextSessionMinutesAfter(now)?.let { index to it }
        }.minByOrNull { it.second }
        if (nextTimedRace != null) return nextTimedRace.first
    }

    val upcomingIndex = indexOfFirst { gp -> gp.session.any { it.race_status != 1 && it.race_status != 4 } }
    if (upcomingIndex >= 0) return upcomingIndex

    return lastIndex.coerceAtLeast(0)
}

private fun RaceGp.containsDate(now: LocalDateTimeParts): Boolean {
    val start = race_time.parseRaceDate() ?: return false
    val endLabel = race_time_detail.substringBefore("·").substringAfter("~", "").trim()
    val end = endLabel.parseMonthDay(start.year) ?: start
    val current = (now.year * 10000L) + (now.month * 100L) + now.day
    return current in start.toSortableDate()..end.toSortableDate()
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

    fun toSortableDate(): Long = (year * 10000L) + (month * 100L) + day
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

private fun String.parseMonthDay(year: Int): RaceDate? {
    val month = substringBefore("月").filter(Char::isDigit).toIntOrNull() ?: return null
    val day = substringAfter("月").substringBefore("日").filter(Char::isDigit).toIntOrNull() ?: return null
    if (month !in 1..12 || day !in 1..31) return null
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
private fun RaceGlassCard(
    gp: RaceGp,
    round: Int,
    focused: Boolean,
    onRaceClick: (RaceGp) -> Unit,
    onTrackClick: (Int) -> Unit
) {
    val isLive = gp.session.any { it.race_status == 2 }
    val isFinished = gp.session.isNotEmpty() && gp.session.all { it.race_status == 1 || it.race_status == 4 }
    val accent = when {
        isLive -> MaterialTheme.colorScheme.primary
        focused && !isFinished -> MaterialTheme.colorScheme.secondary
        isFinished -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusLabel = when {
        isLive -> "直播中"
        focused && !isFinished -> "当前赛站"
        isFinished -> "已结束"
        else -> "即将开始"
    }

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .newsCardReveal("${gp.gp_id}|${gp.race_time}|${gp.gp_name}"),
        shape = RoundedCornerShape(if (focused) 22.dp else 18.dp),
        material = GlassMaterial.THIN,
        selected = isLive,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 15.dp),
        onClick = { onRaceClick(gp) }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
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
                            color = accent,
                            fontWeight = FontWeight.Medium
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
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "R${round.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    RaceInlineMeta(
                        label = statusLabel,
                        accent = accent,
                        leadingIcon = Icons.Rounded.Flag
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (gp.track_id > 0) {
                    Row(
                        modifier = Modifier
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                role = Role.Button,
                                onClick = { onTrackClick(gp.track_id) }
                            )
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Route,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "赛道",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                gp.weather?.temp?.takeIf { it.isNotBlank() }?.let { temp ->
                    RaceInlineMeta(label = "${temp}C", accent = MaterialTheme.colorScheme.secondary)
                }
                if (!isLive) {
                    RaceInlineMeta(
                        label = gp.chp_name.ifBlank { "F1" },
                        accent = MaterialTheme.colorScheme.primary
                    )
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
                        RaceSessionSummary(session)
                    }
                }
            }
        }
    }
}

@Composable
private fun RaceInlineMeta(
    label: String,
    accent: Color,
    leadingIcon: ImageVector? = null
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = accent
            )
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
private fun RaceSessionSummary(session: RaceSession) {
    val accent = when (session.race_status) {
        2 -> MaterialTheme.colorScheme.primary
        1 -> MaterialTheme.colorScheme.tertiary
        4 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }
    val status = when (session.race_status) {
        2 -> "直播中"
        1 -> "已结束"
        4 -> "取消"
        else -> "未开始"
    }
    Column(
        modifier = Modifier
            .width(126.dp)
            .padding(horizontal = 4.dp, vertical = 4.dp),
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

/*
 * Race cards intentionally use one AndroidLiquidGlass surface per list item.
 * Session rows remain within that same sampled material to avoid nested GPU-heavy
 * backdrop layers on Android.
 */
@Composable
internal fun RaceFlag(
    gp: RaceGp,
    modifier: Modifier = Modifier.width(68.dp).height(51.dp)
) {
    val remoteLogo = gp.gp_logo.takeIf { it.isNotBlank() } ?: gp.chp_logo.takeIf { it.isNotBlank() }
    HighResolutionFlag(
        identity = "${gp.gp_name} ${gp.track_name}",
        remoteFallbackUrl = remoteLogo.orEmpty(),
        contentDescription = gp.gp_name,
        modifier = modifier
    )
}
