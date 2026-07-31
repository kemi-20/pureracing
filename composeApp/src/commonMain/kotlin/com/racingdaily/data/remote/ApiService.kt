package com.racingdaily.data.remote

import com.racingdaily.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.get
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import com.racingdaily.util.runSuspendCatching
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class ApiService(private val client: HttpClient) {

    private data class CacheEntry(val value: Any, val storedAt: TimeMark)

    private val cacheMutex = Mutex()
    private val cache = mutableMapOf<String, CacheEntry>()
    private val inFlight = mutableMapOf<String, CompletableDeferred<Any>>()

    private fun <T> ApiResponse<T>.requireData(): T {
        if (code != 200) error(msg.ifBlank { "API request failed with code $code" })
        return requireNotNull(data) {
            "${msg.ifBlank { "API response" }}: response did not contain data"
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T : Any> cached(
        key: String,
        forceRefresh: Boolean = false,
        loader: suspend () -> T
    ): T {
        var cachedValue: Any? = null
        var staleValue: Any? = null
        var producer = false
        lateinit var request: CompletableDeferred<Any>

        cacheMutex.withLock {
            val entry = cache[key]
            if (!forceRefresh && entry != null && entry.storedAt.elapsedNow() < StartupCacheLifetime) {
                cachedValue = entry.value
            } else {
                staleValue = entry?.value
                request = inFlight[key] ?: CompletableDeferred<Any>().also {
                    inFlight[key] = it
                    producer = true
                }
            }
        }
        cachedValue?.let { return it as T }
        if (!producer) return request.await() as T

        return try {
            val value = loader()
            cacheMutex.withLock {
                cache[key] = CacheEntry(value, TimeSource.Monotonic.markNow())
                trimCache()
                if (inFlight[key] === request) inFlight.remove(key)
            }
            request.complete(value)
            value
        } catch (error: Exception) {
            if (error is CancellationException) {
                cacheMutex.withLock {
                    if (inFlight[key] === request) inFlight.remove(key)
                }
                request.completeExceptionally(error)
                throw error
            }
            cacheMutex.withLock {
                if (inFlight[key] === request) inFlight.remove(key)
            }
            staleValue?.let {
                request.complete(it)
                return it as T
            }
            request.completeExceptionally(error)
            throw error
        }
    }

    private fun trimCache() {
        while (cache.size > MaxCacheEntries) {
            val oldestKey = cache.maxByOrNull { (_, entry) -> entry.storedAt.elapsedNow() }?.key
                ?: return
            cache.remove(oldestKey)
        }
    }

    suspend fun getNewsList(tagId: Int, page: Int = 1, forceRefresh: Boolean = false): NewsListData {
        suspend fun request() = client.get("index/index") {
            parameter("tag_id", tagId)
            parameter("page", page)
        }.body<ApiResponse<NewsListData>>().requireData()
        return if (page == 1) cached("news:$tagId:$page", forceRefresh) { request() } else request()
    }

    suspend fun getNewsDetail(id: Int) =
        client.get("index/detail") { parameter("id", id) }.body<ApiResponse<NewsDetail>>().requireData()

    suspend fun getArticleComments(articleId: Int, page: Int = 0) =
        client.submitForm(
            url = "comment/list",
            formParameters = Parameters.build {
                append("article_id", articleId.toString())
                append("page", page.toString())
            }
        ).body<ApiResponse<CommentListData>>().requireData()

    suspend fun getArticleCommentsWithReplies(articleId: Int): CommentListData {
        val comments = getAllArticleComments(articleId)
        val requestLimit = Semaphore(MaxConcurrentCommentReplyRequests)
        val completeComments = supervisorScope {
            comments.comment_list.map { comment ->
                async {
                    requestLimit.withPermit {
                        if (comment.sub_count <= comment.sub_list.size) {
                            comment
                        } else {
                            val replies = runSuspendCatching {
                                getAllCommentReplies(comment.id, comment.sub_count)
                            }.getOrDefault(comment.sub_list)
                            comment.copy(sub_list = replies)
                        }
                    }
                }
            }.awaitAll()
        }
        return comments.copy(comment_list = completeComments)
    }

    private suspend fun getAllArticleComments(articleId: Int): CommentListData {
        val firstPage = getArticleComments(articleId, page = 0)
        if (firstPage.comment_list.isEmpty() || firstPage.comment_list.size >= firstPage.count) {
            return firstPage
        }

        val commentsById = linkedMapOf<Int, ArticleComment>()
        firstPage.comment_list.forEach { comment -> commentsById[comment.id] = comment }
        val pageSize = firstPage.comment_list.size.coerceAtLeast(1)
        val pageCount = ((firstPage.count.toLong() + pageSize - 1L) / pageSize)
            .coerceIn(1L, MaxCommentPages.toLong())
            .toInt()
        val requestLimit = Semaphore(MaxConcurrentCommentPageRequests)
        val remainingPages = supervisorScope {
            (1 until pageCount).map { page ->
                async {
                    requestLimit.withPermit {
                        runSuspendCatching { getArticleComments(articleId, page) }.getOrNull()
                    }
                }
            }.awaitAll()
        }
        remainingPages.filterNotNull().forEach { page ->
            page.comment_list.forEach { comment -> commentsById[comment.id] = comment }
        }
        return firstPage.copy(comment_list = commentsById.values.toList())
    }

    private suspend fun getAllCommentReplies(commentId: Int, expectedCount: Int): List<ArticleComment> {
        val repliesById = linkedMapOf<Int, ArticleComment>()
        var page = 0
        while (page < MaxCommentReplyPages && repliesById.size < expectedCount) {
            val previousSize = repliesById.size
            val data = client.submitForm(
                url = "comment/sub-list",
                formParameters = Parameters.build {
                    append("comment_id", commentId.toString())
                    append("page", page.toString())
                }
            ).body<ApiResponse<CommentSubListData>>().requireData()
            if (data.sub_coment.isEmpty()) break
            data.sub_coment.forEach { reply -> repliesById[reply.id] = reply }
            if (repliesById.size == previousSize) break
            page++
        }
        return repliesById.values.toList()
    }

    suspend fun getNavTabs(forceRefresh: Boolean = false) = cached("news-navigation", forceRefresh) {
        client.get("index/navitv2").body<ApiResponse<Navitv2Data>>().requireData()
    }

    suspend fun getRaceSchedule(forceRefresh: Boolean = false) = cached("race-schedule", forceRefresh) {
        client.get("race/index").body<ApiResponse<List<RaceGp>>>().requireData()
    }

    suspend fun getF1Calendar(seasonId: Int, forceRefresh: Boolean = false) =
        cached("f1-calendar-ics:$seasonId", forceRefresh) {
            client.get(F1CalendarUrl) {
                headers {
                    set(HttpHeaders.Accept, "text/calendar")
                    set(HttpHeaders.Origin, "https://motorsportcalendars.com")
                    set(HttpHeaders.Referrer, "https://motorsportcalendars.com/")
                }
            }.bodyAsText().parseF1Calendar(seasonId)
        }

    suspend fun getF1EnglishCalendar(seasonId: Int, forceRefresh: Boolean = false) =
        cached("f1-calendar-english:$seasonId", forceRefresh) {
            client.get(F1EnglishCalendarUrl) {
                headers {
                    set(HttpHeaders.Accept, "text/calendar")
                    set(HttpHeaders.Origin, "https://motorsportcalendars.com")
                    set(HttpHeaders.Referrer, "https://motorsportcalendars.com/")
                }
            }.bodyAsText().parseF1Calendar(seasonId)
        }

    suspend fun getFormula1TrackImage(seasonId: Int, slug: String, forceRefresh: Boolean = false) =
        cached("formula1-track-image:$seasonId:$slug", forceRefresh) {
            client.get("$Formula1ApiBase/v1/editorial-assemblies/races") {
                headers {
                    set("apikey", Formula1PublicApiKey)
                    set("locale", "en")
                    set(HttpHeaders.Origin, "https://www.formula1.com")
                    set(HttpHeaders.Referrer, "https://www.formula1.com/en/racing/$seasonId/$slug")
                }
                parameter("season", seasonId)
                parameter("identifier", slug)
            }.body<Formula1RacePage>().circuitMapImage.url
        }

    suspend fun getRaceList(chpId: Int, seasonId: Int, forceRefresh: Boolean = false) =
        cached("race-list:$chpId:$seasonId", forceRefresh) {
            client.get("race/list") {
            parameter("chp_id", chpId)
            parameter("season_id", seasonId)
            }.body<ApiResponse<List<RaceListItem>>>().requireData()
        }

    suspend fun getRankingNav(forceRefresh: Boolean = false) = cached("ranking-navigation", forceRefresh) {
        client.get("rank/navigationv2").body<ApiResponse<RankingNavData>>().requireData()
    }

    suspend fun getDriverRanking(chpId: Int, seasonId: Int, forceRefresh: Boolean = false) =
        cached("driver-ranking:$chpId:$seasonId", forceRefresh) {
            client.get("rank/driver") { parameter("chp_id", chpId); parameter("season_id", seasonId) }
                .body<ApiResponse<RankingData>>().requireData()
        }

    suspend fun getTeamRanking(chpId: Int, seasonId: Int, forceRefresh: Boolean = false) =
        cached("team-ranking:$chpId:$seasonId", forceRefresh) {
            client.get("rank/team") { parameter("chp_id", chpId); parameter("season_id", seasonId) }
                .body<ApiResponse<RankingData>>().requireData()
        }

    suspend fun getStationList(chpId: Int, seasonId: Int, forceRefresh: Boolean = false) =
        cached("station-list:$chpId:$seasonId", forceRefresh) {
            client.get("station/list") { parameter("chp_id", chpId); parameter("season_id", seasonId) }
                .body<ApiResponse<StationData>>().requireData()
        }

    suspend fun getStationRank(gpId: Int, forceRefresh: Boolean = false) =
        cached("station-rank:$gpId", forceRefresh) {
            client.get("station/rank") { parameter("gp_id", gpId) }.body<ApiResponse<StationRankData>>().requireData()
        }

    suspend fun getStationScore(gpId: Int, typeId: Int, forceRefresh: Boolean = false) =
        cached("station-score:$gpId:$typeId", forceRefresh) {
            client.get("station/score") {
                parameter("gp_id", gpId)
                parameter("type_id", typeId)
            }.body<ApiResponse<List<StationScoreItem>>>().requireData()
        }

    suspend fun getStationStrategy(gpId: Int, typeId: Int, forceRefresh: Boolean = false) =
        cached("station-strategy:$gpId:$typeId", forceRefresh) {
            client.get("station/score") {
                parameter("gp_id", gpId)
                parameter("type_id", typeId)
            }.body<ApiResponse<StationStrategyData>>().requireData()
        }

    suspend fun getTrackInfo(trackId: Int) =
        client.get("track/index") { parameter("track_id", trackId) }.body<ApiResponse<TrackData>>().requireData()

    suspend fun getTrackScore(trackId: Int) =
        client.get("track/score") { parameter("track_id", trackId) }.body<ApiResponse<TrackScoreData>>().requireData()

    suspend fun getTeamScore(chpId: Int, teamId: Int) =
        client.get("team/score") { parameter("chp_id", chpId); parameter("team_id", teamId) }
            .body<ApiResponse<TeamScoreData>>().requireData()

    suspend fun getTeamInfo(chpId: Int, teamId: Int, seasonId: Int) =
        client.get("team/index") {
            parameter("chp_id", chpId)
            parameter("team_id", teamId)
            parameter("season_id", seasonId)
        }.body<ApiResponse<TeamInfoData>>().requireData()

    suspend fun getDriverInfo(chpId: Int, driverId: Int, seasonId: Int) =
        client.get("driver/infor") {
            parameter("chp_id", chpId)
            parameter("id", driverId)
            parameter("season_id", seasonId)
        }.body<ApiResponse<DriverInfoData>>().requireData()

    suspend fun getDriverPhoto(chpId: Int, driverId: Int) =
        client.get("driver/photo") { parameter("chp_id", chpId); parameter("id", driverId) }
            .body<ApiResponse<DriverPhotoData>>().requireData()

    suspend fun getCustomSeason() = client.get("custom/season").body<ApiResponse<ChampSeason>>().requireData()
    suspend fun getCustomSubstation() = client.get("custom/substation").body<ApiResponse<ChampSubstation>>().requireData()
    suspend fun getCustomDriver(id: Int) = client.get("custom/driver") { parameter("custom_id", id) }.body<ApiResponse<ChampSeason>>().requireData()
    suspend fun getCustomTeam(id: Int) = client.get("custom/team") { parameter("custom_id", id) }.body<ApiResponse<ChampSeason>>().requireData()

    suspend fun getMotogpSeason() = client.get("motogp/season").body<ApiResponse<ChampSeason>>().requireData()
    suspend fun getMotogpSubstation() = client.get("motogp/substation").body<ApiResponse<ChampSubstation>>().requireData()
    suspend fun getMotogpDriver(id: Int) = client.get("motogp/driver") { parameter("motogp_id", id) }.body<ApiResponse<ChampSeason>>().requireData()
    suspend fun getMotogpTeam(id: Int) = client.get("motogp/team") { parameter("motogp_id", id) }.body<ApiResponse<ChampSeason>>().requireData()
    suspend fun getMotogpManufacturer(id: Int) = client.get("motogp/manufacturer") { parameter("motogp_id", id) }.body<ApiResponse<ChampSeason>>().requireData()

    suspend fun getTcrSeason() = client.get("tcr/season").body<ApiResponse<ChampSeason>>().requireData()
    suspend fun getTcrSubstation() = client.get("tcr/substation").body<ApiResponse<ChampSubstation>>().requireData()
    suspend fun getTcrDriver(id: Int) = client.get("tcr/driver") { parameter("tcr_id", id) }.body<ApiResponse<ChampSeason>>().requireData()
    suspend fun getTcrTeam(id: Int) = client.get("tcr/team") { parameter("tcr_id", id) }.body<ApiResponse<ChampSeason>>().requireData()

    suspend fun getAppVersion() = client.get("index/ver").body<ApiResponse<AppVersion>>().requireData()

    suspend fun preloadHomeThenSecondary(seasonId: Int) {
        supervisorScope {
            val navigation = async { runSuspendCatching { getNavTabs() } }
            val headlines = async { runSuspendCatching { getNewsList(tagId = 1, page = 1) } }
            navigation.await()
            headlines.await()
        }

        supervisorScope {
            launch { runSuspendCatching { getF1Calendar(seasonId) } }
            launch { runSuspendCatching { getF1EnglishCalendar(seasonId) } }
            launch { runSuspendCatching { getRaceSchedule() } }
            launch { runSuspendCatching { getRaceList(chpId = 6, seasonId = seasonId) } }
            launch { runSuspendCatching { getStationList(chpId = 6, seasonId = seasonId) } }
            launch {
                val option = runSuspendCatching {
                    val f1Options = getRankingNav().list
                        .flatMap { it.options }
                        .filter { it.chp_id == 6 }
                    f1Options.firstOrNull { it.id == seasonId } ?: f1Options.firstOrNull()
                }.getOrNull() ?: return@launch
                supervisorScope {
                    launch { runSuspendCatching { getDriverRanking(option.chp_id, option.id) } }
                    launch { runSuspendCatching { getTeamRanking(option.chp_id, option.id) } }
                }
            }
        }
    }

    private companion object {
        val StartupCacheLifetime = 2.minutes
        const val MaxCommentPages = 50
        const val MaxConcurrentCommentPageRequests = 6
        const val MaxCommentReplyPages = 50
        const val MaxConcurrentCommentReplyRequests = 6
        const val MaxCacheEntries = 512
        const val F1CalendarUrl =
            "https://files-f1.motorsportcalendars.com/zh/f1-calendar_p1_p2_p3_qualifying_sprint_gp.ics"
        const val F1EnglishCalendarUrl =
            "https://files-f1.motorsportcalendars.com/f1-calendar_gp.ics"
        const val Formula1ApiBase = "https://api.formula1.com"
        const val Formula1PublicApiKey = "BQ1SiSmLUOsp460VzXBlLrh689kGgYEZ"
    }
}

internal fun String.parseF1Calendar(seasonId: Int): List<F1CalendarEvent> {
    // RFC 5545 allows long property values to continue on a whitespace-prefixed line.
    val unfolded = mutableListOf<String>()
    lineSequence().forEach { rawLine ->
        val line = rawLine.trimEnd('\r')
        if ((line.startsWith(' ') || line.startsWith('\t')) && unfolded.isNotEmpty()) {
            unfolded[unfolded.lastIndex] += line.drop(1)
        } else {
            unfolded += line
        }
    }

    val events = mutableListOf<F1CalendarEvent>()
    var properties: MutableMap<String, String>? = null
    unfolded.forEach { line ->
        when (line) {
            "BEGIN:VEVENT" -> properties = linkedMapOf()
            "END:VEVENT" -> {
                val values = properties
                if (values != null) {
                    val start = values["DTSTART"].orEmpty()
                    val status = values["STATUS"].orEmpty().uppercase().ifBlank { "CONFIRMED" }
                    if (
                        start.take(4).toIntOrNull() == seasonId &&
                        (status == "CONFIRMED" || status == "TENTATIVE")
                    ) {
                        events += F1CalendarEvent(
                            uid = values["UID"].orEmpty().decodeIcsText(),
                            summary = values["SUMMARY"].orEmpty().decodeIcsText(),
                            startUtc = start,
                            endUtc = values["DTEND"].orEmpty(),
                            location = values["LOCATION"].orEmpty().decodeIcsText(),
                            status = status
                        )
                    }
                }
                properties = null
            }
            else -> {
                val separator = line.indexOf(':')
                if (separator > 0) {
                    val key = line.substring(0, separator).substringBefore(';').uppercase()
                    properties?.set(key, line.substring(separator + 1))
                }
            }
        }
    }
    return events
}

private fun String.decodeIcsText(): String = buildString(length) {
    var index = 0
    while (index < this@decodeIcsText.length) {
        val char = this@decodeIcsText[index]
        if (char != '\\' || index == this@decodeIcsText.lastIndex) {
            append(char)
            index++
            continue
        }
        when (val escaped = this@decodeIcsText[index + 1]) {
            'n', 'N' -> append('\n')
            '\\', ',', ';', '(', ')' -> append(escaped)
            else -> {
                append('\\')
                append(escaped)
            }
        }
        index += 2
    }
}
