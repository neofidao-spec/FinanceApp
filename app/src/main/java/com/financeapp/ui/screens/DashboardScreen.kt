package com.financeapp.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.data.model.Category
import com.financeapp.data.model.CategorySummary
import com.financeapp.data.model.TransactionType
import com.financeapp.data.model.TransactionWithCategory
import com.financeapp.ui.components.AnimatedNumber
import com.financeapp.ui.components.BudgetProgressRing
import com.financeapp.ui.components.DonutChart
import com.financeapp.ui.components.DonutSegment
import com.financeapp.ui.components.MonthlyData
import com.financeapp.ui.components.MonthlyTrendChart
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
                Text(
                    text = "⚠️",
                    fontSize = 48.sp
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
                        imageVector = Icons.Default.Refresh,
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
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 1. Balance Card with AnimatedNumber
        item {
            BalanceCardWithAnimation(
                balance = uiState.balance,
                totalIncome = uiState.totalIncome,
                totalExpense = uiState.totalExpense
            )
        }

        // 2. Income and Expense cards side by side
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IncomeExpenseCard(
                    label = "Pemasukan",
                    amount = uiState.totalIncome,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                IncomeExpenseCard(
                    label = "Pengeluaran",
                    amount = uiState.totalExpense,
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Health Score
        uiState.healthScore?.let { health ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            health.score >= 80 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            health.score >= 60 -> Color(0xFF2196F3).copy(alpha = 0.1f)
                            health.score >= 40 -> Color(0xFFFF9800).copy(alpha = 0.1f)
                            else -> Color(0xFFF44336).copy(alpha = 0.1f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kesehatan Keuangan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = health.description,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${health.score}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    health.score >= 80 -> Color(0xFF4CAF50)
                                    health.score >= 60 -> Color(0xFF2196F3)
                                    health.score >= 40 -> Color(0xFFFF9800)
                                    else -> Color(0xFFF44336)
                                }
                            )
                            Text(
                                text = health.category,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 4. DonutChart - expense breakdown
        if (uiState.categoryBreakdown.isNotEmpty()) {
            item {
                SectionTitle(title = "Pengeluaran per Kategori")
                DonutChart(
                    segments = uiState.categoryBreakdown.map { summary ->
                        DonutSegment(
                            label = summary.category.name,
                            value = summary.total,
                            color = parseColor(summary.category.color)
                        )
                    },
                    centerText = FormatterUtil.formatCurrency(uiState.totalExpense),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // 4. MonthlyTrendChart
        if (uiState.monthlyTrend.isNotEmpty()) {
            item {
                SectionTitle(title = "Tren Bulanan")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
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

        // 5. Budget Overview section
        if (uiState.budgetSummaries.isNotEmpty()) {
            item {
                SectionTitle(title = "Budget Overview")
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(uiState.budgetSummaries) { budgetWithCategory ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            BudgetProgressRing(
                                progress = (budgetWithCategory.percentage / 100f).coerceIn(0f, 1f),
                                label = budgetWithCategory.category.name,
                                amountText = FormatterUtil.formatCurrency(budgetWithCategory.currentSpent),
                                ringSize = 80
                            )
                        }
                    }
                }
            }
        }

        // 6. Recent Transactions
        item {
            SectionTitle(title = "Transaksi Terbaru")
        }

        if (uiState.recentTransactions.isEmpty()) {
            item {
                EmptyTransactionsState()
            }
        } else {
            items(uiState.recentTransactions) { transaction ->
                TransactionItem(transaction = transaction)
            }
        }
    }
}

@Composable
private fun BalanceCardWithAnimation(
    balance: Double,
    totalIncome: Double,
    totalExpense: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Saldo Anda",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedNumber(
                value = balance,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                format = { FormatterUtil.formatCurrency(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Pemasukan",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = FormatterUtil.formatCurrency(totalIncome),
                        color = Color(0xFF81C784),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Pengeluaran",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = FormatterUtil.formatCurrency(totalExpense),
                        color = Color(0xFFEF9A9A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeExpenseCard(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedNumber(
                value = amount,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                format = { FormatterUtil.formatCurrency(it) }
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun EmptyTransactionsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "📊",
                fontSize = 48.sp
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
    val amountColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
    val prefix = if (isIncome) "+" else "-"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = transaction.category.icon,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.category.name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Text(
                text = transaction.transaction.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Text(
            text = "$prefix${FormatterUtil.formatCurrency(transaction.transaction.amount)}",
            fontWeight = FontWeight.Bold,
            color = amountColor,
            fontSize = 14.sp
        )
    }
    Divider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}

/**
 * Parse a hex color string to a Compose Color.
 */
private fun parseColor(hexColor: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hexColor))
    } catch (e: Exception) {
        Color.Gray
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardContentPreview() {
    MaterialTheme {
        DashboardContent(
            uiState = DashboardUiState(
                balance = 15000000.0,
                totalIncome = 20000000.0,
                totalExpense = 5000000.0,
                recentTransactions = emptyList(),
                topExpenses = emptyList(),
                categoryBreakdown = listOf(
                    CategorySummary(
                        category = Category(5, "Makanan", "🍔", TransactionType.EXPENSE, "#FF5722"),
                        total = 2000000.0,
                        percentage = 40f
                    ),
                    CategorySummary(
                        category = Category(6, "Transportasi", "🚗", TransactionType.EXPENSE, "#9C27B0"),
                        total = 1500000.0,
                        percentage = 30f
                    ),
                    CategorySummary(
                        category = Category(7, "Hiburan", "🎬", TransactionType.EXPENSE, "#E91E63"),
                        total = 1500000.0,
                        percentage = 30f
                    )
                ),
                monthlyTrend = listOf(
                    MonthlyTrendData("Jan", 5000000.0, 3500000.0),
                    MonthlyTrendData("Feb", 5500000.0, 4000000.0),
                    MonthlyTrendData("Mar", 4800000.0, 3200000.0),
                    MonthlyTrendData("Apr", 6000000.0, 4500000.0),
                    MonthlyTrendData("Mei", 5200000.0, 3800000.0),
                    MonthlyTrendData("Jun", 5800000.0, 4100000.0)
                ),
                spendingRate = 0.25f,
                isLoading = false
            ),
            onRetry = {}
        )
    }
}
