package com.racingdaily.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow

val LocalGlassBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

data class GlassNavTab<T>(
    val value: T,
    val icon: String,
    val label: String
)

@Composable
fun GlassBackdropHost(content: @Composable BoxScope.() -> Unit) {
    val backdrop = rememberLayerBackdrop()
    CompositionLocalProvider(LocalGlassBackdrop provides backdrop) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .layerBackdrop(backdrop)
                    .background(
                        Brush.linearGradient(
                            0f to Color(0xFF05070B),
                            0.42f to Color(0xFF101927),
                            0.72f to Color(0xFF101B17),
                            1f to Color(0xFF240908),
                            start = Offset.Zero,
                            end = Offset(1400f, 2200f)
                        )
                    )
                    .drawBehind {
                        val red = Color(0xFFE10600).copy(alpha = 0.12f)
                        val blue = Color(0xFF58A6FF).copy(alpha = 0.09f)
                        val green = Color(0xFF3FB950).copy(alpha = 0.06f)
                        val gap = size.width / 5f
                        repeat(6) { index ->
                            val x = index * gap - size.width * 0.25f
                            drawLine(red, Offset(x, 0f), Offset(x + size.height * 0.55f, size.height), 2.5f)
                            drawLine(blue, Offset(size.width - x * 0.55f, 0f), Offset(size.width - x, size.height), 1.5f)
                        }
                        drawRect(green, topLeft = Offset(0f, size.height * 0.72f), size = size.copy(height = size.height * 0.28f))
                    }
            )
            content()
        }
    }
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    role: Role? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val backdrop = LocalGlassBackdrop.current
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val borderColor =
        if (selected) primary.copy(alpha = 0.58f)
        else Color.White.copy(alpha = 0.18f)
    val surfaceColor =
        if (selected) primary.copy(alpha = 0.26f)
        else surface.copy(alpha = 0.44f)
    val overlayColor =
        if (selected) Color.White.copy(alpha = 0.08f)
        else Color.White.copy(alpha = 0.045f)

    val glassModifier =
        if (backdrop != null) {
            Modifier.drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(if (selected) 18.dp.toPx() else 14.dp.toPx())
                    lens(
                        refractionHeight = if (selected) 16.dp.toPx() else 10.dp.toPx(),
                        refractionAmount = if (selected) 24.dp.toPx() else 18.dp.toPx(),
                        chromaticAberration = selected
                    )
                },
                highlight = {
                    Highlight.Default.copy(alpha = if (selected) 0.72f else 0.42f)
                },
                shadow = {
                    Shadow(
                        radius = if (selected) 24.dp else 18.dp,
                        color = Color.Black.copy(alpha = 0.32f),
                        alpha = if (selected) 0.9f else 0.65f
                    )
                },
                onDrawSurface = {
                    drawRect(surfaceColor)
                    drawRect(overlayColor)
                }
            )
        } else {
            Modifier.background(surfaceColor, shape)
        }

    Box(
        modifier
            .then(glassModifier)
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = null,
                        indication = null,
                        role = role,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    GlassSurface(
        modifier = modifier.defaultMinSize(minHeight = 42.dp),
        shape = RoundedCornerShape(999.dp),
        selected = selected,
        onClick = onClick,
        role = Role.Button,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        modifier = modifier.heightIn(min = 34.dp),
        shape = RoundedCornerShape(999.dp),
        selected = selected,
        onClick = onClick,
        role = Role.Button,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
fun <T> GlassBottomBar(
    tabs: List<GlassNavTab<T>>,
    selected: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(6.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(58.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isSelected = tab.value == selected
                GlassSurface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(18.dp),
                    selected = isSelected,
                    onClick = { onSelected(tab.value) },
                    role = Role.Tab
                ) {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(tab.icon, fontSize = 16.sp, lineHeight = 18.sp, textAlign = TextAlign.Center)
                        Text(
                            tab.label,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
