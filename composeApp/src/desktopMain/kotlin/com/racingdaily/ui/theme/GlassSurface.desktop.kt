package com.racingdaily.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
actual fun GlassSurface(
    modifier: Modifier,
    shape: Shape,
    blurRadius: Dp,
    borderAlpha: Float,
    backgroundAlpha: Float,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color.White.copy(alpha = backgroundAlpha))
            .border(1.dp, Color.White.copy(alpha = borderAlpha), shape),
        content = content
    )
}
