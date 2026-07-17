package com.financeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.data.model.TransactionType
import com.financeapp.ui.utils.FormatterUtil
import com.financeapp.ui.viewmodel.BudgetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(viewModel: BudgetViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah Budget")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Month selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Bulan Sebelumnya")
                }
                Text(
                    uiState.selectedMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Bulan Berikutnya")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                return@Column
            }

            val summary = uiState.budgetSummary

            if (summary != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Budget", fontSize = 14.sp, color = Color.Gray)
                        Text(
                            FormatterUtil.formatCurrency(summary.totalBudget),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Terpakai: ${FormatterUtil.formatCurrency(summary.totalSpent)}", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = (summary.budgetHealth / 100f).coerceIn(0f, 1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when {
                                summary.budgetHealth > 50 -> Color(0xFF4CAF50)
                                summary.budgetHealth > 20 -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            }
                        )
                        Text(
                            "Sisa: ${String.format("%.0f", summary.budgetHealth)}%",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (summary.exceedingBudgets.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("\u26A0\uFE0F", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${summary.exceedingBudgets.size} budget terlampaui!",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (summary.budgets.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("\uD83D\uDCB0", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Belum ada budget", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Tap + untuk menambah budget", fontSize = 12.sp, color = Color.Gray)
                    }
                } else {
                    LazyColumn {
                        items(summary.budgets) { budgetWithCategory ->
                            BudgetItem(
                                budget = budgetWithCategory,
                                onDelete = { viewModel.deleteBudget(budgetWithCategory) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddBudgetDialog(viewModel = viewModel)
    }
}

@Composable
private fun BudgetItem(
    budget: com.financeapp.data.model.BudgetWithCategory,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(budget.category.icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(budget.category.name, fontWeight = FontWeight.Bold)
                        if (budget.budget.description.isNotEmpty()) {
                            Text(budget.budget.description, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text("Batas: ${FormatterUtil.formatCurrency(budget.budget.monthlyLimit)}", fontSize = 12.sp)
                Text("Terpakai: ${FormatterUtil.formatCurrency(budget.currentSpent)}", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = (budget.percentage / 100f).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    budget.isExceeded() -> Color(0xFFF44336)
                    budget.isAlertThreshold() -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                }
            )

            Text(
                when {
                    budget.isExceeded() -> "\u26A0\uFE0F Melebihi batas!"
                    budget.isAlertThreshold() -> "\u26A0\uFE0F Mendekati batas"
                    else -> "Sisa: ${FormatterUtil.formatCurrency(budget.remaining)}"
                },
                fontSize = 12.sp,
                color = when {
                    budget.isExceeded() -> Color(0xFFF44336)
                    budget.isAlertThreshold() -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                }
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Budget") },
            text = { Text("Yakin ingin menghapus budget untuk ${budget.category.name}?") },
            confirmButton = {
                Button(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBudgetDialog(viewModel: BudgetViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    val expenseCategories = uiState.categories.filter { it.type == TransactionType.EXPENSE }

    AlertDialog(
        onDismissRequest = { viewModel.hideAddDialog() },
        title = { Text("Tambah Budget") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    val selectedCategory = expenseCategories.find { it.id == uiState.addCategoryId }
                    OutlinedTextField(
                        value = selectedCategory?.let { "${it.icon} ${it.name}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        expenseCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text("${category.icon} ${category.name}") },
                                onClick = {
                                    viewModel.updateAddCategoryId(category.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.addMonthlyLimit,
                    onValueChange = { viewModel.updateAddMonthlyLimit(it) },
                    label = { Text("Batas Bulanan (Rp)") },
                    prefix = { Text("Rp ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.addDescription,
                    onValueChange = { viewModel.updateAddDescription(it) },
                    label = { Text("Deskripsi (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.addAlertThreshold,
                    onValueChange = { viewModel.updateAddAlertThreshold(it) },
                    label = { Text("Alert Threshold (%)") },
                    suffix = { Text("%") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.addBudget() }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.hideAddDialog() }) {
                Text("Batal")
            }
        }
    )
}
