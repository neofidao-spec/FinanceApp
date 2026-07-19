package com.financeapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.data.model.TransactionType
import com.financeapp.data.model.TransactionWithCategory
import com.financeapp.data.model.UserProgress
import com.financeapp.ui.components.AnimatedNumber
import com.financeapp.ui.components.ShimmerBalanceCard
import com.financeapp.ui.components.ShimmerTransactionItem
import com.financeapp.ui.components.BudgetProgressRing
import com.financeapp.ui.components.DailyQuestCard
import com.financeapp.ui.components.DonutChart
import com.financeapp.ui.components.DonutSegment
import com.financeapp.ui.components.HealthScoreCard
import com.financeapp.ui.components.LevelCard
import com.financeapp.ui.components.MonthlyData
import com.financeapp.ui.components.MonthlyTrendChart
import com.financeapp.ui.components.StreakCard
import com.financeapp.ui.theme.Spacing
import com.financeapp.ui.theme.financeColors
import com.financeapp.ui.utils.FinanceIcons
import androidx.compose.ui.res.stringResource
import com.financeapp.R
import com.financeapp.ui.utils.FormatterUtil
import com.financeapp.ui.viewmodel.DashboardUiState
import com.financeapp.ui.viewmodel.DashboardViewModel
import com.financeapp.ui.viewmodel.GamificationUiState
import com.financeapp.ui.viewmodel.GamificationViewModel
import com.financeapp.ui.viewmodel.MonthlyTrendData

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    gamificationViewModel: GamificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gamificationState by gamificationViewModel.uiState.collectAsState()

    // Complete "Cek Dashboard" quest on first visit
    LaunchedEffect(gamificationState.dailyQuests) {
        val dashboardQuest = gamificationState.dailyQuests.find {
            it.questType == "DASHBOARD_VISIT" && !it.isCompleted
        }
        if (dashboardQuest != null) {
            gamificationViewModel.completeQuest(dashboardQuest)
        }
    }

    DashboardContent(
        uiState = uiState,
        gamificationState = gamificationState,
        onRetry = { viewModel.retry() },
        onUseFreeze = { gamificationViewModel.useFreeze() }
    )
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    gamificationState: GamificationUiState,
    onRetry: () -> Unit,
    onUseFreeze: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
    // Loading state — shimmer skeleton
    if (uiState.isLoading) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(horizontal = Spacing.md),
            contentPadding = PaddingValues(vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item { ShimmerBalanceCard() }
            item { ShimmerTransactionItem() }
            item { ShimmerTransactionItem() }
            item { ShimmerTransactionItem() }
        }
        return@Scaffold
    }

    // Error state
    if (uiState.errorMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.xl),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.TrendingDown,
                    contentDescription = stringResource(R.string.common_error_occurred),
                    modifier = Modifier.size(Spacing.iconXl),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = stringResource(R.string.common_retry),
                        modifier = Modifier.padding(end = Spacing.sm)
                    )
                    Text(stringResource(R.string.common_retry))
                }
            }
        }
        return@Scaffold
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues)
            .padding(horizontal = Spacing.md),
        contentPadding = PaddingValues(vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // 1. Balance Card - Premium gradient design
        item {
            BalanceCard(
                balance = uiState.balance,
                totalIncome = uiState.totalIncome,
                totalExpense = uiState.totalExpense
            )
        }

        // 2. Gamification summary — compact single card (Level + Streak in one row)
        if (gamificationState.userProgress != null && !gamificationState.isLoading) {
            item {
                GamificationSummaryCard(
                    progress = gamificationState.userProgress,
                    onUseFreeze = onUseFreeze
                )
            }
        }

        // 3. Budget Overview + Health Score (combined horizontal row)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                // Budget progress ring(s)
                if (uiState.budgetSummaries.isNotEmpty()) {
                    uiState.budgetSummaries.take(2).forEach { budget ->
                        BudgetProgressRing(
                            progress = (budget.percentage / 100f).coerceIn(0f, 1f),
                            label = budget.category.name,
                            amountText = FormatterUtil.formatCurrency(budget.currentSpent),
                            ringSize = 72,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // Empty budget state — still show placeholder ring
                    BudgetProgressRing(
                        progress = 0f,
                        label = "Belum ada budget",
                        amountText = "Tap +",
                        ringSize = 72,
                        modifier = Modifier.weight(1f)
                    )
                    BudgetProgressRing(
                        progress = 0f,
                        label = "Tambah budget",
                        amountText = "di Budget",
                        ringSize = 72,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Health score ring (compact)
                uiState.healthScore?.let { health ->
                    HealthScoreRing(
                        score = health.score,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Daily Quests
        if (gamificationState.dailyQuests.isNotEmpty() && !gamificationState.isLoading) {
            item {
                DailyQuestCard(quests = gamificationState.dailyQuests)
            }
        }

        // 5. DonutChart - Expense breakdown
        if (uiState.categoryBreakdown.isNotEmpty()) {
            item {
                SectionHeader(title = stringResource(R.string.dashboard_expense_by_category))
                DonutChart(
                    segments = uiState.categoryBreakdown.map { summary ->
                        DonutSegment(
                            label = summary.category.name,
                            value = summary.total,
                            color = FinanceIcons.getColorFromHex(summary.category.color)
                        )
                    },
                    centerText = FormatterUtil.formatCurrency(uiState.totalExpense),
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
            }
        }

        // 6. Monthly Trend Chart
        if (uiState.monthlyTrend.isNotEmpty()) {
            item {
                SectionHeader(title = stringResource(R.string.dashboard_monthly_trend))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    MonthlyTrendChart(
                        data = uiState.monthlyTrend.map { trend ->
                            MonthlyData(
                                month = trend.month,
                                income = trend.income,
                                expense = trend.expense
                            )
                        },
                        modifier = Modifier.padding(Spacing.md)
                    )
                }
            }
        }

        // 9. Recent Transactions
        item {
            SectionHeader(title = stringResource(R.string.dashboard_recent_transactions))
        }

        if (uiState.recentTransactions.isEmpty()) {
            item {
                EmptyTransactionsState()
            }
        } else {
            items(uiState.recentTransactions.take(5)) { transaction ->
                TransactionItem(transaction = transaction)
            }
        }
    }
    }
}

@Composable
private fun HealthScoreRing(
    score: Int,
    modifier: Modifier = Modifier
) {
    val scoreColor = when {
        score >= 80 -> MaterialTheme.colorScheme.financeColors.income
        score >= 60 -> MaterialTheme.colorScheme.primary
        score >= 40 -> MaterialTheme.colorScheme.financeColors.warning
        else -> MaterialTheme.colorScheme.financeColors.expense
    }

    val animatedScore by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 100f),
        label = "healthRing"
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
                .padding(Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                val trackColor = scoreColor.copy(alpha = 0.15f)
                Canvas(modifier = Modifier.size(72.dp)) {
                    val strokeWidth = 6.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = androidx.compose.ui.geometry.Size(
                            size.width - strokeWidth, size.height - strokeWidth
                        ),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = scoreColor,
                        startAngle = -90f,
                        sweepAngle = animatedScore * 360f,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = androidx.compose.ui.geometry.Size(
                            size.width - strokeWidth, size.height - strokeWidth
                        ),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = "Kesehatan",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BalanceCard(
    balance: Double,
    totalIncome: Double,
    totalExpense: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            Text(
                text = stringResource(R.string.dashboard_total_balance),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            AnimatedNumber(
                value = balance,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                format = { FormatterUtil.formatCurrency(it) }
            )
            Spacer(modifier = Modifier.height(Spacing.lg))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.TrendingUp,
                                contentDescription = stringResource(R.string.common_income),
                                tint = MaterialTheme.colorScheme.financeColors.income,
                                modifier = Modifier.size(Spacing.iconT)
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(
                                text = stringResource(R.string.common_income),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = FormatterUtil.formatCurrency(totalIncome),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.financeColors.income
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.TrendingDown,
                                contentDescription = stringResource(R.string.common_expense),
                                tint = MaterialTheme.colorScheme.financeColors.expense,
                                modifier = Modifier.size(Spacing.iconT)
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(
                                text = stringResource(R.string.common_expense),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = FormatterUtil.formatCurrency(totalExpense),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.financeColors.expense
                        )
                    }
                }
            }
        }
    }

@Composable
private fun GamificationSummaryCard(
    progress: UserProgress,
    onUseFreeze: () -> Unit
) {
    val flameColor = when {
        progress.currentStreak >= 30 -> Color(0xFFFF6F00)
        progress.currentStreak >= 7 -> MaterialTheme.colorScheme.financeColors.accent
        progress.currentStreak >= 3 -> Color(0xFFFFA726)
        progress.currentStreak > 0 -> Color(0xFFEF5350)
        else -> Color(0xFFBDBDBD)
    }

    val levelColor = when {
        progress.currentLevel >= 9 -> Color(0xFFFF6F00)
        progress.currentLevel >= 7 -> MaterialTheme.colorScheme.tertiary
        progress.currentLevel >= 5 -> MaterialTheme.colorScheme.primary
        progress.currentLevel >= 3 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color = levelColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${progress.currentLevel}",
                        color = levelColor,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.sm))
                Column {
                    Text(
                        text = progress.levelTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${progress.totalXp} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )

            // Streak section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = flameColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Column {
                    Text(
                        text = "${progress.currentStreak} hari",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = flameColor
                    )
                    Text(
                        text = "Terbaik: ${progress.bestStreak}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Freeze section — only when available
            if (progress.streakFreezes > 0) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onUseFreeze,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AcUnit,
                            contentDescription = "Gunakan streak freeze",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "Freeze (${progress.streakFreezes})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = Spacing.sm)
    )
}

@Composable
private fun EmptyTransactionsState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Savings,
                contentDescription = stringResource(R.string.dashboard_no_transactions),
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = stringResource(R.string.dashboard_no_transactions),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = stringResource(R.string.dashboard_no_transactions_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TransactionItem(transaction: TransactionWithCategory) {
    val isIncome = transaction.transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) MaterialTheme.colorScheme.financeColors.income
        else MaterialTheme.colorScheme.financeColors.expense
    val prefix = if (isIncome) "+" else "-"
    val icon = FinanceIcons.getIcon(transaction.category.name)
    val iconColor = FinanceIcons.getColorFromHex(transaction.category.color)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(Spacing.iconMd)
                    .background(
                        color = iconColor.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.extraSmall
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = transaction.category.name,
                    tint = iconColor,
                    modifier = Modifier.size(Spacing.iconXxs)
                )
            }

            Spacer(modifier = Modifier.width(Spacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = transaction.transaction.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "$prefix${FormatterUtil.formatCurrency(transaction.transaction.amount)}",
                style = MaterialTheme.typography.titleSmall,
                color = amountColor
            )
        }
    }
}
