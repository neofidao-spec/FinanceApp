package com.financeapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A circular progress ring for budget usage visualization.
 *
 * @param progress Progress value from 0f to 1f
 * @param label Label text shown below the ring
 * @param amountText Text showing the amount (e.g., "Rp 500.000")
 * @param modifier Modifier for the composable
 * @param ringSize Size of the ring in dp
 * @param strokeWidth Width of the ring stroke
 * @param animationDuration Duration of the fill animation
 */
@Composable
fun BudgetProgressRing(
    progress: Float,
    label: String,
    amountText: String,
    modifier: Modifier = Modifier,
    ringSize: Int = 100,
    strokeWidth: Float = 12f,
    animationDuration: Int = 1000
) {
    // Clamp progress
    val clampedProgress = progress.coerceIn(0f, 1f)

    // Animated progress
    var animationPlayed by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationPlayed,
        animationSpec = tween(durationMillis = animationDuration),
        label = "ring_animation"
    )

    LaunchedEffect(clampedProgress) {
        animationPlayed = clampedProgress
    }

    // Color based on progress thresholds
    val ringColor = when {
        clampedProgress < 0.7f -> MaterialTheme.colorScheme.primary       // Green
        clampedProgress < 0.9f -> MaterialTheme.colorScheme.tertiary      // Yellow
        else -> MaterialTheme.colorScheme.error                           // Red
    }

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(ringSize.dp),
            contentAlignment = Alignment.Center
        ) {
            val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            Canvas(modifier = Modifier.size(ringSize.dp)) {
                val canvasSize = size.minDimension
                val radius = (canvasSize - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)

                // Background track
                drawCircle(
                    color = trackColor,
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Progress arc
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                )
            }

            // Center percentage text
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ringColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        // Amount text
        Text(
            text = amountText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BudgetProgressRingPreview() {
    MaterialTheme {
        BudgetProgressRing(
            progress = 0.75f,
            label = "Makanan",
            amountText = "Rp 750.000 / Rp 1.000.000"
        )
    }
}
