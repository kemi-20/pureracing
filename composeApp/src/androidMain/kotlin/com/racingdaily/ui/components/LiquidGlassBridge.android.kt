package com.racingdaily.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    content: @Composable RowScope.() -> Unit
) {
    LiquidButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier,
        isInteractive = true,
        tint = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified,
        surfaceColor = Color.Unspecified,
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
    var selectedIndex by remember(tabs) { mutableIntStateOf(externalSelectedIndex) }

    LaunchedEffect(externalSelectedIndex) {
        selectedIndex = externalSelectedIndex
    }

    LiquidBottomTabs(
        selectedTabIndex = { selectedIndex },
        onTabSelected = { index ->
            selectedIndex = index
            tabs.getOrNull(index)?.let { tab ->
                if (tab.value != selected) {
                    onSelected(tab.value)
                }
            }
        },
        backdrop = backdrop,
        tabsCount = tabs.size,
        modifier = modifier
    ) {
        tabs.forEachIndexed { index, tab ->
            LiquidBottomTab(onClick = {
                selectedIndex = index
                if (tab.value != selected) {
                    onSelected(tab.value)
                }
            }) {
                Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(22.dp), tint = Color.White)
                Text(tab.label, style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
    }
}
