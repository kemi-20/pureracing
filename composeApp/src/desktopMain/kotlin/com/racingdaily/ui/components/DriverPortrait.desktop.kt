package com.racingdaily.ui.components

import coil3.size.Size
import coil3.transform.Transformation
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo

internal actual fun createDriverBackgroundTransformation(): Transformation =
    DesktopDriverBackgroundTransformation()

private class DesktopDriverBackgroundTransformation : Transformation() {
    override val cacheKey: String = "driver-white-background-v1"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val width = input.width
        val height = input.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = input.getColor(x, y)
            }
        }
        if (!removeConnectedWhiteBackground(pixels, width, height)) return input

        val bytes = ByteArray(width * height * 4)
        pixels.forEachIndexed { index, color ->
            val offset = index * 4
            val alpha = color ushr 24 and 0xff
            bytes[offset] = ((color ushr 16 and 0xff) * alpha / 255).toByte()
            bytes[offset + 1] = ((color ushr 8 and 0xff) * alpha / 255).toByte()
            bytes[offset + 2] = ((color and 0xff) * alpha / 255).toByte()
            bytes[offset + 3] = alpha.toByte()
        }
        val info = ImageInfo(
            ColorInfo(ColorType.RGBA_8888, ColorAlphaType.PREMUL, input.colorSpace),
            width,
            height
        )
        return Bitmap().also { output ->
            check(output.installPixels(info, bytes, width * 4)) { "Unable to install portrait pixels" }
        }
    }
}
