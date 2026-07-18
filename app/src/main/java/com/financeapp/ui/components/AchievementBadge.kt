package com.financeapp.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financeapp.ui.theme.Spacing
import com.financeapp.ui.theme.financeColors
import com.financeapp.data.model.Achievement

fun resolveAchievementIcon(iconName: String): ImageVector = when (iconName) {
    "edit_note" -> Icons.Filled.EditNote
    "menu_book" -> Icons.Filled.MenuBook
    "military_tech" -> Icons.Filled.MilitaryTech
    "savings" -> Icons.Filled.Savings
    "workspace_premium" -> Icons.Filled.WorkspacePremium
    "eco" -> Icons.Filled.Eco
    "diamond" -> Icons.Filled.Diamond
    "local_fire_department" -> Icons.Filled.LocalFireDepartment
    "bolt" -> Icons.Filled.Bolt
    else -> Icons.Filled.WorkspacePremium
}

@Composable
fun AchievementBadge(
    achievement: Achievement,
    modifier: Modifier = Modifier
) {
    val isUnlocked = achievement.isUnlocked
    val cardColor = if (isUnlocked) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val iconAlpha = if (isUnlocked) 1f else 0.4f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) resolveAchievementIcon(achievement.icon) else Icons.Filled.Lock,
                    contentDescription = achievement.name,
                    tint = if (isUnlocked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(Spacing.smd))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUnlocked) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
                Spacer(modifier = Modifier.height(Spacing.xs))

                if (!isUnlocked) {
                    LinearProgressIndicator(
                        progress = { achievement.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(Spacing.xs)),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${achievement.currentValue} / ${achievement.targetValue}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Terbuka",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.financeColors.income,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
