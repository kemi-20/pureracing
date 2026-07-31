package com.racingdaily.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DriverPortraitTest {
    @Test
    fun removesOnlyWhiteBackgroundConnectedToImageEdges() {
        val width = 10
        val height = 10
        val pixels = IntArray(width * height) { 0xffffffff.toInt() }
        for (y in 3..6) {
            for (x in 3..6) pixels[y * width + x] = 0xff202020.toInt()
        }

        assertTrue(removeConnectedWhiteBackground(pixels, width, height))
        assertEquals(0, pixels.first() ushr 24)
        assertEquals(0xff, pixels[5 * width + 5] ushr 24)
    }

    @Test
    fun preservesNonWhiteBackgrounds() {
        val pixels = IntArray(100) { 0xff1769aa.toInt() }

        assertFalse(removeConnectedWhiteBackground(pixels, width = 10, height = 10))
        assertTrue(pixels.all { it == 0xff1769aa.toInt() })
    }
}
