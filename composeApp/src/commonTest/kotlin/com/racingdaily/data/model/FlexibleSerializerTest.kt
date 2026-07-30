package com.racingdaily.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class FlexibleSerializerTest {
    @Serializable
    private data class FlexibleNumber(
        @Serializable(with = FlexibleIntSerializer::class)
        val value: Int
    )

    @Test
    fun outOfRangeIntegerDoesNotWrapAround() {
        assertEquals(0, Json.decodeFromString<FlexibleNumber>("""{"value":4294967295}""").value)
    }

    @Test
    fun numericStringStillDecodes() {
        assertEquals(42, Json.decodeFromString<FlexibleNumber>("""{"value":"42"}""").value)
    }
}
