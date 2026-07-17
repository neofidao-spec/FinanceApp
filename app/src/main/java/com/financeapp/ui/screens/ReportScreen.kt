package com.financeapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.ui.utils.FormatterUtil
import com.financeapp.ui.viewmodel.ReportViewModel

@Composable
fun ReportScreen(viewModel: ReportViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        CircularProgressIndicator(modifier = Modifier.padding(32.dp))
        return
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Bulan Sebelumnya")
                }
                Text(
                    uiState.currentMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Bulan Berikutnya")
                }
            }
        }

        item {
            val report = uiState.monthlyReport
            if (report != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ringkasan ${report.month}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pemasukan", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    FormatterUtil.formatCurrency(report.income),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pengeluaran", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    FormatterUtil.formatCurrency(report.expense),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF44336)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Saldo", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            FormatterUtil.formatCurrency(report.balance),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = if (report.balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
            } else {
                Text("Tidak ada data untuk bulan ini", color = Color.Gray)
            }
        }

        item {
            val report = uiState.monthlyReport
            if (report != null && report.categoryBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Pengeluaran per Kategori", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (uiState.monthlyReport?.categoryBreakdown?.isNotEmpty() == true) {
            items(uiState.monthlyReport!!.categoryBreakdown) { summary ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(summary.category.icon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(summary.category.name, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = (summary.percentage / 100f).coerceIn(0f, 1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = try {
                                    Color(android.graphics.Color.parseColor(summary.category.color))
                                } catch (e: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                FormatterUtil.formatCurrency(summary.total),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${String.format("%.1f", summary.percentage)}%",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
