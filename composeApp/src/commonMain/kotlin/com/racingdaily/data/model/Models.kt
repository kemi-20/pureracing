package com.racingdaily.data.model

import kotlinx.serialization.Serializable

// Generic API response wrapper
@Serializable
data class ApiResponse<T>(
    val code: Int,
    val msg: String = "",
    val data: T
)

// === News ===
@Serializable
data class NewsItem(
    val id: Int = 0,
    val title: String = "",
    val istop: Int = 0,
    val total_read: Int = 0,
    val type: Int = 0,
    val slide_time: Long = 0,
    val publish_time: Long = 0,
    val article_jump: Int = 0,
    val gp_id: String = "",
    val http_url: String = "",
    val covers: List<NewsCover> = emptyList(),
    val tags: List<NewsTag> = emptyList()
)

@Serializable
data class NewsCover(
    val id: Int = 0,
    val article_id: Int = 0,
    val path: String = "",
    val paths: String = "",
    val format_ind: Int = 0,
    val path_url: String = ""
)

@Serializable
data class NewsTag(
    val id: Int = 0,
    val name: String = ""
)

@Serializable
data class NewsListData(
    val navigation: kotlinx.serialization.json.JsonObject? = null,
    val list: List<NewsItem> = emptyList(),
    val next_page: Int = 0
)

@Serializable
data class NewsDetail(
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val create_time: String = "",
    val source: String = "",
    val total_read: Int = 0,
    val covers: List<NewsCover> = emptyList(),
    val tags: List<NewsTag> = emptyList()
)

@Serializable
data class NavTab(
    val id: Int = 0,
    val name: String = "",
    val tag_state: Int = 0,
    val title_state: Int = 0,
    val content_h_url: String? = null,
    val title_url: String? = null,
    val title_url_off: String? = null,
    val tag_status: String? = null
)

@Serializable
data class Navitv2Data(
    val navbar: List<NavTab> = emptyList(),
    val status: kotlinx.serialization.json.JsonObject? = null,
    val state: kotlinx.serialization.json.JsonObject? = null,
    val edition: String = ""
)

// === Race ===
@Serializable
data class RaceGp(
    val race_time: String = "",
    val race_time_detail: String = "",
    val gp_id: String = "",
    val gp_name: String = "",
    val chp_name: String = "",
    val chp_logo: String = "",
    val gp_logo: String = "",
    val track_name: String = "",
    val track_id: Int = 0,
    val weather: WeatherInfo? = null,
    val session: List<RaceSession> = emptyList()
)

@Serializable
data class WeatherInfo(
    val temp: String = "",
    val icon: String = ""
)

@Serializable
data class RaceSession(
    val session_id: Int = 0,
    val session_name: List<String> = emptyList(),
    val session_type: Int = 0,
    val hour: List<String> = emptyList(),
    val race_status: Int = 0,
    val race_result: List<SessionResult> = emptyList(),
    val live_source: List<LiveSource> = emptyList(),
    val live_broadcast: List<LiveBroadcast> = emptyList()
)

@Serializable
data class SessionResult(
    val rank: Int = 0,
    val driverid: String = "",
    val dr_name: String = "",
    val teamid: String = "",
    val team_logo: String = "",
    val gap: String = ""
)

@Serializable
data class LiveSource(
    val live_name: String = "",
    val live_commentary: List<String> = emptyList(),
    val live_status: Int = 0,
    val live_ispay: Int = 0,
    val live_url: kotlinx.serialization.json.JsonPrimitive? = null
)

@Serializable
data class LiveBroadcast(
    val live_name: String = "",
    val live_commentary: List<String> = emptyList(),
    val live_status: Int = 0,
    val live_states: String = "",
    val live_url: String = ""
)

// === Ranking ===
@Serializable
data class RankingNavData(
    val default_select: Int = 0,
    val list: List<RankingNavItem> = emptyList()
)

@Serializable
data class RankingNavItem(
    val is_select: Int = 0,
    val nav_key: String = "",
    val nav_name: String = "",
    val tab_sort: Int = 0,
    val default_select_option: Int = 0,
    val default_select_sub: Int = 0,
    val options: List<RankingOption> = emptyList()
)

@Serializable
data class RankingOption(
    val chp_id: Int = 0,
    val id: Int = 0,
    val name: String = "",
    val is_choice: Int = 0,
    val is_tab: Int = 0,
    val custom_name: String = ""
)

@Serializable
data class RankingData(
    val default_select: Int = 0,
    val list: List<RankingTab> = emptyList()
)

@Serializable
data class RankingTab(
    val tab_key: String = "",
    val tab_name: String = "",
    val remark: String = "",
    val list: List<kotlinx.serialization.json.JsonObject> = emptyList()
)

// === Station ===
@Serializable
data class StationData(
    val isselect: Int = 0,
    val tmp: List<StationItem> = emptyList()
)

@Serializable
data class StationItem(
    val gp_id: Int = 0,
    val chinese_name: String = "",
    val number: String = ""
)

@Serializable
data class StationRankData(
    val isselect: Int = 0,
    val navbar: List<StationNavItem> = emptyList()
)

@Serializable
data class StationNavItem(
    val id: Int = 0,
    val key_name: String = "",
    val name: String = "",
    val is_select: Int = 0,
    val remark: String = "",
    val tab_sort: Int = 0,
    val tab_type: Int = 0,
    val is_show: Int = 0,
    val is_type: Int = 0
)

// === Track ===
@Serializable
data class TrackData(
    val track: TrackInfo = TrackInfo(),
    val change: kotlinx.serialization.json.JsonObject? = null,
    val t_data: kotlinx.serialization.json.JsonObject? = null
)

@Serializable
data class TrackInfo(
    val id: Int = 0,
    val name: String = "",
    val chinese_name: String = "",
    val addr_chinese_name: String = "",
    val map_img: String = "",
    val country: String = "",
    val location: String = "",
    val longitude: String = "",
    val latitude: String = ""
)

@Serializable
data class TrackScoreData(
    val single: kotlinx.serialization.json.JsonObject? = null,
    val champion: kotlinx.serialization.json.JsonObject? = null,
    val history: List<kotlinx.serialization.json.JsonObject> = emptyList(),
    val driver_history: List<kotlinx.serialization.json.JsonObject> = emptyList()
)

// === Championship ===
@Serializable
data class ChampionshipSeason(
    val remark: String = "",
    val th_data: List<String> = emptyList(),
    val tr_data: List<List<ChampionshipCell>> = emptyList(),
    val config: kotlinx.serialization.json.JsonObject? = null
)

@Serializable
data class ChampionshipCell(
    val type: Int = 0,
    val text_color: String = "",
    val content: String = ""
)

@Serializable
data class ChampionshipSubstation(
    val isselect: Int = 0,
    val tmp: List<SubstationItem> = emptyList()
)

@Serializable
data class SubstationItem(
    val season_name: String = "",
    val custom_id: Int = 0,
    val motogp_id: Int = 0,
    val tcr_id: Int = 0,
    val tcrch_id: Int = 0
)

// === System ===
@Serializable
data class AppVersion(
    val ver: Int = 0,
    val title: String = "",
    val desc: String = "",
    val is_force: Int = 0,
    val ad_dowload_url: String = "",
    val ios_dowload_url: String = ""
)

@Serializable
data class RefererData(
    val content: String = ""
)

// === Team ===
@Serializable
data class TeamScoreData(
    val honor: kotlinx.serialization.json.JsonObject? = null,
    val summary: kotlinx.serialization.json.JsonObject? = null,
    val history: kotlinx.serialization.json.JsonObject? = null
)
