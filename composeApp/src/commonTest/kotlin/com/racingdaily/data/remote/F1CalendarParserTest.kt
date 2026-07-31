package com.racingdaily.data.remote

import kotlin.test.Test
import kotlin.test.assertEquals

class F1CalendarParserTest {
    @Test
    fun parsesFoldedAndEscapedTentativeEvent() {
        val calendar = """
            BEGIN:VCALENDAR
            BEGIN:VEVENT
            UID:race-2026
            DTSTART:20260308T070000Z
            DTEND:20260308T090000Z
            SUMMARY:巴林大奖赛 \(马来西亚\)\, 正赛
            LOCATION:Sepang\; Malaysia
            STATUS:TENTATIVE
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val event = calendar.parseF1Calendar(2026).single()

        assertEquals("巴林大奖赛 (马来西亚), 正赛", event.summary)
        assertEquals("Sepang; Malaysia", event.location)
        assertEquals("TENTATIVE", event.status)
    }

    @Test
    fun ignoresOtherSeasonsAndCancelledEvents() {
        val calendar = """
            BEGIN:VCALENDAR
            BEGIN:VEVENT
            UID:cancelled
            DTSTART:20260501T120000Z
            SUMMARY:Cancelled race
            STATUS:CANCELLED
            END:VEVENT
            BEGIN:VEVENT
            UID:future
            DTSTART:20270501T120000Z
            SUMMARY:Future race
            STATUS:CONFIRMED
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        assertEquals(emptyList(), calendar.parseF1Calendar(2026))
    }

    @Test
    fun unfoldsContinuedProperties() {
        val calendar = """
            BEGIN:VEVENT
            UID:folded
            DTSTART:20260401T120000Z
            SUMMARY:Long Grand
             Prix
            STATUS:CONFIRMED
            END:VEVENT
        """.trimIndent()

        assertEquals("Long GrandPrix", calendar.parseF1Calendar(2026).single().summary)
    }

    @Test
    fun missingStatusDefaultsToConfirmed() {
        val calendar = """
            BEGIN:VEVENT
            UID:without-status
            DTSTART:20260401T120000Z
            SUMMARY:F1: Grand Prix (Australian)
            END:VEVENT
        """.trimIndent()

        assertEquals("CONFIRMED", calendar.parseF1Calendar(2026).single().status)
    }
}
