package com.financeapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.financeapp.ui.theme.Spacing
import com.financeapp.ui.utils.FormatterUtil

/**
 * Data class representing a segment in the donut chart.
 */
data class DonutSegment(
    val label: String,
    val value: Double,
    val color: Color
)

/**
 * A donut/pie chart drawn with Canvas using drawArc.
 *
 * @param segments List of DonutSegment (label, value, color)
 * @param modifier Modifier for the composable
 * @param centerText Text to display in the center (e.g., total value)
 * @param strokeWidth Width of the donut ring
 * @param animationDuration Duration of the sweep animation in milliseconds
 */
@Composable
fun DonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    centerText: String = "",
    strokeWidth: Float = 40f,
    animationDuration: Int = 1000
) {
    if (segments.isEmpty()) return

    val total = segments.sumOf { it.value }.toFloat()

    // Animated progress
    var animationPlayed by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationPlayed,
        animationSpec = tween(durationMillis = animationDuration),
        label = "donut_animation"
    )

    LaunchedEffect(segments) {
        animationPlayed = 1f
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                val canvasSize = size.minDimension
                val radius = (canvasSize - strokeWidth) / 2
                val topLeft = Offset(
                    (size.width - radius * 2) / 2,
                    (size.height - radius * 2) / 2
                )
                val arcSize = Size(radius * 2, radius * 2)

                var startAngle = -90f

                segments.forEach { segment ->
                    val sweepAngle = if (total > 0) {
                        ((segment.value / total) * 360f * animatedProgress).toFloat()
                    } else 0f

                    drawArc(
                        color = segment.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )

                    startAngle += sweepAngle
                }
            }

            // Center text
            if (centerText.isNotEmpty()) {
                Text(
                    text = centerText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // Legend
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            segments.forEach { segment ->
                val percentage = if (total > 0) {
                    (segment.value / total * 100)
                } else 0f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(12.dp)) {
                        drawCircle(color = segment.color)
                    }
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = segment.label,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = FormatterUtil.formatCurrency(segment.value),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = "${String.format("%.1f", percentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DonutChartPreview() {
    MaterialTheme {
        DonutChart(
            segments = listOf(
                DonutSegment("Makanan", 500000.0, Color(0xFFFF5722)),
                DonutSegment("Transportasi", 300000.0, Color(0xFF9C27B0)),
                DonutSegment("Hiburan", 200000.0, Color(0xFFE91E63)),
                DonutSegment("Belanja", 150000.0, Color(0xFF3F51B5))
            ),
            centerText = "Rp 1.150.000",
            modifier = Modifier.padding(16.dp)
        )
    }
}
