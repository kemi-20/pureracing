package com.racingdaily.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racingdaily.ui.theme.*
import kotlinx.coroutines.launch

@Composable
actual fun GlassBottomBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        blurRadius = 20.dp,
        borderAlpha = 0.15f,
        backgroundAlpha = 0.10f
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTabs.forEach { tab ->
                val selected = currentRoute == tab.route
                TabItemDesktop(
                    tab = tab,
                    selected = selected,
                    onClick = { onTabSelected(tab.route) }
                )
            }
        }
    }
}

@Composable
fun RowScope.TabItemDesktop(
    tab: BottomTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val animationScope = rememberCoroutineScope()
    val progressAnimation = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .graphicsLayer {
                val progress = progressAnimation.value
                val scale = 1f + 0.06f * progress
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = null,
                indication = null
            ) {
                onClick()
            }
            .pointerInput(animationScope) {
                val animationSpec = spring<Float>(0.5f, 300f, 0.001f)
                awaitEachGesture {
                    awaitFirstDown()
                    animationScope.launch {
                        progressAnimation.animateTo(1f, animationSpec)
                    }
                    waitForUpOrCancellation()
                    animationScope.launch {
                        progressAnimation.animateTo(0f, animationSpec)
                    }
                }
            }
            .fillMaxHeight()
            .weight(1f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                tab.icon,
                contentDescription = tab.label,
                tint = if (selected) AccentRed else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                tab.label,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) AccentRed else TextSecondary
            )
        }
    }
}
