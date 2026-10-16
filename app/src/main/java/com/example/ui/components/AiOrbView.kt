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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.OmniaAccentCyan
import com.example.ui.theme.OmniaElectricViolet
import com.example.ui.theme.OmniaLavenderHighlight
import com.example.ui.theme.OmniaPrimaryPurple
import com.example.ui.theme.OmniaSuccessGreen

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

    // Rotation angle for active state orbiting ring
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (orbState) {
                    OrbState.THINKING -> 1200
                    OrbState.STREAMING -> 800
                    else -> 4000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing pulse scale animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = when (orbState) {
            OrbState.STREAMING -> 1.15f
            OrbState.THINKING -> 1.10f
            else -> 1.04f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (orbState) {
                    OrbState.STREAMING -> 600
                    OrbState.THINKING -> 900
                    else -> 2200
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Outward energy wave radius fraction
    val waveFraction by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_fraction"
    )

    // Dynamic primary aura colors transitioning seamlessly
    val coreStartColor by animateColorAsState(
        targetValue = when (orbState) {
            OrbState.ERROR -> Color(0xFFFF5252)
            OrbState.SUCCESS -> OmniaSuccessGreen
            OrbState.STREAMING -> OmniaAccentCyan
            OrbState.THINKING -> OmniaLavenderHighlight
            OrbState.IDLE -> OmniaLavenderHighlight
        },
        animationSpec = tween(400),
        label = "core_start"
    )

    val coreEndColor by animateColorAsState(
        targetValue = when (orbState) {
            OrbState.ERROR -> Color(0xFFD32F2F)
            OrbState.SUCCESS -> Color(0xFF00C853)
            OrbState.STREAMING -> OmniaPrimaryPurple
            OrbState.THINKING -> OmniaElectricViolet
            OrbState.IDLE -> OmniaElectricViolet
        },
        animationSpec = tween(400),
        label = "core_end"
    )

    Box(
        modifier = modifier.size(size * pulseScale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val radius = this.size.minDimension / 2f
            val center = this.center

            // 1. Streaming Energy Waves (if streaming)
            if (orbState == OrbState.STREAMING) {
                drawCircle(
                    color = OmniaAccentCyan.copy(alpha = (1.4f - waveFraction).coerceIn(0f, 0.5f)),
                    radius = radius * waveFraction,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // 2. Outer Ambient Radial Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreStartColor.copy(alpha = 0.5f),
                        coreEndColor.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.35f
                ),
                radius = radius * 1.35f
            )

            // 3. Inner Core Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreStartColor,
                        coreEndColor,
                        OmniaPrimaryPurple
                    ),
                    center = center,
                    radius = radius * 0.8f
                ),
                radius = radius * 0.8f
            )

            // 4. Rotating Accent Orbital Ring
            rotate(rotationAngle, center) {
                drawCircle(
                    color = when (orbState) {
                        OrbState.ERROR -> Color(0xFFFF8A80)
                        OrbState.SUCCESS -> Color(0xFFB9F6CA)
                        OrbState.STREAMING -> OmniaAccentCyan
                        else -> OmniaAccentCyan.copy(alpha = 0.85f)
                    },
                    radius = radius * 0.95f,
                    style = Stroke(width = if (orbState == OrbState.STREAMING || orbState == OrbState.THINKING) 2.5.dp.toPx() else 1.5.dp.toPx())
                )
            }
        }
    }
}
