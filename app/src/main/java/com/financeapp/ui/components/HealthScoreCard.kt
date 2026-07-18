package com.financeapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financeapp.domain.HealthScore

@Composable
fun HealthScoreCard(
    score: Int,
    category: String,
    description: String,
    trend: HealthScore.Trend,
    modifier: Modifier = Modifier
) {
    val scoreColor = when {
        score >= 80 -> MaterialTheme.colorScheme.primary
        score >= 60 -> MaterialTheme.colorScheme.primary
        score >= 40 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    val trendIcon = when (trend) {
        HealthScore.Trend.UP -> Icons.Filled.TrendingUp
        HealthScore.Trend.DOWN -> Icons.Filled.TrendingDown
        else -> Icons.Filled.TrendingUp
    }

    // Animated ring progress — spring for natural feel
    val animatedScore by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 100f
        ),
        label = "healthScoreRing"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = scoreColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score ring — animated Canvas
            Box(
                modifier = Modifier
                    .size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(60.dp)) {
                    val sweepAngle = animatedScore * 360f
                    val strokeWidth = 6.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val topLeft = Offset(
                        (size.width - radius * 2) / 2 - strokeWidth / 2,
                        (size.height - radius * 2) / 2 - strokeWidth / 2
                    )

                    // Track
                    drawArc(
                        color = scoreColor.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = androidx.compose.ui.geometry.Size(
                            size.width - strokeWidth,
                            size.height - strokeWidth
                        ),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    // Progress arc with spring animation
                    drawArc(
                        color = scoreColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = androidx.compose.ui.geometry.Size(
                            size.width - strokeWidth,
                            size.height - strokeWidth
                        ),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "$score",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Kesehatan Keuangan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = if (trend == HealthScore.Trend.UP) "Meningkat" else "Menurun",
                        tint = if (trend == HealthScore.Trend.UP) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = category,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = scoreColor,
                modifier = Modifier
                    .background(
                        color = scoreColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
