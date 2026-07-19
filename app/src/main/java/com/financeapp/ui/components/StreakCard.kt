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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    streakFreezes: Int,
    onUseFreeze: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFreezeDialog by remember { mutableStateOf(false) }

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

            // Freeze section — only when user has freezes
            if (streakFreezes > 0) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showFreezeDialog = true },
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AcUnit,
                            contentDescription = "Gunakan streak freeze",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "Freeze ($streakFreezes)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Freeze confirmation dialog
    if (showFreezeDialog) {
        AlertDialog(
            onDismissRequest = { showFreezeDialog = false },
            title = { Text("Gunakan Streak Freeze?") },
            text = {
                Text(
                    "Streak freeze melindungi streak Anda selama 1 hari jika Anda " +
                    "tidak mencatat transaksi. Anda memiliki $streakFreezes freeze tersisa."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUseFreeze()
                        showFreezeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Gunakan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFreezeDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
