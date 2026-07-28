package com.racingdaily.ui.components

import android.graphics.Bitmap
import coil3.size.Size
import coil3.transform.Transformation

internal actual fun createDriverBackgroundTransformation(): Transformation =
    AndroidDriverBackgroundTransformation()

private class AndroidDriverBackgroundTransformation : Transformation() {
    override val cacheKey: String = "driver-white-background-v1"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val width = input.width
        val height = input.height
        val pixels = IntArray(width * height)
        return runCatching {
            input.getPixels(pixels, 0, width, 0, 0, width, height)
            if (!removeConnectedWhiteBackground(pixels, width, height)) return input
            Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888).apply {
                density = input.density
            }
        }.getOrDefault(input)
    }
}
