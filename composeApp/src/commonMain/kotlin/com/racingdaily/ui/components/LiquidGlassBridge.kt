package com.racingdaily.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kyant.backdrop.Backdrop

@Composable
expect fun OriginalLiquidButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    content: @Composable RowScope.() -> Unit
)

@Composable
expect fun <T> OriginalLiquidBottomTabs(
    tabs: List<GlassNavTab<T>>,
    selected: T,
    onSelected: (T) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
)
