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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.sp

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
        currentStreak >= 7 -> Color(0xFFFF8F00) // Amber
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = flameColor.copy(alpha = 0.08f * animatedAlpha)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flame icon + streak count
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = flameColor.copy(alpha = animatedAlpha),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "$currentStreak",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = flameColor.copy(alpha = animatedAlpha)
                    )
                    Text(
                        text = "hari berturut-turut",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Best streak + freeze button
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Terbaik: $bestStreak hari",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (streakFreezes > 0) {
                    IconButton(
                        onClick = onUseFreeze,
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = Color(0xFF42A5F5).copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AcUnit,
                                contentDescription = "Gunakan freeze ($streakFreezes tersisa)",
                                tint = Color(0xFF42A5F5),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Freeze streaks: 0",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
