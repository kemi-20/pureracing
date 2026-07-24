@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.racingdaily.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.racingdaily.ui.liquidglass.utils.InteractiveHighlight
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

val LocalGlassBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }
val LocalNavigationGlassBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

data class GlassNavTab<T>(
    val value: T,
    val icon: ImageVector,
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
                    .pureRacingBackground()
            )
            content()
        }
    }
}

fun Modifier.pureRacingBackground(): Modifier = composed {
    val isLightTheme = !isSystemInDarkTheme()
    val mainGradient = if (isLightTheme) {
        listOf(
            Color(0xFFF4FAFD),
            Color(0xFFCDE8F3),
            Color(0xFFD7EFE6),
            Color(0xFFF5DADD),
            Color(0xFFE4EDF4)
        )
    } else {
        listOf(
            Color(0xFF203448),
            Color(0xFF2A566B),
            Color(0xFF315B4E),
            Color(0xFF643842),
            Color(0xFF22303D)
        )
    }
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    this
        .background(MaterialTheme.colorScheme.background)
        .background(
            Brush.verticalGradient(mainGradient)
        )
        .background(
            Brush.horizontalGradient(
                listOf(
                    primary.copy(alpha = if (isLightTheme) 0.07f else 0.12f),
                    Color.Transparent,
                    secondary.copy(alpha = if (isLightTheme) 0.09f else 0.12f)
                )
            )
        )
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    role: Role? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    /**
     * Heavy Backdrop sampling is expensive and can crash Android when many surfaces
     * are composed inside Lazy lists. Race/list cards should set this to false.
     */
    useBackdrop: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val backdrop = if (useBackdrop) LocalGlassBackdrop.current else null
    val primary = MaterialTheme.colorScheme.primary
    val isLightTheme = !isSystemInDarkTheme()
    val containerColor =
        if (isLightTheme) Color.White.copy(alpha = 0.14f)
        else Color(0xFF314450).copy(alpha = 0.2f)
    val borderColor =
        if (selected) primary.copy(alpha = 0.58f)
        else if (isLightTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f)
        else Color.White.copy(alpha = 0.16f)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animationScope = rememberCoroutineScope()
    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(animationScope = animationScope)
    }

    val glassModifier =
        if (backdrop != null) {
            Modifier.drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(14.dp.toPx())
                    lens(16.dp.toPx(), 22.dp.toPx())
                },
                // Keep large interactive surfaces physically consistent with Kyant's LiquidButton.
                layerBlock = if (onClick != null) {
                    {
                        val width = size.width
                        val height = size.height
                        // Lazy layouts can measure with a zero size first; avoid Infinity/NaN scales.
                        if (width > 0.5f && height > 0.5f) {
                            val progress = interactiveHighlight.pressProgress
                            val scale = lerp(1f, 1f + 4.dp.toPx() / height, progress)

                            val maxOffset = size.minDimension.coerceAtLeast(1f)
                            val offset = interactiveHighlight.offset
                            translationX = maxOffset * tanh(0.05f * offset.x / maxOffset)
                            translationY = maxOffset * tanh(0.05f * offset.y / maxOffset)

                            val maxDragScale = 4.dp.toPx() / height
                            val offsetAngle = atan2(offset.y, offset.x)
                            val maxDimension = size.maxDimension.coerceAtLeast(1f)
                            scaleX = scale +
                                maxDragScale * abs(cos(offsetAngle) * offset.x / maxDimension) *
                                (width / height).fastCoerceAtMost(1f)
                            scaleY = scale +
                                maxDragScale * abs(sin(offsetAngle) * offset.y / maxDimension) *
                                (height / width).fastCoerceAtMost(1f)
                        } else {
                            scaleX = 1f
                            scaleY = 1f
                            translationX = 0f
                            translationY = 0f
                        }
                    }
                } else {
                    null
                },
                highlight = { Highlight.Default.copy(alpha = if (selected) 0.7f else if (isLightTheme) 0.3f else 0.42f) },
                shadow = { Shadow(radius = 18.dp, alpha = if (isLightTheme) 0.18f else 0.48f) },
                innerShadow = { InnerShadow(radius = 10.dp, alpha = if (selected) 0.48f else if (isLightTheme) 0.16f else 0.26f) },
                onDrawSurface = {
                    drawRect(containerColor)
                    if (selected) {
                        drawRect(primary.copy(alpha = 0.18f))
                    }
                }
            )
        } else {
            Modifier.background(containerColor, shape)
        }

    Box(
        modifier
            .graphicsLayer {
                if (backdrop == null && onClick != null) {
                    val scale = if (isPressed) 0.975f else 1f
                    scaleX = scale
                    scaleY = scale
                }
            }
            .then(glassModifier)
            .then(if (backdrop != null && onClick != null) interactiveHighlight.modifier else Modifier)
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = if (backdrop == null) interactionSource else null,
                        indication = null,
                        role = role,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .then(if (backdrop != null && onClick != null) interactiveHighlight.gestureModifier else Modifier)
            .padding(contentPadding),
        content = content
    )
}


