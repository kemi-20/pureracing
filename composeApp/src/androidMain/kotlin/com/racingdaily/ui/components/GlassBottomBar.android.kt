package com.racingdaily.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.racingdaily.ui.theme.*
import kotlinx.coroutines.launch

private val CapsuleShape = RoundedCornerShape(50)

@Composable
actual fun GlassBottomBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit
) {
    val isLightTheme = !isSystemInDarkTheme()
    val containerColor = if (isLightTheme)
        Color(0xFFFAFAFA).copy(0.4f)
    else
        Color(0xFF121212).copy(0.4f)

    val backdrop = rememberLayerBackdrop()

    Box(
        Modifier
            .fillMaxWidth()
            .layerBackdrop(backdrop)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp) },
                effects = {
                    vibrancy()
                    blur(12f.dp.toPx())
                    lens(20f.dp.toPx(), 20f.dp.toPx())
                },
                onDrawSurface = { drawRect(containerColor) }
            )
            .height(72.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTabs.forEach { tab ->
                val selected = currentRoute == tab.route
                TabItem(tab, selected) { onTabSelected(tab.route) }
            }
        }
    }
}

@Composable
fun RowScope.TabItem(
    tab: BottomTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .graphicsLayer {
                val scale = lerp(1f, 1.06f, progress.value)
                scaleX = scale
                scaleY = scale
            }
            .clip(CapsuleShape)
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onClick
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    scope.launch { progress.animateTo(1f, spring(0.5f, 300f, 0.001f)) }
                    waitForUpOrCancellation()
                    scope.launch { progress.animateTo(0f, spring(0.5f, 300f, 0.001f)) }
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
