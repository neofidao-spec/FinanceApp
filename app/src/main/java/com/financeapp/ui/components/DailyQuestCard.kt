package com.financeapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.financeapp.data.model.QuestCategory
import com.financeapp.data.repository.QuestWithTemplate
import com.financeapp.ui.theme.Spacing
import com.financeapp.ui.theme.financeColors

@Composable
fun DailyQuestCard(
    quests: List<QuestWithTemplate>,
    onQuestClick: ((QuestWithTemplate) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (quests.isEmpty()) return

    val completedCount = quests.count { it.assignment.isCompleted }
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
                QuestItem(
                    quest = quest,
                    onClick = { onQuestClick?.invoke(quest) }
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
            }
        }
    }
}

@Composable
private fun QuestItem(
    quest: QuestWithTemplate,
    onClick: (() -> Unit)? = null
) {
    val isCompleted = quest.assignment.isCompleted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && !isCompleted)
                    Modifier.clickable { onClick() } else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Check icon
        Icon(
            imageVector = if (isCompleted) Icons.Filled.CheckCircle
            else Icons.Filled.RadioButtonUnchecked,
            contentDescription = if (isCompleted) "Selesai" else "Belum selesai",
            tint = if (isCompleted) MaterialTheme.colorScheme.financeColors.income
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(Spacing.smd))

        // Quest details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = quest.template.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = quest.template.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(Spacing.sm))

        // Category-colored XP reward
        val categoryColor = when (quest.template.category) {
            QuestCategory.PENCATATAN -> MaterialTheme.colorScheme.primary
            QuestCategory.BUDGETING -> MaterialTheme.colorScheme.financeColors.income
            QuestCategory.EKSPLORASI -> MaterialTheme.colorScheme.financeColors.accent
            QuestCategory.DISIPLIN -> MaterialTheme.colorScheme.financeColors.expense
            QuestCategory.REVIEW -> MaterialTheme.colorScheme.tertiary
        }
        Text(
            text = "+${quest.template.xpReward} XP",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isCompleted) MaterialTheme.colorScheme.financeColors.income.copy(alpha = 0.6f)
            else categoryColor
        )
    }
}
