package com.racingdaily.data.remote

import com.racingdaily.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class ApiService(private val client: HttpClient) {

    // === News ===
    suspend fun getNewsList(tagId: Int, page: Int = 1): NewsListData =
        client.get("index/index") {
            parameter("tag_id", tagId)
            parameter("page", page)
        }.body<ApiResponse<NewsListData>>().data

    suspend fun getNewsDetail(id: Int): NewsDetail =
        client.get("index/detail") {
            parameter("id", id)
        }.body<ApiResponse<NewsDetail>>().data

    suspend fun getNavTabs(): Navitv2Data =
        client.get("index/navitv2").body<ApiResponse<Navitv2Data>>().data

    suspend fun getTrackHome(gpId: Int): NewsListData =
        client.get("index/trackhome") {
            parameter("gp_id", gpId)
        }.body<ApiResponse<NewsListData>>().data

    // === Race ===
    suspend fun getRaceSchedule(): List<RaceGp> =
        client.get("race/index").body<ApiResponse<List<RaceGp>>>().data

    // === Rankings ===
    suspend fun getRankingNavigation(): RankingNavData =
        client.get("rank/navigationv2").body<ApiResponse<RankingNavData>>().data

    suspend fun getDriverRanking(chpId: Int, seasonId: Int): RankingData =
        client.get("rank/driver") {
            parameter("chp_id", chpId)
            parameter("season_id", seasonId)
        }.body<ApiResponse<RankingData>>().data

    suspend fun getTeamRanking(chpId: Int, seasonId: Int): RankingData =
        client.get("rank/team") {
            parameter("chp_id", chpId)
            parameter("season_id", seasonId)
        }.body<ApiResponse<RankingData>>().data

    // === Stations ===
    suspend fun getStationList(chpId: Int, seasonId: Int): StationData =
        client.get("station/list") {
            parameter("chp_id", chpId)
            parameter("season_id", seasonId)
        }.body<ApiResponse<StationData>>().data

    suspend fun getStationRank(gpId: Int): StationRankData =
        client.get("station/rank") {
            parameter("gp_id", gpId)
        }.body<ApiResponse<StationRankData>>().data

    suspend fun getStationScore(gpId: Int): List<kotlinx.serialization.json.JsonObject> =
        client.get("station/score") {
            parameter("gp_id", gpId)
        }.body<ApiResponse<List<kotlinx.serialization.json.JsonObject>>>().data

    // === Tracks ===
    suspend fun getTrackInfo(trackId: Int): TrackData =
        client.get("track/index") {
            parameter("track_id", trackId)
        }.body<ApiResponse<TrackData>>().data

    suspend fun getTrackScore(trackId: Int): TrackScoreData =
        client.get("track/score") {
            parameter("track_id", trackId)
        }.body<ApiResponse<TrackScoreData>>().data

    // === Drivers ===
    suspend fun getDriverPhotos(chpId: Int, driverId: Int): List<kotlinx.serialization.json.JsonObject> =
        client.get("driver/photo") {
            parameter("chp_id", chpId)
            parameter("driver_id", driverId)
        }.body<ApiResponse<List<kotlinx.serialization.json.JsonObject>>>().data

    // === Teams ===
    suspend fun getTeamScore(chpId: Int, teamId: Int): TeamScoreData =
        client.get("team/score") {
            parameter("chp_id", chpId)
            parameter("team_id", teamId)
        }.body<ApiResponse<TeamScoreData>>().data

    // === Custom Championship ===
    suspend fun getCustomSeason(): ChampionshipSeason =
        client.get("custom/season").body<ApiResponse<ChampionshipSeason>>().data

    suspend fun getCustomSubstations(): ChampionshipSubstation =
        client.get("custom/substation").body<ApiResponse<ChampionshipSubstation>>().data

    suspend fun getCustomRank(customId: Int): StationData =
        client.get("custom/rank") {
            parameter("custom_id", customId)
        }.body<ApiResponse<StationData>>().data

    suspend fun getCustomScore(customId: Int): List<kotlinx.serialization.json.JsonObject> =
        client.get("custom/score") {
            parameter("custom_id", customId)
        }.body<ApiResponse<List<kotlinx.serialization.json.JsonObject>>>().data

    suspend fun getCustomDriver(customId: Int): ChampionshipSeason =
        client.get("custom/driver") {
            parameter("custom_id", customId)
        }.body<ApiResponse<ChampionshipSeason>>().data

    suspend fun getCustomTeam(customId: Int): ChampionshipSeason =
        client.get("custom/team") {
            parameter("custom_id", customId)
        }.body<ApiResponse<ChampionshipSeason>>().data

    // === MotoGP ===
    suspend fun getMotogpSeason(): ChampionshipSeason =
        client.get("motogp/season").body<ApiResponse<ChampionshipSeason>>().data

    suspend fun getMotogpSubstations(): ChampionshipSubstation =
        client.get("motogp/substation").body<ApiResponse<ChampionshipSubstation>>().data

    suspend fun getMotogpRank(motogpId: Int): StationData =
        client.get("motogp/rank") {
            parameter("motogp_id", motogpId)
        }.body<ApiResponse<StationData>>().data

    suspend fun getMotogpScore(motogpId: Int): List<kotlinx.serialization.json.JsonObject> =
        client.get("motogp/score") {
            parameter("motogp_id", motogpId)
        }.body<ApiResponse<List<kotlinx.serialization.json.JsonObject>>>().data

    suspend fun getMotogpDriver(motogpId: Int): ChampionshipSeason =
        client.get("motogp/driver") {
            parameter("motogp_id", motogpId)
        }.body<ApiResponse<ChampionshipSeason>>().data

    suspend fun getMotogpTeam(motogpId: Int): ChampionshipSeason =
        client.get("motogp/team") {
            parameter("motogp_id", motogpId)
        }.body<ApiResponse<ChampionshipSeason>>().data

    suspend fun getMotogpManufacturer(motogpId: Int): ChampionshipSeason =
        client.get("motogp/manufacturer") {
            parameter("motogp_id", motogpId)
        }.body<ApiResponse<ChampionshipSeason>>().data

    // === TCR ===
    suspend fun getTcrSeason(): ChampionshipSeason =
        client.get("tcr/season").body<ApiResponse<ChampionshipSeason>>().data

    suspend fun getTcrSubstations(): ChampionshipSubstation =
        client.get("tcr/substation").body<ApiResponse<ChampionshipSubstation>>().data

    suspend fun getTcrRank(tcrId: Int): StationData =
        client.get("tcr/rank") {
            parameter("tcr_id", tcrId)
        }.body<ApiResponse<StationData>>().data

    suspend fun getTcrScore(tcrId: Int): List<kotlinx.serialization.json.JsonObject> =
        client.get("tcr/score") {
            parameter("tcr_id", tcrId)
        }.body<ApiResponse<List<kotlinx.serialization.json.JsonObject>>>().data

    suspend fun getTcrDriver(tcrId: Int): ChampionshipSeason =
        client.get("tcr/driver") {
            parameter("tcr_id", tcrId)
        }.body<ApiResponse<ChampionshipSeason>>().data

    suspend fun getTcrTeam(tcrId: Int): ChampionshipSeason =
        client.get("tcr/team") {
            parameter("tcr_id", tcrId)
        }.body<ApiResponse<ChampionshipSeason>>().data

    // === System ===
    suspend fun getAppVersion(): AppVersion =
        client.get("index/ver").body<ApiResponse<AppVersion>>().data

    suspend fun getReferer(): RefererData =
        client.get("index/referer").body<ApiResponse<RefererData>>().data
}
