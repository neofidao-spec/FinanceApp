package com.financeapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.ui.utils.FinanceIcons
import com.financeapp.ui.utils.FormatterUtil
import com.financeapp.ui.viewmodel.ReportViewModel
import com.financeapp.ui.theme.financeColors
import com.financeapp.ui.theme.Spacing
import androidx.compose.material3.CardDefaults

@Composable
fun ReportScreen(viewModel: ReportViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(modifier = Modifier.padding(Spacing.md)) {
        // Month selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Bulan Sebelumnya")
                }
                Text(
                    uiState.currentMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Bulan Berikutnya")
                }
            }
        }

        // Loading state
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xxl),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            return@LazyColumn
        }

        // Error state
        if (uiState.errorMessage != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Kesalahan",
                            modifier = Modifier.size(Spacing.iconLg),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(Spacing.smd))
                        Text(
                            uiState.errorMessage ?: "Terjadi kesalahan",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
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
            }
            return@LazyColumn
        }

        // Report summary card
        item {
            val report = uiState.monthlyReport
            if (report != null) {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text("Ringkasan ${report.month}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(Spacing.smd))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pemasukan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    FormatterUtil.formatCurrency(report.income),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.financeColors.income
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pengeluaran", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    FormatterUtil.formatCurrency(report.expense),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.financeColors.expense
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.smd))
                        Text("Saldo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            FormatterUtil.formatCurrency(report.balance),
                            style = MaterialTheme.typography.titleLarge,
                            color = if (report.balance >= 0) MaterialTheme.colorScheme.financeColors.income else MaterialTheme.colorScheme.financeColors.expense
                        )
                    }
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Tidak ada data",
                            modifier = Modifier.size(Spacing.iconXl),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text(
                            "Belum Ada Data",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            "Tidak ada transaksi di bulan ini",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        item {
            val report = uiState.monthlyReport
            if (report != null && report.categoryBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.md))
                Text("Pengeluaran per Kategori", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(Spacing.sm))
            }
        }

        if (uiState.monthlyReport?.categoryBreakdown?.isNotEmpty() == true) {
            items(uiState.monthlyReport!!.categoryBreakdown) { summary ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.xs),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.smd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = FinanceIcons.getIcon(summary.category.name),
                            contentDescription = summary.category.name,
                            modifier = Modifier.size(Spacing.iconXs),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(Spacing.smd))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(summary.category.name, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            LinearProgressIndicator(
                                progress = (summary.percentage / 100f).coerceIn(0f, 1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(Spacing.xs)),
                                color = try {
                                    Color(android.graphics.Color.parseColor(summary.category.color))
                                } catch (e: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(Spacing.smd))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                FormatterUtil.formatCurrency(summary.total),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${String.format("%.1f", summary.percentage)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
