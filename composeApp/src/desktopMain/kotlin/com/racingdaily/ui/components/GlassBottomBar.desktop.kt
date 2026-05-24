package com.racingdaily.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racingdaily.ui.theme.*

@Composable
actual fun GlassBottomBar(
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        BottomTabs.forEach { tab ->
            LiquidBottomTab(onClick = { onTabSelected(tab.route) }) {
                val selected = currentRoute == tab.route
                Icon(tab.icon, tab.label, tint = if (selected) AccentRed else TextSecondary, modifier = Modifier.size(22.dp))
                Text(tab.label, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = if (selected) AccentRed else TextSecondary)
            }
        }
    }
}
