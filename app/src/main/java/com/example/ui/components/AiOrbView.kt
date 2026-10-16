package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

enum class OrbState {
    IDLE,
    THINKING,
    STREAMING,
    SUCCESS,
    ERROR
}

@Composable
fun AiOrbView(
    size: Dp = 48.dp,
    isPulsing: Boolean = true,
    isThinking: Boolean = false,
    isStreaming: Boolean = false,
    isError: Boolean = false,
    orbState: OrbState = when {
        isError -> OrbState.ERROR
        isStreaming -> OrbState.STREAMING
        isThinking || isPulsing -> OrbState.THINKING
        else -> OrbState.IDLE
    },
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_infinite_anim")

    // Rotation angle for active orbital ring
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (orbState) {
                    OrbState.THINKING -> 1800
                    OrbState.STREAMING -> 1200
                    else -> 5000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing pulse scale animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = when (orbState) {
            OrbState.STREAMING -> 1.08f
            OrbState.THINKING -> 1.05f
            else -> 1.02f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (orbState) {
                    OrbState.STREAMING -> 700
                    OrbState.THINKING -> 1000
                    else -> 2400
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Outward energy wave radius fraction
    val waveFraction by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_fraction"
    )

    // Dynamic aura colors using user's exact palette
    val coreStartColor by animateColorAsState(
        targetValue = when (orbState) {
            OrbState.ERROR -> OmniaErrorRed
            OrbState.SUCCESS -> OmniaSuccessGreen
            OrbState.STREAMING -> OmniaBrightCyan
            OrbState.THINKING -> OmniaNeonCyan
            OrbState.IDLE -> OmniaElectricBlue
        },
        animationSpec = tween(400),
        label = "core_start"
    )

    val coreEndColor by animateColorAsState(
        targetValue = when (orbState) {
            OrbState.ERROR -> Color(0xFFB91C1C)
            OrbState.SUCCESS -> Color(0xFF059669)
            OrbState.STREAMING -> OmniaElectricBlue
            OrbState.THINKING -> OmniaRoyalBlue
            OrbState.IDLE -> OmniaRoyalBlue
        },
        animationSpec = tween(400),
        label = "core_end"
    )

    // Crucial: Fixed layout size prevents measurement cycles and flickering
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val radius = this.size.minDimension / 2f
            val center = this.center

            // 1. Streaming Energy Waves (if streaming)
            if (orbState == OrbState.STREAMING) {
                drawCircle(
                    color = OmniaBrightCyan.copy(alpha = (1.35f - waveFraction).coerceIn(0f, 0.45f)),
                    radius = radius * waveFraction,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // 2. Outer Ambient Radial Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreStartColor.copy(alpha = 0.4f),
                        coreEndColor.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.3f
                ),
                radius = radius * 1.3f
            )

            // 3. Inner Core Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreStartColor,
                        coreEndColor,
                        OmniaElectricBlue
                    ),
                    center = center,
                    radius = radius * 0.78f
                ),
                radius = radius * 0.78f
            )

            // 4. Rotating Accent Orbital Ring
            rotate(rotationAngle, center) {
                drawCircle(
                    color = when (orbState) {
                        OrbState.ERROR -> Color(0xFFFCA5A5)
                        OrbState.SUCCESS -> Color(0xFF6EE7B7)
                        OrbState.STREAMING -> OmniaBrightCyan
                        else -> OmniaNeonCyan.copy(alpha = 0.8f)
                    },
                    radius = radius * 0.92f,
                    style = Stroke(width = if (orbState == OrbState.STREAMING || orbState == OrbState.THINKING) 2.dp.toPx() else 1.2.dp.toPx())
                )
            }
        }
    }
}
