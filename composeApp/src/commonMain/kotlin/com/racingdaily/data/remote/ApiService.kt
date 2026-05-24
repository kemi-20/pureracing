package com.racingdaily.data.remote

import com.racingdaily.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class ApiService(private val client: HttpClient) {

    suspend fun getNewsList(tagId: Int, page: Int = 1) =
        client.get("index/index") { parameter("tag_id", tagId); parameter("page", page) }
            .body<ApiResponse<NewsListData>>().data

    suspend fun getNewsDetail(id: Int) =
        client.get("index/detail") { parameter("id", id) }.body<ApiResponse<NewsDetail>>().data

    suspend fun getNavTabs() =
        client.get("index/navitv2").body<ApiResponse<Navitv2Data>>().data

    suspend fun getRaceSchedule() =
        client.get("race/index").body<ApiResponse<List<RaceGp>>>().data

    suspend fun getRankingNav() =
        client.get("rank/navigationv2").body<ApiResponse<RankingNavData>>().data

    suspend fun getDriverRanking(chpId: Int, seasonId: Int) =
        client.get("rank/driver") { parameter("chp_id", chpId); parameter("season_id", seasonId) }
            .body<ApiResponse<RankingData>>().data

    suspend fun getTeamRanking(chpId: Int, seasonId: Int) =
        client.get("rank/team") { parameter("chp_id", chpId); parameter("season_id", seasonId) }
            .body<ApiResponse<RankingData>>().data

    suspend fun getStationList(chpId: Int, seasonId: Int) =
        client.get("station/list") { parameter("chp_id", chpId); parameter("season_id", seasonId) }
            .body<ApiResponse<StationData>>().data

    suspend fun getStationRank(gpId: Int) =
        client.get("station/rank") { parameter("gp_id", gpId) }.body<ApiResponse<StationRankData>>().data

    suspend fun getTrackInfo(trackId: Int) =
        client.get("track/index") { parameter("track_id", trackId) }.body<ApiResponse<TrackData>>().data

    suspend fun getTrackScore(trackId: Int) =
        client.get("track/score") { parameter("track_id", trackId) }.body<ApiResponse<TrackScoreData>>().data

    suspend fun getTeamScore(chpId: Int, teamId: Int) =
        client.get("team/score") { parameter("chp_id", chpId); parameter("team_id", teamId) }
            .body<ApiResponse<TeamScoreData>>().data

    suspend fun getCustomSeason() = client.get("custom/season").body<ApiResponse<ChampSeason>>().data
    suspend fun getCustomSubstation() = client.get("custom/substation").body<ApiResponse<ChampSubstation>>().data
    suspend fun getCustomDriver(id: Int) = client.get("custom/driver") { parameter("custom_id", id) }.body<ApiResponse<ChampSeason>>().data
    suspend fun getCustomTeam(id: Int) = client.get("custom/team") { parameter("custom_id", id) }.body<ApiResponse<ChampSeason>>().data

    suspend fun getMotogpSeason() = client.get("motogp/season").body<ApiResponse<ChampSeason>>().data
    suspend fun getMotogpSubstation() = client.get("motogp/substation").body<ApiResponse<ChampSubstation>>().data
    suspend fun getMotogpDriver(id: Int) = client.get("motogp/driver") { parameter("motogp_id", id) }.body<ApiResponse<ChampSeason>>().data
    suspend fun getMotogpTeam(id: Int) = client.get("motogp/team") { parameter("motogp_id", id) }.body<ApiResponse<ChampSeason>>().data
    suspend fun getMotogpManufacturer(id: Int) = client.get("motogp/manufacturer") { parameter("motogp_id", id) }.body<ApiResponse<ChampSeason>>().data

    suspend fun getTcrSeason() = client.get("tcr/season").body<ApiResponse<ChampSeason>>().data
    suspend fun getTcrSubstation() = client.get("tcr/substation").body<ApiResponse<ChampSubstation>>().data
    suspend fun getTcrDriver(id: Int) = client.get("tcr/driver") { parameter("tcr_id", id) }.body<ApiResponse<ChampSeason>>().data
    suspend fun getTcrTeam(id: Int) = client.get("tcr/team") { parameter("tcr_id", id) }.body<ApiResponse<ChampSeason>>().data

    suspend fun getAppVersion() = client.get("index/ver").body<ApiResponse<AppVersion>>().data
}