@Composable
fun LightweightSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    role: Role? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit
) {
    GlassSurface(
        modifier = modifier,
        shape = shape,
        selected = selected,
        onClick = onClick,
        role = role,
        contentPadding = contentPadding,
        useBackdrop = false,
        content = content
    )
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val backdrop = LocalGlassBackdrop.current
    if (backdrop != null) {
        val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
        OriginalLiquidButton(
            onClick = onClick,
            backdrop = backdrop,
            modifier = modifier.defaultMinSize(minHeight = 48.dp),
            selected = selected,
            surfaceColor = Color.Unspecified
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    } else {
        FallbackGlassButton(
            onClick = onClick,
            modifier = modifier,
            selected = selected,
            content = content
        )
    }
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    val backdrop = LocalGlassBackdrop.current
    if (backdrop != null) {
        val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
        OriginalLiquidButton(
            onClick = onClick,
            backdrop = backdrop,
            modifier = modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
            selected = selected,
            surfaceColor = Color.Unspecified
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                Icon(
                    icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    } else {
        GlassSurface(
            modifier = modifier.size(48.dp),
            shape = CircleShape,
            selected = selected,
            onClick = onClick,
            role = Role.Button
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = contentDescription,
                    tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun FallbackGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    GlassSurface(
        modifier = modifier.defaultMinSize(minHeight = 46.dp),
        shape = RoundedCornerShape(999.dp),
        selected = selected,
        onClick = onClick,
        role = Role.Button,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 11.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun FallbackGlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    GlassSurface(
        modifier = modifier.heightIn(min = 38.dp),
        shape = RoundedCornerShape(999.dp),
        selected = selected,
        onClick = onClick,
        role = Role.Button,
        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
}

@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    val backdrop = LocalGlassBackdrop.current
    if (backdrop != null) {
        val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        OriginalLiquidButton(
            onClick = onClick,
            backdrop = backdrop,
            modifier = modifier.defaultMinSize(minHeight = 48.dp),
            selected = selected,
            surfaceColor = Color.Unspecified
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                if (leadingIcon != null) {
                    Icon(
                        leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    } else {
        FallbackGlassChip(
            label = label,
            selected = selected,
            onClick = onClick,
            modifier = modifier,
            leadingIcon = leadingIcon
        )
    }
}

@Composable
fun InfoPill(
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.secondary,
    leadingIcon: ImageVector? = null
) {
    val backdrop = LocalGlassBackdrop.current
    val isLightTheme = !isSystemInDarkTheme()
    val shape = RoundedCornerShape(999.dp)
    val glassModifier = if (backdrop != null) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(1.5.dp.toPx())
                lens(12.dp.toPx(), 20.dp.toPx(), chromaticAberration = true)
            },
            highlight = { Highlight.Default.copy(alpha = if (isLightTheme) 0.82f else 0.9f) },
            shadow = { Shadow(radius = 10.dp, alpha = if (isLightTheme) 0.24f else 0.42f) },
            innerShadow = { InnerShadow(radius = 6.dp, alpha = if (isLightTheme) 0.24f else 0.34f) },
            onDrawSurface = {
                drawRect(accent, blendMode = BlendMode.Hue)
                drawRect(Color.White.copy(alpha = if (isLightTheme) 0.07f else 0.04f))
                drawRect(accent.copy(alpha = if (isLightTheme) 0.04f else 0.07f))
            }
        )
    } else {
        Modifier
            .background(accent.copy(alpha = 0.1f), shape)
            .border(1.dp, accent.copy(alpha = 0.2f), shape)
    }

    Row(
        modifier
            .then(glassModifier)
            .clip(shape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(14.dp), tint = accent)
        }
        Text(
            label,
            color = accent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SectionLabel(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, content = trailing)
    }
}

@Composable
private fun FallbackGlassBottomBar(
    tabs: List<GlassNavTab<Any?>>,
    selected: Any?,
    onSelected: (Any?) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(999.dp),
        contentPadding = PaddingValues(5.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(58.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isSelected = tab.value == selected
                val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                GlassSurface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(999.dp),
                    selected = isSelected,
                    onClick = { onSelected(tab.value) },
                    role = Role.Tab
                ) {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            tab.icon,
                            contentDescription = tab.label,
                            modifier = Modifier.size(21.dp),
                            tint = contentColor
                        )
                        Text(
                            tab.label,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = contentColor,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> List<GlassNavTab<T>>.asAnyTabs(): List<GlassNavTab<Any?>> =
    this as List<GlassNavTab<Any?>>

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable RowScope.() -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        navigationIcon()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .width(28.dp)
                            .height(3.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(999.dp))
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

@Composable
fun <T> GlassBottomBar(
    tabs: List<GlassNavTab<T>>,
    selected: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val backdrop = LocalNavigationGlassBackdrop.current ?: LocalGlassBackdrop.current
    if (backdrop != null) {
        OriginalLiquidBottomTabs(
            tabs = tabs,
            selected = selected,
            onSelected = onSelected,
            backdrop = backdrop,
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    } else {
        FallbackGlassBottomBar(
            tabs = tabs.asAnyTabs(),
            selected = selected,
            onSelected = { value -> @Suppress("UNCHECKED_CAST") onSelected(value as T) },
            modifier = modifier
        )
    }
}

/*
 * Adapted from ImageToolbox EnhancedTopAppBar by T8RIN, Apache-2.0.
 * The original component switches between Material top app bar variants with animated content.
 */
@Composable
fun EnhancedTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = EnhancedTopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = EnhancedTopAppBarDefaults.colors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    type: EnhancedTopAppBarType = EnhancedTopAppBarType.Normal,
    drawHorizontalStroke: Boolean = true
) {
    AnimatedContent(
        targetState = type,
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) {
        when (it) {
            EnhancedTopAppBarType.Center -> CenterAlignedTopAppBar(
                title = title,
                modifier = modifier.drawHorizontalStroke(drawHorizontalStroke),
                navigationIcon = navigationIcon,
                actions = actions,
                windowInsets = windowInsets,
                colors = colors,
                scrollBehavior = scrollBehavior
            )

            EnhancedTopAppBarType.Normal -> TopAppBar(
                title = title,
                modifier = modifier.drawHorizontalStroke(drawHorizontalStroke),
                navigationIcon = navigationIcon,
                actions = actions,
                windowInsets = windowInsets,
                colors = colors,
                scrollBehavior = scrollBehavior
            )

            EnhancedTopAppBarType.Large -> LargeTopAppBar(
                title = title,
                modifier = modifier.drawHorizontalStroke(drawHorizontalStroke),
                navigationIcon = navigationIcon,
                actions = actions,
                windowInsets = windowInsets,
                colors = colors,
                scrollBehavior = scrollBehavior
            )
        }
    }
}

enum class EnhancedTopAppBarType {
    Center, Normal, Large
}

object EnhancedTopAppBarDefaults {
    val windowInsets: WindowInsets
        @Composable
        get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)

    @Composable
    fun colors(
        containerColor: Color = Color.Transparent,
        scrolledContainerColor: Color = Color.Transparent,
        navigationIconContentColor: Color = MaterialTheme.colorScheme.onSurface,
        titleContentColor: Color = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor: Color = MaterialTheme.colorScheme.onSurface,
    ): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = containerColor,
        scrolledContainerColor = scrolledContainerColor,
        navigationIconContentColor = navigationIconContentColor,
        titleContentColor = titleContentColor,
        actionIconContentColor = actionIconContentColor
    )
}

/*
 * Inspired by ImageToolbox EnhancedIconButton by T8RIN, Apache-2.0.
 * This keeps the animated shape/color behavior while routing the surface through Backdrop glass.
 */
@Composable
fun EnhancedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    icon: ImageVector,
    contentDescription: String? = null
) {
    if (enabled) {
        GlassIconButton(
            icon = icon,
            contentDescription = contentDescription,
            onClick = onClick,
            modifier = modifier,
            selected = selected
        )
    } else {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)) {
            Box(modifier.size(48.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
fun PreferenceGlassRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    endContent: @Composable RowScope.() -> Unit = {}
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick,
        role = if (onClick != null) Role.Button else null,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, content = endContent)
        }
    }
}

private fun Modifier.drawHorizontalStroke(enabled: Boolean) = composed {
    if (!enabled) {
        Modifier
    } else {
        Modifier
            .zIndex(1f)
            .drawWithContent {
                drawContent()
                drawRect(
                    color = Color.White.copy(alpha = 0.08f),
                    topLeft = Offset(0f, size.height - 1f),
                    size = Size(size.width, 1f)
                )
            }
    }
}
