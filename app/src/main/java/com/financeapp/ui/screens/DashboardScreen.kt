package com.financeapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
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
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.data.model.CategorySummary
import com.financeapp.data.model.TransactionType
import com.financeapp.data.model.TransactionWithCategory
import com.financeapp.ui.components.AnimatedNumber
import com.financeapp.ui.components.BudgetProgressRing
import com.financeapp.ui.components.DonutChart
import com.financeapp.ui.components.DonutSegment
import com.financeapp.ui.components.MonthlyData
import com.financeapp.ui.components.MonthlyTrendChart
import com.financeapp.ui.theme.FinanceAppTheme
import com.financeapp.ui.utils.FinanceIcons
import com.financeapp.ui.utils.FormatterUtil
import com.financeapp.ui.viewmodel.DashboardUiState
import com.financeapp.ui.viewmodel.DashboardViewModel
import com.financeapp.ui.viewmodel.MonthlyTrendData

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    DashboardContent(
        uiState = uiState,
        onRetry = { viewModel.retry() }
    )
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onRetry: () -> Unit
) {
    // Loading state
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Error state
    if (uiState.errorMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.TrendingDown,
                    contentDescription = "Terjadi kesalahan",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Coba lagi",
                        modifier = Modifier.padding(end = 8.dp)
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
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Balance Card - Premium gradient design
        item {
            BalanceCard(
                balance = uiState.balance,
                totalIncome = uiState.totalIncome,
                totalExpense = uiState.totalExpense
            )
        }

        // 2. Health Score Card
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

        // 3. Income/Expense Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IncomeExpenseCard(
                    modifier = Modifier.weight(1f),
                    label = "Pemasukan",
                    amount = uiState.totalIncome,
                    icon = Icons.Filled.ArrowUpward,
                    color = Color(0xFF2E7D32)
                )
                IncomeExpenseCard(
                    modifier = Modifier.weight(1f),
                    label = "Pengeluaran",
                    amount = uiState.totalExpense,
                    icon = Icons.Filled.ArrowDownward,
                    color = Color(0xFFC62828)
                )
            }
        }

        // 4. DonutChart - Expense breakdown
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
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        // 5. Monthly Trend Chart
        if (uiState.monthlyTrend.isNotEmpty()) {
            item {
                SectionHeader(title = "Tren Bulanan")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
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
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // 6. Budget Overview
        if (uiState.budgetSummaries.isNotEmpty()) {
            item {
                SectionHeader(title = "Budget Overview")
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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

        // 7. Recent Transactions
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1a237e),
                            Color(0xFF283593),
                            Color(0xFF3949ab)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Total Saldo",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedNumber(
                    value = balance,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    format = { FormatterUtil.formatCurrency(it) }
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.TrendingUp,
                                contentDescription = "Pemasukan",
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Pemasukan",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = FormatterUtil.formatCurrency(totalIncome),
                            color = Color(0xFF81C784),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.TrendingDown,
                                contentDescription = "Pengeluaran",
                                tint = Color(0xFFEF9A9A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Pengeluaran",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = FormatterUtil.formatCurrency(totalExpense),
                            color = Color(0xFFEF9A9A),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthScoreCard(
    score: Int,
    category: String,
    description: String,
    trend: com.financeapp.domain.HealthScore.Trend
) {
    val scoreColor = when {
        score >= 80 -> Color(0xFF2E7D32)
        score >= 60 -> Color(0xFF1565C0)
        score >= 40 -> Color(0xFFE65100)
        else -> Color(0xFFC62828)
    }
    
    val trendIcon = when (trend) {
        com.financeapp.domain.HealthScore.Trend.UP -> Icons.Filled.TrendingUp
        com.financeapp.domain.HealthScore.Trend.DOWN -> Icons.Filled.TrendingDown
        else -> Icons.Filled.TrendingUp
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = scoreColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = scoreColor.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$score",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Kesehatan Keuangan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = if (trend == com.financeapp.domain.HealthScore.Trend.UP) "Meningkat" else "Menurun",
                        tint = if (trend == com.financeapp.domain.HealthScore.Trend.UP) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = category,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = scoreColor,
                modifier = Modifier
                    .background(
                        color = scoreColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun IncomeExpenseCard(
    modifier: Modifier = Modifier,
    label: String,
    amount: Double,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
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
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun EmptyTransactionsState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Savings,
                contentDescription = "Belum ada transaksi",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Belum ada transaksi",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
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
    val amountColor = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
    val prefix = if (isIncome) "+" else "-"
    val icon = FinanceIcons.getIcon(transaction.category.name)
    val iconColor = FinanceIcons.getColorFromHex(transaction.category.color)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = iconColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = transaction.category.name,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
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
                fontWeight = FontWeight.SemiBold,
                color = amountColor,
                fontSize = 14.sp
            )
        }
    }
}
