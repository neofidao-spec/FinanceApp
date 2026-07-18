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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.data.model.TransactionType
import com.financeapp.data.model.TransactionWithCategory
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
    // Loading state — shimmer skeleton
    if (uiState.isLoading) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
            contentPadding = PaddingValues(vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item { ShimmerBalanceCard() }
            item { ShimmerTransactionItem() }
            item { ShimmerTransactionItem() }
            item { ShimmerTransactionItem() }
        }
        return
    }

    // Error state
    if (uiState.errorMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.xl),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.TrendingDown,
                    contentDescription = "Terjadi kesalahan",
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
                        contentDescription = "Coba lagi",
                        modifier = Modifier.padding(end = Spacing.sm)
                    )
                    Text("Coba Lagi")
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
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

        // 2. Gamification Cards (Streak + Level)
        if (gamificationState.userProgress != null && !gamificationState.isLoading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    LevelCard(
                        progress = gamificationState.userProgress,
                        modifier = Modifier.weight(1.5f)
                    )
                    StreakCard(
                        currentStreak = gamificationState.userProgress.currentStreak,
                        bestStreak = gamificationState.userProgress.bestStreak,
                        streakFreezes = gamificationState.userProgress.streakFreezes,
                        onUseFreeze = onUseFreeze,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Health Score Card
        uiState.healthScore?.let { health ->
            item {
                HealthScoreCard(
                    score = health.score,
                    category = health.category,
                    description = health.description,
                    trend = health.trend
                )
            }
        }

        // 4. Daily Quests
        if (gamificationState.dailyQuests.isNotEmpty() && !gamificationState.isLoading) {
            item {
                DailyQuestCard(quests = gamificationState.dailyQuests)
            }
        }

        // 5. Income/Expense Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                IncomeExpenseCard(
                    modifier = Modifier.weight(1f),
                    label = "Pemasukan",
                    amount = uiState.totalIncome,
                    icon = Icons.Filled.ArrowUpward,
                    color = MaterialTheme.colorScheme.financeColors.income
                )
                IncomeExpenseCard(
                    modifier = Modifier.weight(1f),
                    label = "Pengeluaran",
                    amount = uiState.totalExpense,
                    icon = Icons.Filled.ArrowDownward,
                    color = MaterialTheme.colorScheme.financeColors.expense
                )
            }
        }

        // 6. DonutChart - Expense breakdown
        if (uiState.categoryBreakdown.isNotEmpty()) {
            item {
                SectionHeader(title = "Pengeluaran per Kategori")
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

        // 7. Monthly Trend Chart
        if (uiState.monthlyTrend.isNotEmpty()) {
            item {
                SectionHeader(title = "Tren Bulanan")
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

        // 8. Budget Overview
        if (uiState.budgetSummaries.isNotEmpty()) {
            item {
                SectionHeader(title = "Budget Overview")
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    uiState.budgetSummaries.take(3).forEach { budget ->
                        BudgetProgressRing(
                            progress = (budget.percentage / 100f).coerceIn(0f, 1f),
                            label = budget.category.name,
                            amountText = FormatterUtil.formatCurrency(budget.currentSpent),
                            ringSize = 80,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 9. Recent Transactions
        item {
            SectionHeader(title = "Transaksi Terbaru")
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(Spacing.lg)
        ) {
            Column {
                Text(
                    text = "Total Saldo",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                AnimatedNumber(
                    value = balance,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
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
                                contentDescription = "Pemasukan",
                                tint = MaterialTheme.colorScheme.financeColors.income,
                                modifier = Modifier.size(Spacing.iconT)
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(
                                text = "Pemasukan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
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
                                contentDescription = "Pengeluaran",
                                tint = MaterialTheme.colorScheme.financeColors.expense,
                                modifier = Modifier.size(Spacing.iconT)
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(
                                text = "Pengeluaran",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
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
}

@Composable
private fun IncomeExpenseCard(
    modifier: Modifier = Modifier,
    label: String,
    amount: Double,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(Spacing.iconSm)
                        .background(
                            color = color.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            AnimatedNumber(
                value = amount,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                format = { FormatterUtil.formatCurrency(it) }
            )
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                contentDescription = "Belum ada transaksi",
                modifier = Modifier.size(Spacing.iconLg),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "Belum ada transaksi",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = "Mulai tambahkan transaksi pertamamu!",
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
