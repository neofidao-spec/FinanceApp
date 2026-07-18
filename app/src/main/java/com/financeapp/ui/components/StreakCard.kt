package com.financeapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financeapp.ui.theme.Spacing
import com.financeapp.ui.theme.financeColors

@Composable
fun StreakCard(
    currentStreak: Int,
    bestStreak: Int,
    streakFreezes: Int,
    onUseFreeze: () -> Unit,
    modifier: Modifier = Modifier
) {
    val flameColor = when {
        currentStreak >= 30 -> Color(0xFFFF6F00) // Orange — hot streak
        currentStreak >= 7 -> MaterialTheme.colorScheme.financeColors.accent // Amber
        currentStreak >= 3 -> Color(0xFFFFA726) // Light orange
        currentStreak > 0 -> Color(0xFFEF5350) // Red — starting
        else -> Color(0xFFBDBDBD) // Grey — inactive
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
                .padding(Spacing.md)
        ) {
            // Flame icon + streak count — centered for compactness
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = flameColor.copy(alpha = animatedAlpha),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$currentStreak hari",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = flameColor.copy(alpha = animatedAlpha)
                )
            }

            // Best streak — subtle below
            Text(
                text = "Terbaik: $bestStreak hari",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Freeze row — only when has freezes
            if (streakFreezes > 0) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onUseFreeze,
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AcUnit,
                            contentDescription = "Gunakan freeze ($streakFreezes tersisa)",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "$streakFreezes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}