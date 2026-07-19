package com.financeapp.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.financeapp.ui.theme.Spacing
import com.financeapp.ui.theme.financeColors
import com.financeapp.ui.utils.TierState
import com.financeapp.ui.utils.TierUtils

@Composable
fun XpSummaryDialog(
    totalXp: Int,
    currentLevel: Int,
    onDismiss: () -> Unit
) {
    val currentTier = TierUtils.getTierForXp(totalXp)
    val tierProgress = TierUtils.getTierProgress(totalXp)
    val xpToNext = TierUtils.getXpToNextTier(totalXp)
    val tierStatusList = TierUtils.getTierStatusList(totalXp)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TierBadge(tier = currentTier.tier, size = 72.dp)
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = currentTier.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = currentTier.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column {
                // XP stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalXp",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.financeColors.accent
                        )
                        Text(
                            text = "Total XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Lv.$currentLevel",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Level",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Tier ${currentTier.tier}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tier",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // Tier progress bar
                if (xpToNext != null) {
                    Text(
                        text = "Progress ke Tier berikutnya",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    LinearProgressIndicator(
                        progress = { tierProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(MaterialTheme.shapes.extraSmall),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "$xpToNext XP lagi ke tier berikutnya",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Anda sudah di tier tertinggi!",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.financeColors.income,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // Tier list
                Text(
                    text = "Semua Tier",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(Spacing.sm))

                tierStatusList.forEach { tierStatus ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tier icon
                        when (tierStatus.status) {
                            TierState.COMPLETED -> Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Selesai",
                                tint = MaterialTheme.colorScheme.financeColors.income,
                                modifier = Modifier.size(20.dp)
                            )
                            TierState.CURRENT -> Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Saat ini",
                                tint = MaterialTheme.colorScheme.financeColors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            TierState.LOCKED -> Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Terkunci",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(Spacing.sm))

                        // Tier name
                        Text(
                            text = tierStatus.tier.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (tierStatus.status == TierState.CURRENT)
                                FontWeight.Bold else FontWeight.Normal,
                            color = when (tierStatus.status) {
                                TierState.COMPLETED -> MaterialTheme.colorScheme.onSurface
                                TierState.CURRENT -> MaterialTheme.colorScheme.primary
                                TierState.LOCKED -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // XP range
                        Text(
                            text = if (tierStatus.tier.xpMax == -1) "${tierStatus.tier.xpMin}+ XP"
                            else "${tierStatus.tier.xpMin}-${tierStatus.tier.xpMax} XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}
