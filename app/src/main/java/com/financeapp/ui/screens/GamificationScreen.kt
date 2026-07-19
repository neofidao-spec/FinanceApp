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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.financeapp.ui.theme.financeColors
import com.financeapp.ui.theme.Spacing

@Composable
fun GamificationScreen(
    viewModel: GamificationViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading && state.userProgress == null) {
        Box(
            modifier = Modifier.fillMaxSize().padding(Spacing.xl),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.errorMessage != null && state.userProgress == null) {
        Box(
            modifier = Modifier.fillMaxSize().padding(Spacing.xl),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.TrendingUp,
                    contentDescription = "Kesalahan memuat data",
                    modifier = Modifier.size(Spacing.iconXl),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                val msg = state.errorMessage
                Text(msg ?: "", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(Spacing.md))
                Button(
                    onClick = { viewModel.retry() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Muat ulang", modifier = Modifier.padding(end = Spacing.sm))
                    Text("Coba Lagi")
                }
            }
        }
        return
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Profil", "Prestasi")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        // Title with settings icon
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = Spacing.md, top = Spacing.md, end = Spacing.md, bottom = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Profil & Prestasi",
                style = MaterialTheme.typography.headlineLarge
            )
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Pengaturan",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Freeze button — small, near settings
            val progress = state.userProgress
            if (progress != null && progress.streakFreezes > 0) {
                var showFreezeDialog by remember { mutableStateOf(false) }

                IconButton(
                    onClick = { showFreezeDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text(
                                    text = "${progress.streakFreezes}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AcUnit,
                            contentDescription = "Streak freeze tersedia",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (showFreezeDialog) {
                    AlertDialog(
                        onDismissRequest = { showFreezeDialog = false },
                        title = { Text("Gunakan Streak Freeze?") },
                        text = {
                            Text(
                                "Streak freeze melindungi streak Anda selama 1 hari " +
                                "jika Anda tidak mencatat transaksi. " +
                                "Anda memiliki ${progress.streakFreezes} freeze tersisa."
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                viewModel.useFreeze()
                                showFreezeDialog = false
                            }) {
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
        }

        // Tab bar
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.padding(horizontal = Spacing.md),
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
            .padding(horizontal = Spacing.md),
        contentPadding = PaddingValues(vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // 1. Level + Streak cards
        state.userProgress?.let { progress ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.smd)
                ) {
                    LevelCard(
                        progress = progress,
                        modifier = Modifier.weight(1f)
                    )
                    StreakCard(
                        currentStreak = progress.currentStreak,
                        bestStreak = progress.bestStreak,
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
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
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
            .padding(horizontal = Spacing.md),
        contentPadding = PaddingValues(vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.smd)
    ) {
        // Summary header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.mdLg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "Prestasi",
                        tint = MaterialTheme.colorScheme.financeColors.accent,
                        modifier = Modifier.size(Spacing.iconLg)
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = "$unlockedCount / $totalCount",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = "Pencapaian Terbuka",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Spacing.smd))
                    LinearProgressIndicator(
                        progress = { if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Spacing.sm)
                            .clip(RoundedCornerShape(Spacing.xs)),
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
                        .padding(Spacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "Belum ada pencapaian",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text(
                            text = "Belum Ada Pencapaian",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "Mulai catat transaksi untuk membuka pencapaian",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
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
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = Spacing.xs)
    )
}

@Composable
private fun StatCard(level: Int, totalXpEarned: Int, bestStreak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(Icons.Filled.CheckCircle, "$level", "Level", MaterialTheme.colorScheme.financeColors.income)
            StatItem(Icons.Filled.Star, "$totalXpEarned", "Total XP", MaterialTheme.colorScheme.financeColors.accent)
            StatItem(Icons.Filled.LocalFireDepartment, "$bestStreak", "Best Streak", MaterialTheme.colorScheme.financeColors.accent)
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
            Icon(icon, contentDescription = "Statistik", tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ChallengeCard(challenge: Challenge) {
    val progress = if (challenge.targetValue > 0)
        (challenge.currentValue.toFloat() / challenge.targetValue).coerceIn(0f, 1f)
    else 0f

    val typeColor = when {
        challenge.challengeType.contains("weekly", ignoreCase = true) -> MaterialTheme.colorScheme.primary
        challenge.challengeType.contains("monthly", ignoreCase = true) -> MaterialTheme.colorScheme.financeColors.accent
        else -> MaterialTheme.colorScheme.financeColors.accent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = challenge.challengeType,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = typeColor,
                    modifier = Modifier
                        .background(typeColor.copy(alpha = 0.12f), MaterialTheme.shapes.extraSmall)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                if (challenge.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "Tantangan selesai",
                        tint = MaterialTheme.colorScheme.financeColors.accent,
                        modifier = Modifier.size(Spacing.iconT)
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(text = challenge.name, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(Spacing.xs)),
                color = if (challenge.isCompleted) MaterialTheme.colorScheme.financeColors.income else typeColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${challenge.currentValue} / ${challenge.targetValue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "+${challenge.xpReward} XP",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.financeColors.accent
                )
            }
            if (!challenge.isCompleted) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = "Deadline: ${challenge.endDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.financeColors.accent.copy(alpha = 0.7f)
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
            contentDescription = "Riwayat XP",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Spacing.iconXxs)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = sourceLabel, style = MaterialTheme.typography.titleSmall)
            Text(
                text = xp.createdAt.format(dateFormatter),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "+${xp.amount} XP",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.financeColors.accent
        )
    }
}
