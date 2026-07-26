package com.racingdaily.data.remote

import com.racingdaily.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.parameter
import io.ktor.http.Parameters
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
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
        return data
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T : Any> cached(
        key: String,
        forceRefresh: Boolean = false,
        loader: suspend () -> T
    ): T {
        var cachedValue: Any? = null
        var producer = false
        lateinit var request: CompletableDeferred<Any>

        cacheMutex.withLock {
            if (forceRefresh) cache.remove(key)
            val entry = cache[key]
            if (entry != null && entry.storedAt.elapsedNow() < StartupCacheLifetime) {
                cachedValue = entry.value
            } else {
                if (entry != null) cache.remove(key)
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
                if (inFlight[key] === request) inFlight.remove(key)
            }
            request.complete(value)
            value
        } catch (error: Throwable) {
            cacheMutex.withLock {
                if (inFlight[key] === request) inFlight.remove(key)
            }
            request.completeExceptionally(error)
            throw error
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
        val comments = getArticleComments(articleId, page = 0)
        val completeComments = supervisorScope {
            comments.comment_list.map { comment ->
                async {
                    if (comment.sub_count <= comment.sub_list.size) {
                        comment
                    } else {
                        val replies = runCatching {
                            getAllCommentReplies(comment.id, comment.sub_count)
                        }.getOrDefault(comment.sub_list)
                        comment.copy(sub_list = replies)
                    }
                }
            }.awaitAll()
        }
        return comments.copy(comment_list = completeComments)
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
            val navigation = async { runCatching { getNavTabs() } }
            val headlines = async { runCatching { getNewsList(tagId = 1, page = 1) } }
            navigation.await()
            headlines.await()
        }

        supervisorScope {
            launch { runCatching { getRaceSchedule() } }
            launch { runCatching { getRaceList(chpId = 6, seasonId = seasonId) } }
            launch {
                val stations = runCatching { getStationList(chpId = 6, seasonId = seasonId).tmp }
                    .getOrDefault(emptyList())
                stations.takeLast(4).forEach { station ->
                    val navigation = runCatching { getStationRank(station.gp_id).navbar }
                        .getOrDefault(emptyList())
                        .filter { it.key_name in PreloadedStationSessionKeys }
                    navigation.map { nav ->
                        async { runCatching { getStationScore(station.gp_id, nav.id) } }
                    }.awaitAll()
                }
            }
            launch {
                val option = runCatching {
                    getRankingNav().list
                        .flatMap { it.options }
                        .firstOrNull { it.id == seasonId }
                        ?: getRankingNav().list.flatMap { it.options }.firstOrNull()
                }.getOrNull() ?: return@launch
                supervisorScope {
                    launch { runCatching { getDriverRanking(option.chp_id, option.id) } }
                    launch { runCatching { getTeamRanking(option.chp_id, option.id) } }
                }
            }
        }
    }

    private companion object {
        val StartupCacheLifetime = 2.minutes
        const val MaxCommentReplyPages = 50
        val PreloadedStationSessionKeys = setOf(
            "fp1cj", "fp2cj", "fp3cj", "ccpws", "ccpwscj", "ccscj", "pwscj", "zscj"
        )
    }
}
