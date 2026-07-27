package com.racingdaily.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun Modifier.newsCardReveal(key: Any?): Modifier {
    val hasRevealed = rememberSaveable(key) { mutableStateOf(false) }
    if (hasRevealed.value) return this

    val reveal = remember(key) { Animatable(0f) }
    LaunchedEffect(key) {
        reveal.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 1f, stiffness = 360f)
        )
        hasRevealed.value = true
    }
    return graphicsLayer {
        alpha = reveal.value
    }
}
