package com.financeapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.financeapp.ui.theme.Spacing
import com.financeapp.ui.theme.financeColors

/**
 * Data class representing a monthly data point for the trend chart.
 */
data class MonthlyData(
    val month: String,
    val income: Double,
    val expense: Double
)

/**
 * A line chart showing income vs expense trends over months.
 *
 * @param data List of MonthlyData (month, income, expense)
 * @param modifier Modifier for the composable
 * @param incomeColor Color for the income line
 * @param expenseColor Color for the expense line
 * @param animationDuration Duration of the line drawing animation
 */
@Composable
fun MonthlyTrendChart(
    data: List<MonthlyData>,
    modifier: Modifier = Modifier,
    incomeColor: Color = MaterialTheme.colorScheme.financeColors.income,
    expenseColor: Color = MaterialTheme.colorScheme.financeColors.expense,
    animationDuration: Int = 1500
) {
    if (data.isEmpty()) return

    // Capture colors outside Canvas
    val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val dotCenterColor = MaterialTheme.colorScheme.surface

    // Animated progress for line drawing
    var animationPlayed by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationPlayed,
        animationSpec = tween(durationMillis = animationDuration),
        label = "trend_animation"
    )

    LaunchedEffect(data) {
        animationPlayed = 1f
    }

    val maxValue = data.flatMap { listOf(it.income, it.expense) }.maxOrNull()

    Column(modifier = modifier.fillMaxWidth()) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.width(Spacing.smd).height(3.dp)) {
                drawLine(
                    color = incomeColor,
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                    strokeWidth = size.height,
                    cap = StrokeCap.Round
                )
            }
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = "Pemasukan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(Spacing.md))
            Canvas(modifier = Modifier.width(Spacing.smd).height(3.dp)) {
                drawLine(
                    color = expenseColor,
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                    strokeWidth = size.height,
                    cap = StrokeCap.Round
                )
            }
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = "Pengeluaran",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = Spacing.md, vertical = Spacing.sm)
        ) {
            val chartWidth = size.width
            val chartHeight = size.height
            val pointCount = data.size
            if (pointCount < 2) return@Canvas

            val xStep = chartWidth / (pointCount - 1).toFloat()
            val yScale = if (maxValue != null && maxValue > 0) chartHeight / maxValue.toFloat() else 1f

            // Draw Y-axis gridlines
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = chartHeight - (chartHeight / gridLines) * i
                drawLine(
                    color = gridLineColor,
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1f
                )
            }

            // Draw income line path
            val incomePath = Path()
            val incomePoints = data.mapIndexed { index, monthlyData ->
                Offset(
                    x = index * xStep,
                    y = chartHeight - (monthlyData.income.toFloat() * yScale)
                )
            }

            if (incomePoints.isNotEmpty()) {
                incomePath.moveTo(incomePoints[0].x, incomePoints[0].y)
                for (i in 1 until incomePoints.size) {
                    // Only draw up to animated progress
                    val progressLimit = i.toFloat() / (incomePoints.size - 1)
                    if (progressLimit <= animatedProgress) {
                        incomePath.lineTo(incomePoints[i].x, incomePoints[i].y)
                    }
                }
                drawPath(
                    path = incomePath,
                    color = incomeColor,
                    style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // Draw expense line path
            val expensePath = Path()
            val expensePoints = data.mapIndexed { index, monthlyData ->
                Offset(
                    x = index * xStep,
                    y = chartHeight - (monthlyData.expense.toFloat() * yScale)
                )
            }

            if (expensePoints.isNotEmpty()) {
                expensePath.moveTo(expensePoints[0].x, expensePoints[0].y)
                for (i in 1 until expensePoints.size) {
                    val progressLimit = i.toFloat() / (expensePoints.size - 1)
                    if (progressLimit <= animatedProgress) {
                        expensePath.lineTo(expensePoints[i].x, expensePoints[i].y)
                    }
                }
                drawPath(
                    path = expensePath,
                    color = expenseColor,
                    style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // Draw dot markers
            if (animatedProgress >= 1f) {
                incomePoints.forEach { point ->
                    drawCircle(
                        color = incomeColor,
                        radius = 5f,
                        center = point
                    )
                    drawCircle(
                        color = dotCenterColor,
                        radius = 3f,
                        center = point
                    )
                }

                expensePoints.forEach { point ->
                    drawCircle(
                        color = expenseColor,
                        radius = 5f,
                        center = point
                    )
                    drawCircle(
                        color = dotCenterColor,
                        radius = 3f,
                        center = point
                    )
                }
            }
        }

        // X-axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { monthlyData ->
                Text(
                    text = monthlyData.month,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MonthlyTrendChartPreview() {
    MaterialTheme {
        MonthlyTrendChart(
            data = listOf(
                MonthlyData("Jan", 5000000.0, 3500000.0),
                MonthlyData("Feb", 5500000.0, 4000000.0),
                MonthlyData("Mar", 4800000.0, 3200000.0),
                MonthlyData("Apr", 6000000.0, 4500000.0),
                MonthlyData("Mei", 5200000.0, 3800000.0),
                MonthlyData("Jun", 5800000.0, 4100000.0)
            ),
            modifier = Modifier.padding(Spacing.md)
        )
    }
}
