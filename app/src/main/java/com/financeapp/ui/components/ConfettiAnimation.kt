package com.financeapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    var x: Float,
    var y: Float,
    var angle: Double,
    var speed: Float,
    var color: Color,
    var size: Float,
    var decay: Float = 0.96f
)

/**
 * Lightweight confetti burst animation.
 * Shows a burst of particles from the center that fade out over ~1 second.
 * Usage: just add ConfettiAnimation(visible = triggerState) — it auto-plays and disappears.
 */
@Composable
fun ConfettiAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 40,
    onDismiss: () -> Unit = {}
) {
    if (!visible) return

    val colors = remember {
        listOf(
            Color(0xFFFF5252), Color(0xFFFFAB40), Color(0xFFFFEE58),
            Color(0xFF69F0AE), Color(0xFF40C4FF), Color(0xFFE040FB)
        )
    }

    val particles = remember {
        List(particleCount) {
            val angle = Random.nextDouble(0.0, 2.0 * Math.PI)
            val speed = Random.nextFloat() * 8f + 4f
            Particle(
                x = 0f,
                y = 0f,
                angle = angle,
                speed = speed,
                color = colors[Random.nextInt(colors.size)],
                size = Random.nextFloat() * 6f + 3f
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = LinearEasing)
        )
        onDismiss()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        particles.forEach { particle ->
            val t = progress.value
            val px = centerX + (cos(particle.angle) * particle.speed * 50f * t).toFloat()
            val py = centerY + (sin(particle.angle) * particle.speed * 50f * t).toFloat() +
                    (t * t * 200f) // gravity

            val alpha = (1f - t).coerceIn(0f, 1f)
            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.size * (1f - t * 0.5f),
                center = Offset(px, py)
            )
        }
    }
}
