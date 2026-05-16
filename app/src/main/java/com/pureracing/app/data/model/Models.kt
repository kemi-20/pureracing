package com.pureracing.app.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val code: Int,
    val msg: String,
    val data: T?
)

data class PagedData<T>(
    val list: List<T>,
    val total: Int,
    val page: Int
)

// 赛季
data class Season(val id: Int, val name: String, val year: Int)
data class SeasonDetail(val id: Int, val name: String, val year: Int, val rounds: Int)

// 赛程
data class RaceSchedule(
    val id: Int,
    val name: String,
    val circuit: String,
    val country: String,
    @SerializedName("race_time") val raceTime: String,
    val round: Int,
    val status: Int  // 0=未开始 1=进行中 2=已结束
)

// 赛事详情
data class RaceDetail(
    val id: Int,
    val name: String,
    val circuit: String,
    val results: List<RaceResult>,
    @SerializedName("fastest_lap") val fastestLap: FastestLap?
)

data class RaceResult(
    val position: Int,
    val driver: String,
    val team: String,
    val time: String,
    val points: Int,
    val tire: String
)

data class FastestLap(val driver: String, val lap: Int, val time: String)

// 排名
data class RankItem(
    val position: Int,
    val name: String,
    val team: String,
    val points: Int,
    val wins: Int,
    val nationality: String,
    val avatar: String
)

// 车队
data class Team(val id: Int, val name: String, val logo: String, val nationality: String)
data class TeamDetail(
    val id: Int,
    val name: String,
    val logo: String,
    val drivers: List<String>,
    val points: Int,
    val wins: Int
)

// 新闻
data class NewsItem(
    val id: Int,
    val title: String,
    val cover: String,
    val summary: String,
    @SerializedName("created_at") val createdAt: String
)

// 收藏
data class CollectionRequest(val type: String, val id: Int, val action: Int)  // action: 1=收藏 0=取消
