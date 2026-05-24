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
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle

@Composable
actual fun GlassSurface(
    modifier: Modifier,
    shape: Shape,
    blurRadius: Dp,
    borderAlpha: Float,
    backgroundAlpha: Float,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = LocalHazeState.current
    if (hazeState != null) {
        Box(
            modifier = modifier
                .hazeChild(
                    state = hazeState,
                    shape = shape,
                    style = HazeStyle(
                        tint = Color.White.copy(alpha = backgroundAlpha),
                        blurRadius = blurRadius
                    )
                )
                .border(1.dp, Color.White.copy(alpha = borderAlpha), shape),
            content = content
        )
    } else {
        Box(
            modifier = modifier
                .clip(shape)
                .background(Color.White.copy(alpha = backgroundAlpha))
                .border(1.dp, Color.White.copy(alpha = borderAlpha), shape),
            content = content
        )
    }
}

@Composable
actual fun BackdropWrapper(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val hazeState = LocalHazeState.current
    Box(
        modifier = modifier.then(
            if (hazeState != null) Modifier.haze(state = hazeState) else Modifier
        )
    ) {
        content()
    }
}
