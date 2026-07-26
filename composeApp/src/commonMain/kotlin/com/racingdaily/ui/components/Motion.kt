package com.racingdaily.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.newsCardReveal(key: Any?): Modifier {
    val density = LocalDensity.current
    val reveal = remember(key) { Animatable(0f) }
    LaunchedEffect(key) {
        reveal.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 1f, stiffness = 360f)
        )
    }
    val revealOffset = with(density) { 18.dp.toPx() }
    return graphicsLayer {
        alpha = reveal.value
        translationY = (1f - reveal.value) * revealOffset
        val revealScale = 0.985f + 0.015f * reveal.value
        scaleX = revealScale
        scaleY = revealScale
    }
}
