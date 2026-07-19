package com.financeapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.financeapp.ui.theme.Spacing
import com.financeapp.ui.theme.financeColors

@Composable
fun StreakCard(
    currentStreak: Int,
    bestStreak: Int,
    modifier: Modifier = Modifier
) {
    val flameColor = when {
        currentStreak >= 30 -> Color(0xFFFF6F00)
        currentStreak >= 7 -> MaterialTheme.colorScheme.financeColors.accent
        currentStreak >= 3 -> Color(0xFFFFA726)
        currentStreak > 0 -> Color(0xFFEF5350)
        else -> Color(0xFFBDBDBD)
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (currentStreak > 0) 1f else 0.5f,
        animationSpec = tween(600)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row 1: Icon — same height as LevelCard badge (44dp)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        color = flameColor.copy(alpha = 0.15f * animatedAlpha)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = flameColor.copy(alpha = animatedAlpha),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Row 2: Main number + label
            Text(
                text = "$currentStreak hari",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = flameColor.copy(alpha = animatedAlpha),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Row 3: Bottom info — best streak
            Text(
                text = "Terbaik: $bestStreak",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
