package com.racingdaily.ui.screens.home

import com.racingdaily.data.model.Cover
import kotlin.test.Test
import kotlin.test.assertEquals

class NewsCoverImageTest {
    @Test
    fun coverSourcesPreferAbsoluteUrlThenNormalizedPathAndRetry() {
        val sources = Cover(
            path_url = "https://cdn.example/cover.png",
            path = "/uploads/cover.png"
        ).imageSources()

        assertEquals(
            listOf(
                "https://cdn.example/cover.png",
                "https://oss.static.romielf.com/uploads/cover.png",
                "https://cdn.example/cover.png?pureracing_retry=1"
            ),
            sources
        )
    }

    @Test
    fun existingQueryUsesAmpersandForRetry() {
        assertEquals(
            listOf(
                "https://cdn.example/cover.png?v=2",
                "https://cdn.example/cover.png?v=2&pureracing_retry=1"
            ),
            Cover(path_url = "https://cdn.example/cover.png?v=2").imageSources()
        )
    }
}
