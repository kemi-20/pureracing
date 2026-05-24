package com.racingdaily.ui.components.util

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class InteractiveHighlight(
    private val animationScope: CoroutineScope,
    private val position: (size: Size, offset: Offset) -> Offset,
) {

    private val pressProgress = Animatable(0f)

    private var animX = 0f
    private var animY = 0f

    private val highlightShader = """
        uniform float2 uResolution;
        uniform float2 uPosition;
        uniform float uProgress;
        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / uResolution;
            float2 delta = uv - uPosition / uResolution;
            float dist = length(delta);
            float falloff = smoothstep(0.5, 0.0, dist);
            return half4(1.0, 1.0, 1.0, falloff * uProgress * 0.15);
        }
    """.trimIndent()

    val modifier: Modifier
        get() = if (Build.VERSION.SDK_INT >= 33) {
            Modifier.drawWithContent {
                drawContent()
                val shader = RuntimeShader(highlightShader).apply {
                    setFloatUniform("uResolution", size.width, size.height)
                    setFloatUniform("uPosition", animX, animY)
                    setFloatUniform("uProgress", pressProgress.value)
                }
                drawRect(brush = ShaderBrush(shader))
            }
        } else {
            Modifier.drawWithContent {
                drawContent()
                drawRect(Color.White.copy(alpha = pressProgress.value * 0.08f))
            }
        }

    val gestureModifier: Modifier
        get() = Modifier.pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown()
                animationScope.launch {
                    pressProgress.animateTo(1f, spring(1f, 1000f, 0.001f))
                }
                waitForUpOrCancellation()
                animationScope.launch {
                    pressProgress.animateTo(0f, spring(1f, 1000f, 0.001f))
                }
            }
        }
}
