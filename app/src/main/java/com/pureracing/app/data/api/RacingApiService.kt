package com.pureracing.app.data.api

import com.pureracing.app.data.model.*
import retrofit2.http.*

interface RacingApiService {

    // 认证
    @POST("v1/login/phone_account_login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<LoginData>

    @POST("v1/login/set_password")
    suspend fun setPassword(@Body body: SetPasswordRequest): ApiResponse<Unit>

    @POST("v1/update-pwd")
    suspend fun updatePassword(@Body body: UpdatePasswordRequest): ApiResponse<Unit>

    // 赛季
    @GET("v1/season")
    suspend fun getSeasons(): ApiResponse<List<Season>>

    @GET("v1/season/{id}")
    suspend fun getSeasonDetail(@Path("id") id: Int): ApiResponse<SeasonDetail>

    // 赛程
    @GET("v1/schedule")
    suspend fun getSchedule(@Query("season_id") seasonId: Int): ApiResponse<List<RaceSchedule>>

    // 赛事详情
    @GET("v1/race/race_detail")
    suspend fun getRaceDetail(@Query("race_id") raceId: Int): ApiResponse<RaceDetail>

    // 排名
    @GET("v1/rank")
    suspend fun getRankings(
        @Query("season_id") seasonId: Int,
        @Query("type") type: String  // driver / constructor
    ): ApiResponse<List<RankItem>>

    // 车队
    @GET("v1/team")
    suspend fun getTeams(@Query("season_id") seasonId: Int): ApiResponse<List<Team>>

    @GET("v1/team/{id}")
    suspend fun getTeamDetail(@Path("id") id: Int): ApiResponse<TeamDetail>

    // 新闻
    @GET("v1/news")
    suspend fun getNews(@Query("page") page: Int = 1): ApiResponse<PagedData<NewsItem>>

    // 收藏
    @PATCH("v1/collection_patch")
    suspend fun toggleCollection(@Body body: CollectionRequest): ApiResponse<Unit>
}
