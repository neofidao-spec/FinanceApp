package com.financeapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.platform.LocalFocusManager
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.data.model.BudgetWithCategory
import com.financeapp.data.model.TransactionType
import com.financeapp.ui.utils.FinanceIcons
import com.financeapp.ui.utils.FormatterUtil
import com.financeapp.ui.viewmodel.BudgetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(viewModel: BudgetViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

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
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah Budget")
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header with month selector
            item {
                MonthSelector(
                    selectedMonth = uiState.selectedMonth,
                    onPrevious = { viewModel.previousMonth() },
                    onNext = { viewModel.nextMonth() }
                )
            }

            // Summary Card
            item {
                uiState.budgetSummary?.let { summary ->
                    BudgetSummaryCard(summary = summary)
                }
            }

            // Quick Stats
            item {
                uiState.budgetSummary?.let { summary ->
                    QuickStatsRow(summary = summary)
                }
            }

            // Budget List
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Budget Saya",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val budgets = uiState.budgetSummary?.budgets ?: emptyList()
            if (budgets.isEmpty()) {
                item {
                    EmptyBudgetState()
                }
            } else {
                items(budgets) { budget ->
                    BudgetItem(
                        budget = budget,
                        onDelete = { viewModel.deleteBudget(budget) }
                    )
                }
            }
        }
    }

    // Add Budget Dialog
    if (uiState.showAddDialog) {
        AddBudgetDialog(viewModel = viewModel)
    }
}

@Composable
private fun MonthSelector(
    selectedMonth: java.time.YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.Filled.KeyboardArrowLeft,
                contentDescription = "Bulan Sebelumnya",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = selectedMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNext) {
            Icon(
                Icons.Filled.KeyboardArrowRight,
                contentDescription = "Bulan Berikutnya",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BudgetSummaryCard(summary: com.financeapp.data.model.BudgetSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    text = "Total Budget",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = FormatterUtil.formatCurrency(summary.totalBudget),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Terpakai",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = FormatterUtil.formatCurrency(summary.totalSpent),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Sisa",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = FormatterUtil.formatCurrency(summary.totalBudget - summary.totalSpent),
                            color = if (summary.totalBudget - summary.totalSpent >= 0) Color(0xFF81C784) else Color(0xFFEF9A9A),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress bar
                val progress = if (summary.totalBudget > 0) {
                    (summary.totalSpent / summary.totalBudget).toFloat().coerceIn(0f, 1f)
                } else 0f
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${String.format("%.0f", progress * 100)}% terpakai",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun QuickStatsRow(summary: com.financeapp.data.model.BudgetSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Budget count
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Budget",
            value = "${summary.budgets.size}",
            icon = Icons.Filled.Savings,
            color = MaterialTheme.colorScheme.primaryContainer
        )
        
        // Over budget count
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Over Budget",
            value = "${summary.exceedingBudgets.size}",
            icon = Icons.Filled.Warning,
            color = if (summary.exceedingBudgets.isNotEmpty()) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surfaceVariant
        )
        
        // Health
        val healthIcon = when {
            summary.budgetHealth > 70 -> Icons.Filled.CheckCircle
            summary.budgetHealth > 40 -> Icons.Filled.Warning
            else -> Icons.Filled.Error
        }
        
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Kesehatan",
            value = "${String.format("%.0f", summary.budgetHealth)}%",
            icon = healthIcon,
            color = when {
                summary.budgetHealth > 70 -> Color(0xFFE8F5E9)
                summary.budgetHealth > 40 -> Color(0xFFFFF8E1)
                else -> Color(0xFFFFEBEE)
            }
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BudgetItem(
    budget: BudgetWithCategory,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val icon = FinanceIcons.getIcon(budget.category.name)
    val iconColor = FinanceIcons.getColorFromHex(budget.category.color)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = iconColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = budget.category.name,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = budget.category.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    if (budget.budget.description.isNotEmpty()) {
                        Text(
                            text = budget.budget.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Amount info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Terpakai: ${FormatterUtil.formatCurrency(budget.currentSpent)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Batas: ${FormatterUtil.formatCurrency(budget.budget.monthlyLimit)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar
            val animatedProgress by animateFloatAsState(
                targetValue = budget.percentage / 100f,
                animationSpec = tween(durationMillis = 800),
                label = "budget_progress"
            )
            
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    budget.isExceeded() -> Color(0xFFF44336)
                    budget.isAlertThreshold() -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when {
                            budget.isExceeded() -> Icons.Filled.Error
                            budget.isAlertThreshold() -> Icons.Filled.Warning
                            else -> Icons.Filled.CheckCircle
                        },
                        contentDescription = when {
                            budget.isExceeded() -> "Melebihi batas"
                            budget.isAlertThreshold() -> "Mendekati batas"
                            else -> "Aman"
                        },
                        modifier = Modifier.size(14.dp),
                        tint = when {
                            budget.isExceeded() -> Color(0xFFF44336)
                            budget.isAlertThreshold() -> Color(0xFFFF9800)
                            else -> Color(0xFF4CAF50)
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            budget.isExceeded() -> "Melebihi batas!"
                            budget.isAlertThreshold() -> "Mendekati batas"
                            else -> "Aman"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            budget.isExceeded() -> Color(0xFFF44336)
                            budget.isAlertThreshold() -> Color(0xFFFF9800)
                            else -> Color(0xFF4CAF50)
                        }
                    )
                }
                Text(
                    text = "Sisa: ${FormatterUtil.formatCurrency(budget.remaining)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Budget") },
            text = { Text("Yakin ingin menghapus budget untuk ${budget.category.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
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

@Composable
private fun EmptyBudgetState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Savings,
            contentDescription = "Belum ada budget",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Belum Ada Budget",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Buat budget untuk mengontrol pengeluaranmu",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBudgetDialog(viewModel: BudgetViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    
    val expenseCategories = uiState.categories.filter {
        it.type == TransactionType.EXPENSE
    }

    AlertDialog(
        onDismissRequest = { viewModel.hideAddDialog() },
        title = { Text("Tambah Budget", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    val selectedCategory = expenseCategories.find { it.id == uiState.addCategoryId }
                    OutlinedTextField(
                        value = selectedCategory?.let { "${it.name}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        expenseCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    viewModel.updateAddCategoryId(category.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.addMonthlyLimit,
                    onValueChange = { viewModel.updateAddMonthlyLimit(it) },
                    label = { Text("Batas Bulanan (Rp)") },
                    prefix = { Text("Rp ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.addDescription,
                    onValueChange = { viewModel.updateAddDescription(it) },
                    label = { Text("Deskripsi (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.addAlertThreshold,
                    onValueChange = { viewModel.updateAddAlertThreshold(it) },
                    label = { Text("Alert Threshold (%)") },
                    suffix = { Text("%") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { focusManager.clearFocus(); viewModel.addBudget() },
                shape = RoundedCornerShape(12.dp)
            ) {
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
