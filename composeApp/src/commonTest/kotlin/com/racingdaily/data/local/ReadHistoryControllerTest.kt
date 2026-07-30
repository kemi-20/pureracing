package com.racingdaily.data.local

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReadHistoryControllerTest {
    @Test
    fun retainsOnlyTheMostRecentTwoThousandArticles() {
        var persisted = emptySet<Int>()
        val controller = ReadHistoryController(emptySet()) { persisted = it }

        (1..2_001).forEach(controller::markRead)

        assertEquals(2_000, controller.articleIds.size)
        assertFalse(controller.isRead(1))
        assertTrue(controller.isRead(2))
        assertTrue(controller.isRead(2_001))
        assertEquals(controller.articleIds, persisted)
    }
}
