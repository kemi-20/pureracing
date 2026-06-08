package com.racingdaily.data.remote

import com.racingdaily.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class ApiService(private val client: HttpClient) {

    private fun <T> ApiResponse<T>.requireData(): T {
        if (code != 200) error(msg.ifBlank { "API request failed with code $code" })
        return data
    }

    suspend fun getNewsList(tagId: Int, page: Int = 1) =
        client.get("index/index") { parameter("tag_id", tagId); parameter("page", page) }
            .body<ApiResponse<NewsListData>>().requireData()

    suspend fun getNewsDetail(id: Int) =
        client.get("index/detail") { parameter("id", id) }.body<ApiResponse<NewsDetail>>().requireData()

    suspend fun getNavTabs() =
        client.get("index/navitv2").body<ApiResponse<Navitv2Data>>().requireData()

    suspend fun getRaceSchedule() =
        client.get("race/index").body<ApiResponse<List<RaceGp>>>().requireData()

    suspend fun getRankingNav() =
        client.get("rank/navigationv2").body<ApiResponse<RankingNavData>>().requireData()

    suspend fun getDriverRanking(chpId: Int, seasonId: Int) =
        client.get("rank/driver") { parameter("chp_id", chpId); parameter("season_id", seasonId) }
            .body<ApiResponse<RankingData>>().requireData()

    suspend fun getTeamRanking(chpId: Int, seasonId: Int) =
        client.get("rank/team") { parameter("chp_id", chpId); parameter("season_id", seasonId) }
            .body<ApiResponse<RankingData>>().requireData()

    suspend fun getStationList(chpId: Int, seasonId: Int) =
        client.get("station/list") { parameter("chp_id", chpId); parameter("season_id", seasonId) }
            .body<ApiResponse<StationData>>().requireData()

    suspend fun getStationRank(gpId: Int) =
        client.get("station/rank") { parameter("gp_id", gpId) }.body<ApiResponse<StationRankData>>().requireData()

    suspend fun getStationScore(gpId: Int) =
        client.get("station/score") { parameter("gp_id", gpId) }.body<ApiResponse<kotlinx.serialization.json.JsonElement>>().requireData()

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
}
