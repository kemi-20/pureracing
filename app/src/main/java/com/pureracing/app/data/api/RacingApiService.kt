package com.pureracing.app.data.api

import com.pureracing.app.data.model.*
import retrofit2.http.*

interface RacingApiService {

    // 赛季
    @GET("season")
    suspend fun getSeasons(): ApiResponse<List<Season>>

    @GET("season/{id}")
    suspend fun getSeasonDetail(@Path("id") id: Int): ApiResponse<SeasonDetail>

    // 赛程
    @GET("schedule")
    suspend fun getSchedule(@Query("season_id") seasonId: Int): ApiResponse<List<RaceSchedule>>

    // 赛事详情
    @GET("race/race_detail")
    suspend fun getRaceDetail(@Query("race_id") raceId: Int): ApiResponse<RaceDetail>

    // 排名
    @GET("rank")
    suspend fun getRankings(
        @Query("season_id") seasonId: Int,
        @Query("type") type: String  // driver / constructor
    ): ApiResponse<List<RankItem>>

    // 车队
    @GET("team")
    suspend fun getTeams(@Query("season_id") seasonId: Int): ApiResponse<List<Team>>

    @GET("team/{id}")
    suspend fun getTeamDetail(@Path("id") id: Int): ApiResponse<TeamDetail>

    // 新闻
    @GET("news")
    suspend fun getNews(@Query("page") page: Int = 1): ApiResponse<PagedData<NewsItem>>

    // 收藏
    @PATCH("collection_patch")
    suspend fun toggleCollection(@Body body: CollectionRequest): ApiResponse<Unit>
}
