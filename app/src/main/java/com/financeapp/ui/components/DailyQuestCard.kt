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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financeapp.data.model.DailyQuest
import com.financeapp.ui.theme.Spacing
import com.financeapp.ui.theme.financeColors

@Composable
fun DailyQuestCard(
    quests: List<DailyQuest>,
    modifier: Modifier = Modifier
) {
    if (quests.isEmpty()) return

    val completedCount = quests.count { it.isCompleted }
    val totalCount = quests.size
    val overallProgress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = overallProgress,
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quest Harian",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$completedCount/$totalCount",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (completedCount == totalCount) MaterialTheme.colorScheme.financeColors.income
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Overall progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(Spacing.xs)),
                color = if (completedCount == totalCount) MaterialTheme.colorScheme.financeColors.income
                else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(Spacing.smd))

            // Individual quest items
            quests.forEach { quest ->
                QuestItem(quest = quest)
                Spacer(modifier = Modifier.height(Spacing.sm))
            }
        }
    }
}

@Composable
private fun QuestItem(quest: DailyQuest) {
    val progress = if (quest.targetValue > 0) {
        quest.currentValue.toFloat() / quest.targetValue
    } else 0f

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Check icon
        Icon(
            imageVector = if (quest.isCompleted) Icons.Filled.CheckCircle
            else Icons.Filled.RadioButtonUnchecked,
            contentDescription = if (quest.isCompleted) "Selesai" else "Belum selesai",
            tint = if (quest.isCompleted) MaterialTheme.colorScheme.financeColors.income
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(Spacing.smd))

        // Quest details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = quest.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (quest.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(Spacing.xs)),
                color = if (quest.isCompleted) MaterialTheme.colorScheme.financeColors.income
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
        }

        Spacer(modifier = Modifier.width(Spacing.sm))

        // XP reward
        Text(
            text = "+${quest.xpReward} XP",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (quest.isCompleted) MaterialTheme.colorScheme.financeColors.income.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.financeColors.accent
        )
    }
}
