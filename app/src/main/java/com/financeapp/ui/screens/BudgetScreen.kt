package com.financeapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.financeapp.ui.theme.MotionTokens
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.data.model.BudgetWithCategory
import com.financeapp.data.model.TransactionType
import com.financeapp.ui.utils.FinanceIcons
import com.financeapp.ui.utils.FormatterUtil
import com.financeapp.ui.viewmodel.BudgetViewModel
import com.financeapp.ui.viewmodel.GamificationViewModel
import com.financeapp.ui.theme.financeColors
import com.financeapp.ui.theme.Spacing
import androidx.compose.ui.res.stringResource
import com.financeapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel(),
    gamificationViewModel: GamificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Complete "Cek Budget" quest when user opens BudgetScreen (once only)
    LaunchedEffect(Unit) {
        gamificationViewModel.autoCompleteQuest("cek_budget")
    }

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
        // Show FAB only when there are budgets (not empty state)
        if (uiState.budgetSummary?.budgets?.isNotEmpty() == true) {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.budget_add_title))
                }
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

        // Error state
        if (uiState.errorMessage != null && uiState.budgetSummary == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = stringResource(R.string.common_error_occurred),
                        modifier = Modifier.size(Spacing.iconXl),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        text = uiState.errorMessage ?: stringResource(R.string.common_error_occurred),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Button(
                        onClick = { viewModel.retry() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.common_retry), modifier = Modifier.padding(end = Spacing.sm))
                        Text(stringResource(R.string.common_retry))
                    }
                }
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
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text = stringResource(R.string.budget_my_budgets),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = Spacing.md)
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
            }

            val budgets = uiState.budgetSummary?.budgets ?: emptyList()
            if (budgets.isEmpty()) {
                item {
                    EmptyBudgetState(onAdd = { viewModel.showAddDialog() })
                }
            } else {
                items(
                    items = budgets,
                    key = { it.budget.id }
                ) { budget ->
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
            .padding(horizontal = Spacing.md, vertical = Spacing.smd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.budget_previous_month),
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
                contentDescription = stringResource(R.string.budget_next_month),
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
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.budget_total),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = FormatterUtil.formatCurrency(summary.totalBudget),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.budget_used),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = FormatterUtil.formatCurrency(summary.totalSpent),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.budget_remaining_label),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = FormatterUtil.formatCurrency(summary.totalBudget - summary.totalSpent),
                            color = if (summary.totalBudget - summary.totalSpent >= 0) MaterialTheme.colorScheme.financeColors.income else MaterialTheme.colorScheme.financeColors.expense,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(Spacing.md))
                
                // Progress bar
                val progress = if (summary.totalBudget > 0) {
                    (summary.totalSpent / summary.totalBudget).toFloat().coerceIn(0f, 1f)
                } else 0f
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                    color = when {
                        progress > 0.8f -> MaterialTheme.colorScheme.financeColors.expense
                        progress > 0.5f -> MaterialTheme.colorScheme.financeColors.warning
                        else -> MaterialTheme.colorScheme.financeColors.income
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Spacer(modifier = Modifier.height(Spacing.sm))
                
                Text(
                    text = "${String.format("%.0f", progress * 100)}% terpakai",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
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
            .padding(horizontal = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.smd)
    ) {
        // Budget count
        StatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.budget_count),
            value = "${summary.budgets.size}",
            icon = Icons.Filled.Savings,
            color = MaterialTheme.colorScheme.surface
        )
        
        // Over budget count
        StatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.budget_over_budget),
            value = "${summary.exceedingBudgets.size}",
            icon = Icons.Filled.Warning,
            color = MaterialTheme.colorScheme.surface
        )
        
        // Health
        val healthIcon = when {
            summary.budgetHealth > 70 -> Icons.Filled.CheckCircle
            summary.budgetHealth > 40 -> Icons.Filled.Warning
            else -> Icons.Filled.Error
        }
        
        StatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.budget_health),
            value = "${String.format("%.0f", summary.budgetHealth)}%",
            icon = healthIcon,
            color = MaterialTheme.colorScheme.surface
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
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.smd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.budget_category_icon),
                modifier = Modifier.size(Spacing.iconXs),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
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
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
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
                            shape = MaterialTheme.shapes.small
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = budget.category.name,
                        tint = iconColor,
                        modifier = Modifier.size(Spacing.iconXs)
                    )
                }
                
                Spacer(modifier = Modifier.width(Spacing.smd))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = budget.category.name,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (budget.budget.description.isNotEmpty()) {
                        Text(
                            text = budget.budget.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.common_delete),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.smd))
            
            // Amount info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Terpakai: ${FormatterUtil.formatCurrency(budget.currentSpent)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Batas: ${FormatterUtil.formatCurrency(budget.budget.monthlyLimit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.sm))
            
            // Progress bar
            val animatedProgress by animateFloatAsState(
                targetValue = budget.percentage / 100f,
                animationSpec = tween(durationMillis = MotionTokens.LONG),
                label = "budget_progress"
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                    color = when {
                        animatedProgress > 0.8f -> MaterialTheme.colorScheme.financeColors.expense
                        animatedProgress > 0.5f -> MaterialTheme.colorScheme.financeColors.warning
                        else -> MaterialTheme.colorScheme.financeColors.income
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "${String.format("%.0f", budget.percentage)}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        animatedProgress > 0.8f -> MaterialTheme.colorScheme.financeColors.expense
                        animatedProgress > 0.5f -> MaterialTheme.colorScheme.financeColors.warning
                        else -> MaterialTheme.colorScheme.financeColors.income
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.sm))
            
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
                            budget.isExceeded() -> stringResource(R.string.budget_exceeded_cd)
                            budget.isAlertThreshold() -> stringResource(R.string.budget_approaching_limit)
                            else -> stringResource(R.string.budget_safe)
                        },
                        modifier = Modifier.size(Spacing.iconT),
                        tint = when {
                            budget.isExceeded() -> MaterialTheme.colorScheme.financeColors.expense
                            budget.isAlertThreshold() -> MaterialTheme.colorScheme.financeColors.warning
                            else -> MaterialTheme.colorScheme.financeColors.income
                        }
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = when {
                            budget.isExceeded() -> stringResource(R.string.budget_exceeded)
                            budget.isAlertThreshold() -> stringResource(R.string.budget_approaching_limit)
                            else -> stringResource(R.string.budget_safe)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = when {
                            budget.isExceeded() -> MaterialTheme.colorScheme.financeColors.expense
                            budget.isAlertThreshold() -> MaterialTheme.colorScheme.financeColors.warning
                            else -> MaterialTheme.colorScheme.financeColors.income
                        }
                    )
                }
                Text(
                    text = "Sisa: ${FormatterUtil.formatCurrency(budget.remaining)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.budget_delete_title)) },
            text = { Text("Yakin ingin menghapus budget untuk ${budget.category.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.financeColors.expense)
                ) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun EmptyBudgetState(onAdd: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Savings,
            contentDescription = stringResource(R.string.budget_no_budget_cd),
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = stringResource(R.string.budget_no_budget),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = stringResource(R.string.budget_no_budget_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Button(onClick = onAdd) {
            Text(stringResource(R.string.budget_create))
        }
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
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = { viewModel.hideAddDialog() },
        title = { Text(stringResource(R.string.budget_add_title), fontWeight = FontWeight.Bold) },
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
                        label = { Text(stringResource(R.string.budget_category)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = MaterialTheme.shapes.small
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

                Spacer(modifier = Modifier.height(Spacing.smd))

                OutlinedTextField(
                    value = uiState.addMonthlyLimit,
                    onValueChange = { viewModel.updateAddMonthlyLimit(it) },
                    label = { Text(stringResource(R.string.budget_monthly_limit)) },
                    prefix = { Text("Rp ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small
                )

                Spacer(modifier = Modifier.height(Spacing.smd))

                OutlinedTextField(
                    value = uiState.addDescription,
                    onValueChange = { viewModel.updateAddDescription(it) },
                    label = { Text(stringResource(R.string.budget_description_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small
                )

                Spacer(modifier = Modifier.height(Spacing.smd))

                OutlinedTextField(
                    value = uiState.addAlertThreshold,
                    onValueChange = { viewModel.updateAddAlertThreshold(it) },
                    label = { Text(stringResource(R.string.budget_alert_threshold)) },
                    suffix = { Text("%") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { focusManager.clearFocus(); viewModel.addBudget() },
                shape = MaterialTheme.shapes.small
            ) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.hideAddDialog() }) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
