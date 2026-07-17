package com.financeapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financeapp.ui.components.BalanceCard
import com.financeapp.ui.utils.FormatterUtil
import com.financeapp.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        item {
            BalanceCard(uiState.balance)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Pemasukan", fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            FormatterUtil.formatCurrency(uiState.totalIncome),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Green
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Pengeluaran", fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            FormatterUtil.formatCurrency(uiState.totalExpense),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text("Pengeluaran Terbesar", fontWeight = FontWeight.Bold)
        }

        items(uiState.topExpenses) { expense ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(expense.category.icon, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(expense.category.name, fontWeight = FontWeight.Bold)
                    Text("${String.format("%.1f", expense.percentage)}%", fontSize = 12.sp)
                }
                Text(FormatterUtil.formatCurrency(expense.total), fontWeight = FontWeight.Bold)
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text("Transaksi Terbaru", fontWeight = FontWeight.Bold)
        }

        items(uiState.recentTransactions) { transaction ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(transaction.category.icon, fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(transaction.category.name, fontWeight = FontWeight.Bold)
                    Text(transaction.transaction.description, fontSize = 12.sp)
                }
                Text(
                    FormatterUtil.formatCurrency(transaction.transaction.amount),
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.transaction.type.name == "INCOME") Color.Green else Color.Red
                )
            }
        }
    }
}
