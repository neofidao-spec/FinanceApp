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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.financeapp.ui.components.MonthlyData
import com.financeapp.ui.components.MonthlyTrendChart
import com.financeapp.ui.components.TierBadge
import com.financeapp.ui.components.XpSummaryDialog
import com.financeapp.ui.theme.Spacing
import com.financeapp.ui.theme.financeColors
import com.financeapp.ui.utils.FinanceIcons
import com.financeapp.ui.utils.TierUtils
import com.financeapp.domain.HealthScore
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

    // Complete "Cek Dashboard" quest on first visit (once only, after quests loaded)
    LaunchedEffect(Unit) {
        // Wait for quests to be loaded
        while (gamificationState.dailyQuests.isEmpty() && gamificationState.isLoading) {
            kotlinx.coroutines.delay(100)
        }
        val dashboardQuest = gamificationState.dailyQuests.find {
            it.template.id == "cek_dashboard" && !it.assignment.isCompleted
        }
        dashboardQuest?.let { gamificationViewModel.completeQuest(it) }
    }

    DashboardContent(
        uiState = uiState,
        gamificationState = gamificationState,
        onRetry = { viewModel.retry() }
    )
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    gamificationState: GamificationUiState,
    onRetry: () -> Unit,
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
                    progress = gamificationState.userProgress
                )
            }
        }

        // 3. Budget Overview + Health Score (combined horizontal row)
        item {
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Budget total ring (1 ring only)
                    val totalBudget = uiState.budgetSummaries.sumOf { it.budget.monthlyLimit }
                    val totalSpent = uiState.budgetSummaries.sumOf { it.currentSpent }
                    val budgetProgress = if (totalBudget > 0) {
                        (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f)
                    } else 0f

                    BudgetProgressRing(
                        progress = budgetProgress,
                        label = if (uiState.budgetSummaries.isNotEmpty()) "Budget" else "Belum ada",
                        amountText = if (uiState.budgetSummaries.isNotEmpty())
                            "${String.format("%.0f", budgetProgress * 100)}% terpakai"
                        else "Atur di Budget",
                        ringSize = 72,
                        modifier = Modifier.weight(1f)
                    )

                    // Vertical divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(72.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    )

                    // Right: Health score ring with explanation
                    uiState.healthScore?.let { health ->
                        HealthScoreRing(
                            score = health.score,
                            category = health.category,
                            description = health.description,
                            trend = health.trend,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 4. Daily Quests
        if (gamificationState.dailyQuests.isNotEmpty() && !gamificationState.isLoading) {
            item {
                DailyQuestCard(
                    quests = gamificationState.dailyQuests
                )
            }
        }

        // 5. Monthly Trend Chart
        if (uiState.monthlyTrend.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.md)
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_monthly_trend),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        MonthlyTrendChart(
                            data = uiState.monthlyTrend.map { trend ->
                                MonthlyData(
                                    month = trend.month,
                                    income = trend.income,
                                    expense = trend.expense
                                )
                            }
                        )
                    }
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
    category: String = "",
    description: String = "",
    trend: HealthScore.Trend = HealthScore.Trend.STABLE,
    modifier: Modifier = Modifier
) {
    val scoreColor = when {
        score >= 80 -> MaterialTheme.colorScheme.financeColors.income
        score >= 60 -> MaterialTheme.colorScheme.primary
        score >= 40 -> MaterialTheme.colorScheme.financeColors.warning
        else -> MaterialTheme.colorScheme.financeColors.expense
    }

    val trendIcon = when (trend) {
        HealthScore.Trend.UP -> Icons.Filled.TrendingUp
        HealthScore.Trend.DOWN -> Icons.Filled.TrendingDown
        else -> Icons.Filled.TrendingUp
    }

    val trendColor = when (trend) {
        HealthScore.Trend.UP -> MaterialTheme.colorScheme.financeColors.income
        HealthScore.Trend.DOWN -> MaterialTheme.colorScheme.financeColors.expense
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val animatedScore by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 100f),
        label = "healthRing"
    )

    Column(
        modifier = modifier.padding(Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            val trackColor = scoreColor.copy(alpha = 0.15f)
            Canvas(modifier = Modifier.size(56.dp)) {
                val strokeWidth = 5.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2

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
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xs))

        // Category + trend
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (category.isNotEmpty()) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Icon(
                    imageVector = trendIcon,
                    contentDescription = if (trend == HealthScore.Trend.UP) "Meningkat" else "Menurun",
                    tint = trendColor,
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Text(
                    text = "Kesehatan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Description (if present)
        if (description.isNotEmpty()) {
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
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
    progress: UserProgress
) {
    var showXpDialog by remember { mutableStateOf(false) }
    val tier = TierUtils.getTierForLevel(progress.currentLevel)

    val flameColor = when {
        progress.currentStreak >= 30 -> Color(0xFFFF6F00)
        progress.currentStreak >= 7 -> MaterialTheme.colorScheme.financeColors.accent
        progress.currentStreak >= 3 -> Color(0xFFFFA726)
        progress.currentStreak > 0 -> Color(0xFFEF5350)
        else -> Color(0xFFBDBDBD)
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
            // Level section — Tier badge (clickable)
            Row(verticalAlignment = Alignment.CenterVertically) {
                TierBadge(
                    tier = tier.tier,
                    size = 40.dp,
                    onClick = { showXpDialog = true }
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Column {
                    Text(
                        text = tier.name,
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
        }
    }

    // XP Summary Dialog
    if (showXpDialog) {
        XpSummaryDialog(
            totalXp = progress.totalXp,
            currentLevel = progress.currentLevel,
            onDismiss = { showXpDialog = false }
        )
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
