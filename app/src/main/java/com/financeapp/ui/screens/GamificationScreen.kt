package com.financeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.data.model.Achievement
import com.financeapp.data.model.Challenge
import com.financeapp.data.model.DefaultAchievements
import com.financeapp.data.model.XpHistory
import com.financeapp.ui.components.AchievementBadge
import com.financeapp.ui.components.LevelCard
import com.financeapp.ui.components.StreakCard
import com.financeapp.ui.viewmodel.GamificationViewModel
import com.financeapp.ui.utils.FormatterUtil
import java.time.format.DateTimeFormatter

@Composable
fun GamificationScreen(
    viewModel: GamificationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading && state.userProgress == null) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.errorMessage != null && state.userProgress == null) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                val msg = state.errorMessage
                Text(msg ?: "", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.retry() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Muat ulang", modifier = Modifier.padding(end = 8.dp))
                    Text("Coba Lagi")
                }
            }
        }
        return
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Profil", "Prestasi")

    Column(modifier = Modifier.fillMaxSize()) {
        // Title
        Text(
            text = "Profil & Prestasi",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
        )

        // Tab bar
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.padding(horizontal = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> ProfileTab(state = state, viewModel = viewModel)
            1 -> AchievementTab(state = state)
        }
    }
}

@Composable
private fun ProfileTab(
    state: com.financeapp.ui.viewmodel.GamificationUiState,
    viewModel: GamificationViewModel
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM HH:mm") }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Level + Streak cards
        state.userProgress?.let { progress ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LevelCard(
                        progress = progress,
                        modifier = Modifier.weight(1.5f)
                    )
                    StreakCard(
                        currentStreak = progress.currentStreak,
                        bestStreak = progress.bestStreak,
                        streakFreezes = progress.streakFreezes,
                        onUseFreeze = { viewModel.useFreeze() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Stats summary
        state.userProgress?.let { progress ->
            item {
                StatCard(progress.currentLevel, progress.totalXp, progress.bestStreak)
            }
        }

        // 3. Active Challenges
        if (state.activeChallenges.isNotEmpty()) {
            item { SectionTitle("Tantangan Aktif") }
            state.activeChallenges.forEach { challenge ->
                item { ChallengeCard(challenge = challenge) }
            }
        }

        // 4. Completed Challenges
        if (state.completedChallenges.isNotEmpty()) {
            item { SectionTitle("Tantangan Terselesaikan") }
            state.completedChallenges.take(3).forEach { challenge ->
                item { ChallengeCard(challenge = challenge) }
            }
        }

        // 5. XP History
        if (state.recentXpHistory.isNotEmpty()) {
            item { SectionTitle("Riwayat XP") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        state.recentXpHistory.forEach { xp ->
                            XpHistoryRow(xp = xp, dateFormatter = dateFormatter)
                            if (xp != state.recentXpHistory.last()) {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementTab(state: com.financeapp.ui.viewmodel.GamificationUiState) {
    val achievements = state.achievements
    val unlockedCount = achievements.count { it.isUnlocked }
    val totalCount = achievements.size

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFF8F00),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$unlockedCount / $totalCount",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pencapaian Terbuka",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        if (achievements.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada pencapaian",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Group & display by category
        val groupedByCategory = achievements.groupBy { it.category }
        groupedByCategory.forEach { (category, categoryAchievements) ->
            item {
                SectionTitle(title = category)
            }
            categoryAchievements.forEach { achievement ->
                item {
                    AchievementBadge(achievement = achievement)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun StatCard(level: Int, totalXpEarned: Int, bestStreak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(Icons.Filled.CheckCircle, "$level", "Level", Color(0xFF2E7D32))
            StatItem(Icons.Filled.Star, "$totalXpEarned", "Total XP", Color(0xFFFF8F00))
            StatItem(Icons.Filled.LocalFireDepartment, "$bestStreak", "Best Streak", Color(0xFFE65100))
        }
    }
}

@Composable
private fun StatItem(icon: ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ChallengeCard(challenge: Challenge) {
    val progress = if (challenge.targetValue > 0)
        (challenge.currentValue.toFloat() / challenge.targetValue).coerceIn(0f, 1f)
    else 0f

    val typeColor = when {
        challenge.challengeType.contains("weekly", ignoreCase = true) -> Color(0xFF1565C0)
        challenge.challengeType.contains("monthly", ignoreCase = true) -> Color(0xFF7B1FA2)
        else -> Color(0xFFE65100)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = challenge.challengeType,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = typeColor,
                    modifier = Modifier
                        .background(typeColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (challenge.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFF8F00),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = challenge.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (challenge.isCompleted) Color(0xFF2E7D32) else typeColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${challenge.currentValue} / ${challenge.targetValue}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "+${challenge.xpReward} XP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFF8F00)
                )
            }
            if (!challenge.isCompleted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Deadline: ${challenge.endDate}",
                    fontSize = 10.sp,
                    color = Color(0xFFE65100).copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun XpHistoryRow(xp: XpHistory, dateFormatter: DateTimeFormatter) {
    val sourceLabel = when (xp.source) {
        "TRANSACTION" -> "Mencatat Transaksi"
        "STREAK" -> "Streak Bonus"
        "BUDGET_ADHERENCE" -> "Patuh Budget"
        "ACHIEVEMENT" -> "Bonus Menabung"
        "QUEST" -> "Quest Selesai"
        "CHALLENGE" -> "Tantangan Selesai"
        "DAILY_LOGIN" -> "Login Harian"
        else -> xp.source
    }

    val icon = when (xp.source) {
        "TRANSACTION" -> Icons.Filled.CheckCircle
        "STREAK" -> Icons.Filled.LocalFireDepartment
        "BUDGET_ADHERENCE" -> Icons.Filled.Star
        "ACHIEVEMENT" -> Icons.Filled.TrendingUp
        "QUEST" -> Icons.Filled.EmojiEvents
        "CHALLENGE" -> Icons.Filled.EmojiEvents
        "DAILY_LOGIN" -> Icons.Filled.Star
        else -> Icons.Filled.Help
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = sourceLabel, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(
                text = xp.createdAt.format(dateFormatter),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "+${xp.amount} XP",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFFF8F00)
        )
    }
}
