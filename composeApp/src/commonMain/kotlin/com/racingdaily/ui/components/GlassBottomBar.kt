package com.racingdaily.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.racingdaily.ui.navigation.Routes
import com.racingdaily.ui.theme.*

data class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val BottomTabs = listOf(
    BottomTab(Routes.Home, "News", rememberNewsIcon()),
    BottomTab(Routes.Race, "Race", rememberRaceIcon()),
    BottomTab(Routes.Rankings, "Rank", rememberRankIcon()),
    BottomTab(Routes.More, "More", rememberMoreIcon()),
)

val LocalLayerBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

@Composable
fun GlassBottomBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit
) {
    val backdrop = LocalLayerBackdrop.current ?: return

    val selectedIndex = BottomTabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    LiquidBottomTabs(
        selectedTabIndex = { selectedIndex },
        onTabSelected = { index ->
            if (index in BottomTabs.indices) onTabSelected(BottomTabs[index].route)
        },
        backdrop = backdrop,
        tabsCount = BottomTabs.size,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        BottomTabs.forEach { tab ->
            LiquidBottomTab(onClick = { onTabSelected(tab.route) }) {
                val selected = currentRoute == tab.route
                Icon(
                    tab.icon,
                    contentDescription = tab.label,
                    tint = if (selected) AccentRed else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
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

// Icons
fun rememberNewsIcon(): ImageVector = ImageVector.Builder(
    name = "News", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.White), pathFillType = PathFillType.NonZero) {
        moveTo(4f, 4f); lineTo(4f, 20f); lineTo(20f, 20f); lineTo(20f, 4f); lineTo(4f, 4f); close()
        moveTo(6f, 6f); lineTo(18f, 6f); lineTo(18f, 18f); lineTo(6f, 18f); lineTo(6f, 6f); close()
        moveTo(8f, 10f); lineTo(16f, 10f); lineTo(16f, 8f); lineTo(8f, 8f); close()
        moveTo(8f, 14f); lineTo(14f, 14f); lineTo(14f, 12f); lineTo(8f, 12f); close()
    }
}.build()

fun rememberRaceIcon(): ImageVector = ImageVector.Builder(
    name = "Race", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.White), pathFillType = PathFillType.NonZero) {
        moveTo(12f, 2f); curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f); curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
        curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f); curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f); close()
        moveTo(12f, 20f); curveTo(7.59f, 20f, 4f, 16.41f, 4f, 12f); curveTo(4f, 7.59f, 7.59f, 4f, 12f, 4f)
        curveTo(16.41f, 4f, 20f, 7.59f, 20f, 12f); curveTo(20f, 16.41f, 16.41f, 20f, 12f, 20f); close()
        moveTo(12f, 6f); lineTo(9f, 17f); lineTo(15f, 12f); lineTo(12f, 6f); close()
    }
}.build()

fun rememberRankIcon(): ImageVector = ImageVector.Builder(
    name = "Rank", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.White), pathFillType = PathFillType.NonZero) {
        moveTo(6f, 10f); lineTo(6f, 20f); lineTo(10f, 20f); lineTo(10f, 10f); lineTo(6f, 10f); close()
        moveTo(10f, 4f); lineTo(10f, 20f); lineTo(14f, 20f); lineTo(14f, 4f); lineTo(10f, 4f); close()
        moveTo(14f, 8f); lineTo(14f, 20f); lineTo(18f, 20f); lineTo(18f, 8f); lineTo(14f, 8f); close()
    }
}.build()

fun rememberMoreIcon(): ImageVector = ImageVector.Builder(
    name = "More", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.White), pathFillType = PathFillType.NonZero) {
        moveTo(12f, 8f); curveTo(13.1f, 8f, 14f, 7.1f, 14f, 6f); curveTo(14f, 4.9f, 13.1f, 4f, 12f, 4f)
        curveTo(10.9f, 4f, 10f, 4.9f, 10f, 6f); curveTo(10f, 7.1f, 10.9f, 8f, 12f, 8f); close()
        moveTo(12f, 14f); curveTo(13.1f, 14f, 14f, 13.1f, 14f, 12f); curveTo(14f, 10.9f, 13.1f, 10f, 12f, 10f)
        curveTo(10.9f, 10f, 10f, 10.9f, 10f, 12f); curveTo(10f, 13.1f, 10.9f, 14f, 12f, 14f); close()
        moveTo(12f, 20f); curveTo(13.1f, 20f, 14f, 19.1f, 14f, 18f); curveTo(14f, 16.9f, 13.1f, 16f, 12f, 16f)
        curveTo(10.9f, 16f, 10f, 16.9f, 10f, 18f); curveTo(10f, 19.1f, 10.9f, 20f, 12f, 20f); close()
    }
}.build()
