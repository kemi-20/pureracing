package com.racingdaily.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racingdaily.ui.theme.*
import kotlinx.coroutines.launch
import com.kyant.backdrop.*
import com.kyant.backdrop.effects.*
import com.kyant.backdrop.backdrops.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.util.lerp

@Composable
actual fun GlassBottomBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit
) {
    val backdrop = LocalLayerBackdrop.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BottomTabs.forEach { tab ->
                val selected = currentRoute == tab.route
                if (backdrop != null) {
                    // Liquid Glass Interactive item
                    TabItem(
                        tab = tab,
                        selected = selected,
                        onClick = { onTabSelected(tab.route) },
                        backdrop = backdrop
                    )
                } else {
                    // Fallback to normal Glass Card
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.06f))
                            .clickable { onTabSelected(tab.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
            }
        }
    }
}

@Composable
fun RowScope.TabItem(
    tab: BottomTab,
    selected: Boolean,
    onClick: () -> Unit,
    backdrop: LayerBackdrop
) {
    val animationScope = rememberCoroutineScope()
    val progressAnimation = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { CircleShape },
                effects = {
                    vibrancy()
                    blur(4f.dp.toPx())
                    lens(16f.dp.toPx(), 32f.dp.toPx())
                },
                layerBlock = {
                    val progress = progressAnimation.value
                    val maxScale = (size.width + 16f.dp.toPx()) / size.width
                    val scale = lerp(1f, maxScale, progress)
                    scaleX = scale
                    scaleY = scale
                },
                onDrawSurface = {
                    if (selected) {
                        drawRect(Color.White.copy(alpha = 0.16f))
                    } else {
                        drawRect(Color.White.copy(alpha = 0.06f))
                    }
                }
            )
            .clickable(
                interactionSource = null,
                indication = null
            ) {
                onClick()
            }
            .pointerInput(animationScope) {
                val animationSpec = spring<Float>(0.5f, 300f, 0.001f)
                awaitEachGesture {
                    // press
                    awaitFirstDown()
                    animationScope.launch {
                        progressAnimation.animateTo(1f, animationSpec)
                    }

                    // release
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

