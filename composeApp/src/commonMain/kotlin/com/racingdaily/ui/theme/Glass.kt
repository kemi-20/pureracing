package com.racingdaily.ui.theme

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState

val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

@Composable
expect fun GlassSurface(
    modifier: Modifier,
    shape: Shape,
    blurRadius: Dp,
    borderAlpha: Float,
    backgroundAlpha: Float,
    content: @Composable BoxScope.() -> Unit
)

@Composable
expect fun BackdropWrapper(
    modifier: Modifier,
    content: @Composable () -> Unit
)
