package com.financeapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.ui.utils.FinanceIcons
import com.financeapp.ui.utils.FormatterUtil
import com.financeapp.ui.viewmodel.GamificationViewModel
import com.financeapp.ui.viewmodel.ReportViewModel
import com.financeapp.ui.theme.financeColors
import com.financeapp.ui.theme.Spacing
import androidx.compose.material3.CardDefaults

@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel(),
    gamificationViewModel: GamificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gamificationState by gamificationViewModel.uiState.collectAsState()

    // Complete "Buka Laporan" quest when user opens ReportScreen (once only, after quests loaded)
    LaunchedEffect(Unit) {
        while (gamificationState.dailyQuests.isEmpty() && gamificationState.isLoading) {
            kotlinx.coroutines.delay(100)
        }
        val reportQuest = gamificationState.dailyQuests.find {
            it.template.id == "buka_laporan" && !it.assignment.isCompleted
        }
        reportQuest?.let { gamificationViewModel.completeQuest(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
    LazyColumn(modifier = Modifier.padding(paddingValues).padding(Spacing.md)) {
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text("Ringkasan ${report.month}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(Spacing.md))
                        androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pemasukan", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(Spacing.xs))
                                Text(
                                    FormatterUtil.formatCurrency(report.income),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.financeColors.income
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pengeluaran", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(Spacing.xs))
                                Text(
                                    FormatterUtil.formatCurrency(report.expense),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.financeColors.expense
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(Spacing.md))
                        androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text("Saldo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
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
                            imageVector = Icons.Filled.Receipt,
                            contentDescription = "Tidak ada data",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text(
                            "Belum Ada Data",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            "Tidak ada transaksi di bulan ini",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Mulai Catat Transaksi")
                        }
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
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                LinearProgressIndicator(
                                    progress = (summary.percentage / 100f).coerceIn(0f, 1f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(Spacing.xs)),
                                    color = when {
                                        summary.percentage > 80 -> MaterialTheme.colorScheme.financeColors.expense
                                        summary.percentage > 50 -> MaterialTheme.colorScheme.financeColors.warning
                                        else -> MaterialTheme.colorScheme.financeColors.income
                                    }
                                )
                                Text(
                                    text = "${String.format("%.0f", summary.percentage)}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        summary.percentage > 80 -> MaterialTheme.colorScheme.financeColors.expense
                                        summary.percentage > 50 -> MaterialTheme.colorScheme.financeColors.warning
                                        else -> MaterialTheme.colorScheme.financeColors.income
                                    }
                                )
                            }
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
}
