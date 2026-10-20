package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonPurple

@Composable
fun VoiceWaveAnimation(
    isListening: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.45f else 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.75f else 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse2"
    )

    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = if (isListening) 0.6f else 0.2f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaPulse"
    )

    Box(
        modifier = modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            val baseRadius = size.minDimension / 3.2f

            if (isListening) {
                // Outer glowing pulse
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonPurple.copy(alpha = alphaPulse), Color.Transparent),
                        center = center,
                        radius = baseRadius * pulse2
                    ),
                    radius = baseRadius * pulse2
                )

                // Middle cyan ring
                drawCircle(
                    color = ElectricBlue.copy(alpha = alphaPulse * 1.2f),
                    radius = baseRadius * pulse1,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        content()
    }
}
