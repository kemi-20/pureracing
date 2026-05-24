package com.racingdaily.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

object FlexibleStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val input = decoder as? JsonDecoder ?: return decoder.decodeString()
        val element = input.decodeJsonElement()
        if (element is JsonNull) return ""
        return when (element) {
            is JsonPrimitive -> element.content
            else -> element.toString()
        }
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}

object FlexibleIntSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleInt", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Int {
        val input = decoder as? JsonDecoder ?: return decoder.decodeInt()
        val element = input.decodeJsonElement()
        if (element is JsonNull) return 0
        return when (element) {
            is JsonPrimitive -> element.intOrNull ?: element.longOrNull?.toInt() ?: element.content.toIntOrNull() ?: 0
            else -> 0
        }
    }

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeInt(value)
    }
}

@Serializable data class ApiResponse<T>(val code: Int, val msg: String = "", val data: T)

// News
@Serializable data class NewsItem(@Serializable(with = FlexibleIntSerializer::class) val id: Int = 0, val title: String = "", val istop: Int = 0, val total_read: Int = 0,
    val type: Int = 0, val publish_time: Long = 0, @Serializable(with = FlexibleStringSerializer::class) val gp_id: String = "", val http_url: String = "",
    val covers: List<Cover> = emptyList(), val tags: List<Tag> = emptyList())
@Serializable data class Cover(val path_url: String = "", val path: String = "")
@Serializable data class Tag(val id: Int = 0, val name: String = "")
@Serializable data class NewsListData(val list: List<NewsItem> = emptyList(), val next_page: Int = 0)
@Serializable data class NewsDetail(val details: ArticleDetail = ArticleDetail())
@Serializable data class ArticleDetail(val id: Int = 0, val title: String = "", val content: String = "",
    val total_read: Int = 0, val author: String = "", val user_name: String = "", val source_link: String = "",
    val temotime: String = "", val iteam: List<kotlinx.serialization.json.JsonObject> = emptyList(),
    val tag: List<String> = emptyList(), val conten: String = "")
@Serializable data class NavTab(val id: Int = 0, val name: String = "")
@Serializable data class Navitv2Data(val navbar: List<NavTab> = emptyList())

// Race
@Serializable data class RaceGp(val race_time: String = "", val race_time_detail: String = "",
    @Serializable(with = FlexibleStringSerializer::class) val gp_id: String = "", val gp_name: String = "", val chp_name: String = "", val chp_logo: String = "",
    val gp_logo: String = "", val track_name: String = "", val track_id: Int = 0,
    val weather: WeatherInfo? = null, val session: List<RaceSession> = emptyList())
@Serializable data class WeatherInfo(val temp: String = "", val icon: String = "")
@Serializable data class RaceSession(val session_id: Int = 0, val session_name: List<String> = emptyList(),
    val session_type: Int = 0, val hour: List<String> = emptyList(), val race_status: Int = 0,
    val race_result: List<SessionResult> = emptyList())
@Serializable data class SessionResult(val rank: Int = 0, @Serializable(with = FlexibleStringSerializer::class) val driverid: String = "", val dr_name: String = "",
    @Serializable(with = FlexibleStringSerializer::class) val teamid: String = "", val team_logo: String = "", val gap: String = "")

// Ranking
@Serializable data class RankingNavData(val list: List<RankingNavItem> = emptyList())
@Serializable data class RankingNavItem(val options: List<RankingOption> = emptyList())
@Serializable data class RankingOption(val chp_id: Int = 0, val id: Int = 0, @Serializable(with = FlexibleStringSerializer::class) val name: String = "", val custom_name: String = "")
@Serializable data class RankingData(val list: List<RankingTab> = emptyList())
@Serializable data class RankingTab(val tab_key: String = "", val tab_name: String = "", val remark: String = "",
    val list: List<kotlinx.serialization.json.JsonObject> = emptyList())

// Station
@Serializable data class StationItem(val gp_id: Int = 0, val chinese_name: String = "", val number: String = "")
@Serializable data class StationData(val tmp: List<StationItem> = emptyList())
@Serializable data class StationRankData(val navbar: List<StationNavItem> = emptyList())
@Serializable data class StationNavItem(val id: Int = 0, val key_name: String = "", val name: String = "")

// Track
@Serializable data class TrackInfo(val id: Int = 0, val name: String = "", val chinese_name: String = "",
    val country: String = "", val location: String = "", val map_img: String = "")
@Serializable data class TrackData(val track: TrackInfo = TrackInfo())
@Serializable data class TrackScoreData(val history: List<kotlinx.serialization.json.JsonObject> = emptyList())

// Team
@Serializable data class TeamScoreData(val history: kotlinx.serialization.json.JsonObject? = null)

// Championship
@Serializable data class ChampSeason(val remark: String = "", val th_data: List<String> = emptyList(),
    val tr_data: List<List<ChampCell>> = emptyList())
@Serializable data class ChampCell(val type: Int = 0, val text_color: String = "", val content: String = "")
@Serializable data class ChampSubstation(val tmp: List<ChampSub> = emptyList())
@Serializable data class ChampSub(val season_name: String = "", val custom_id: Int = 0, val motogp_id: Int = 0, val tcr_id: Int = 0)
@Serializable data class ChampRankData(val tmp: List<kotlinx.serialization.json.JsonObject> = emptyList())

// System
@Serializable data class AppVersion(val ver: Int = 0, val title: String = "", val desc: String = "",
    val is_force: Int = 0, val ad_dowload_url: String = "", val ios_dowload_url: String = "")
