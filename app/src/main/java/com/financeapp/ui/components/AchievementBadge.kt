package com.financeapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financeapp.data.model.Achievement
import com.financeapp.data.model.AchievementCategory

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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Text(
                text = if (isUnlocked) achievement.icon else "🔒",
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isUnlocked) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = achievement.description,
                    fontSize = 12.sp,
                    color = if (isUnlocked) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (!isUnlocked) {
                    LinearProgressIndicator(
                        progress = { achievement.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${achievement.currentValue} / ${achievement.targetValue}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "✅ Terbuka",
                        fontSize = 10.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AchievementBadgePreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AchievementBadge(
                achievement = Achievement(
                    name = "Pencatat Pemula",
                    description = "Catat 5 transaksi",
                    icon = "📝",
                    category = AchievementCategory.TRANSACTIONS.name,
                    targetValue = 5,
                    currentValue = 3,
                    isUnlocked = false
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            AchievementBadge(
                achievement = Achievement(
                    name = "First Save",
                    description = "Dapatkan pemasukan pertama",
                    icon = "🌱",
                    category = AchievementCategory.SAVINGS.name,
                    targetValue = 1,
                    currentValue = 1,
                    isUnlocked = true
                )
            )
        }
    }
}
