package com.personal.novaai.ui.components

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
import androidx.compose.ui.unit.dp
import com.personal.novaai.ui.theme.ElectricBlue
import com.personal.novaai.ui.theme.VioletNeon

@Composable
fun AnimatedAiOrb(
    isListening: Boolean,
    amplitude: Float = 0f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_rotation"
    )

    val activeScale = if (isListening) pulseScale + (amplitude * 0.3f) else 1.0f

    Box(modifier = modifier.size(180.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(140.dp * activeScale)) {
            val center = this.center
            val radius = size.minDimension / 2f

            // Outer Glow Ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ElectricBlue.copy(alpha = 0.4f),
                        VioletNeon.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.3f
                )
            )

            // Dynamic Rotating Core
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        ElectricBlue,
                        VioletNeon,
                        ElectricBlue.copy(alpha = 0.8f),
                        VioletNeon.copy(alpha = 0.3f),
                        ElectricBlue
                    ),
                    center = center
                ),
                radius = radius * 0.75f,
                style = Stroke(width = 8f)
            )

            // Inner Core Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        ElectricBlue.copy(alpha = 0.8f),
                        VioletNeon.copy(alpha = 0.3f)
                    ),
                    center = center,
                    radius = radius * 0.5f
                ),
                radius = radius * 0.5f
            )
        }
    }
}
