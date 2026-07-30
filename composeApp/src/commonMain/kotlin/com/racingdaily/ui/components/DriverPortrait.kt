package com.racingdaily.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.transformations
import coil3.transform.Transformation
import kotlin.math.max
import kotlin.math.min

@Composable
fun DriverPortrait(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalPlatformContext.current
    val request = remember(context, url) {
        ImageRequest.Builder(context)
            .data(url)
            .transformations(createDriverBackgroundTransformation())
            .build()
    }
    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

internal expect fun createDriverBackgroundTransformation(): Transformation

internal fun removeConnectedWhiteBackground(pixels: IntArray, width: Int, height: Int): Boolean {
    val pixelCount = width.toLong() * height.toLong()
    if (width < 8 || height < 8 || pixelCount > pixels.size.toLong()) return false

    var visibleCount = 0
    var whiteCount = 0
    var edgeCount = 0
    var whiteEdgeCount = 0
    var cornerCount = 0
    var whiteCornerCount = 0
    val cornerWidth = max(2, width / 10)
    val cornerHeight = max(2, height / 10)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = pixels[y * width + x]
            if (alpha(color) < 24) continue
            visibleCount++
            val white = isCoverageWhite(color)
            if (white) whiteCount++

            if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                edgeCount++
                if (white) whiteEdgeCount++
            }

            val inCorner = (x < cornerWidth || x >= width - cornerWidth) &&
                (y < cornerHeight || y >= height - cornerHeight)
            if (inCorner) {
                cornerCount++
                if (white) whiteCornerCount++
            }
        }
    }

    if (visibleCount == 0 || edgeCount == 0 || cornerCount == 0) return false
    val overallCoverage = whiteCount.toFloat() / visibleCount
    val edgeCoverage = whiteEdgeCount.toFloat() / edgeCount
    val cornerCoverage = whiteCornerCount.toFloat() / cornerCount
    if (overallCoverage < 0.20f || edgeCoverage < 0.60f || cornerCoverage < 0.70f) return false

    val visited = BooleanArray(width * height)
    val queue = IntArray(width * height)
    var read = 0
    var write = 0

    fun enqueue(index: Int) {
        if (!visited[index] && isFloodWhite(pixels[index])) {
            visited[index] = true
            queue[write++] = index
        }
    }

    for (x in 0 until width) enqueue(x)
    for (y in 1 until height) {
        enqueue(y * width)
        enqueue(y * width + width - 1)
    }

    while (read < write) {
        val index = queue[read++]
        val x = index % width
        val y = index / width
        if (x > 0) enqueue(index - 1)
        if (x + 1 < width) enqueue(index + 1)
        if (y > 0) enqueue(index - width)
        if (y + 1 < height) enqueue(index + width)
    }

    if (write == 0) return false
    for (index in pixels.indices) {
        if (!visited[index]) continue
        val color = pixels[index]
        val r = red(color)
        val g = green(color)
        val b = blue(color)
        val minimum = min(r, min(g, b))
        val chroma = max(r, max(g, b)) - minimum
        val edgeAlpha = max(
            ((248 - minimum) / 23f).coerceIn(0f, 1f),
            ((chroma - 8) / 16f).coerceIn(0f, 1f)
        )
        val newAlpha = (alpha(color) * edgeAlpha).toInt().coerceIn(0, 255)
        pixels[index] = (newAlpha shl 24) or (r shl 16) or (g shl 8) or b
    }
    return true
}

private fun isCoverageWhite(color: Int): Boolean {
    val r = red(color)
    val g = green(color)
    val b = blue(color)
    return min(r, min(g, b)) >= 238 && max(r, max(g, b)) - min(r, min(g, b)) <= 18
}

private fun isFloodWhite(color: Int): Boolean {
    if (alpha(color) < 24) return false
    val r = red(color)
    val g = green(color)
    val b = blue(color)
    return min(r, min(g, b)) >= 225 && max(r, max(g, b)) - min(r, min(g, b)) <= 24
}

private fun alpha(color: Int) = color ushr 24 and 0xff
private fun red(color: Int) = color ushr 16 and 0xff
private fun green(color: Int) = color ushr 8 and 0xff
private fun blue(color: Int) = color and 0xff
