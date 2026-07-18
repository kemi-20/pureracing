package com.racingdaily.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.racingdaily.ui.liquidglass.components.LiquidBottomTab
import com.racingdaily.ui.liquidglass.components.LiquidBottomTabs
import com.racingdaily.ui.liquidglass.components.LiquidButton

@Composable
actual fun OriginalLiquidButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier,
    selected: Boolean,
    tint: Color,
    surfaceColor: Color,
    content: @Composable RowScope.() -> Unit
) {
    LiquidButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier,
        isInteractive = true,
        tint = when {
            tint != Color.Unspecified -> tint
            selected -> MaterialTheme.colorScheme.primary
            else -> Color.Unspecified
        },
        surfaceColor = surfaceColor,
        content = content
    )
}

@Composable
actual fun <T> OriginalLiquidBottomTabs(
    tabs: List<GlassNavTab<T>>,
    selected: T,
    onSelected: (T) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier
) {
    val externalSelectedIndex = tabs.indexOfFirst { it.value == selected }.coerceAtLeast(0)
    val selectedIndexState = remember(tabs) { mutableIntStateOf(externalSelectedIndex) }
    val selectedTabIndex = remember(selectedIndexState) { { selectedIndexState.intValue } }

    LaunchedEffect(externalSelectedIndex) {
        selectedIndexState.intValue = externalSelectedIndex
    }

    LiquidBottomTabs(
        selectedTabIndex = selectedTabIndex,
        onTabSelected = { index ->
            selectedIndexState.intValue = index
            tabs.getOrNull(index)?.let { tab -> onSelected(tab.value) }
        },
        backdrop = backdrop,
        tabsCount = tabs.size,
        accentColor = MaterialTheme.colorScheme.primary,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f),
        modifier = modifier
    ) {
        val contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        tabs.forEachIndexed { index, tab ->
            LiquidBottomTab(onClick = {
                selectedIndexState.intValue = index
                onSelected(tab.value)
            }) {
                Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(22.dp), tint = contentColor)
                Text(tab.label, style = MaterialTheme.typography.labelSmall, color = contentColor)
            }
        }
    }
}
