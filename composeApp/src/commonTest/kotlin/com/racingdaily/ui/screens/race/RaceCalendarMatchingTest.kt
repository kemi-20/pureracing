package com.racingdaily.ui.screens.race

import com.racingdaily.data.model.F1CalendarEvent
import com.racingdaily.data.model.RaceListItem
import com.racingdaily.data.model.RaceGp
import com.racingdaily.data.model.StationItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RaceCalendarMatchingTest {
    @Test
    fun uidRoundFallbackDoesNotDependOnVisibleGroupIndex() {
        val season = listOf(
            RaceListItem(gp_name = "意大利大奖赛"),
            RaceListItem(gp_name = "西班牙大奖赛")
        )
        val event = calendarEvent(uid = "http://2026.f1calendar.com/#GP1_2026_gp")

        val match = matchCalendarRaceToSeason(
            raceName = "Barcelona-Catalunya",
            apiRaceName = "Barcelona-Catalunya",
            events = listOf(event),
            seasonList = season
        )

        assertEquals("西班牙大奖赛", match?.gp_name)
    }

    @Test
    fun cancelledRoundIsNotAttachedToCalendarEvent() {
        val season = listOf(RaceListItem(gp_name = "取消站", status = 4))

        val match = matchCalendarRaceToSeason(
            raceName = "Unknown",
            apiRaceName = "Unknown",
            events = listOf(calendarEvent(uid = "f1#GP1_gp")),
            seasonList = season
        )

        assertNull(match)
    }

    @Test
    fun uidRoundFallbackSkipsCancelledApiRounds() {
        val season = listOf(
            RaceListItem(gp_name = "澳大利亚大奖赛"),
            RaceListItem(gp_name = "巴林大奖赛", status = 4),
            RaceListItem(gp_name = "沙特大奖赛", status = 4),
            RaceListItem(gp_name = "迈阿密大奖赛"),
            RaceListItem(gp_name = "巴林大奖赛马来站")
        )

        val match = matchCalendarRaceToSeason(
            raceName = "巴林大奖赛 (马来西亚)",
            apiRaceName = "巴林大奖赛",
            events = listOf(calendarEvent(uid = "http://2026.f1calendar.com/#GP2_2026_gp")),
            seasonList = season
        )

        assertEquals("巴林大奖赛马来站", match?.gp_name)
    }

    @Test
    fun calendarRoundParsingIsCaseInsensitive() {
        assertEquals(13, calendarEvent(uid = "calendar#gp12_race").calendarRound())
    }

    @Test
    fun firstCalendarUidUsesZeroBasedRoundNumber() {
        assertEquals(1, calendarEvent(uid = "http://2026.f1calendar.com/#GP0_2026_gp").calendarRound())
    }

    @Test
    fun seasonGpIdsNeverGuessAnUnverifiedSequentialId() {
        val ids = resolveSeasonGpIds(
            schedule = listOf(RaceGp(gp_id = "4155", gp_name = "澳大利亚大奖赛")),
            stations = listOf(StationItem(gp_id = 4160, chinese_name = "巴林大奖赛", number = "2")),
            seasonList = listOf(
                RaceListItem(gp_name = "澳大利亚大奖赛"),
                RaceListItem(gp_name = "巴林大奖赛"),
                RaceListItem(gp_name = "未知大奖赛")
            )
        )

        assertEquals("4155", ids["澳大利亚"])
        assertEquals("4160", ids["巴林"])
        assertNull(ids["未知"])
    }

    private fun calendarEvent(uid: String) = F1CalendarEvent(
        uid = uid,
        summary = "",
        startUtc = "20260301T000000Z",
        endUtc = "20260301T010000Z",
        location = "",
        status = "CONFIRMED"
    )
}
