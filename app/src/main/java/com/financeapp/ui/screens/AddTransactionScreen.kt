package com.financeapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.data.model.TransactionType
import com.financeapp.ui.components.AccountSelectorRow
import com.financeapp.ui.components.AmountInput
import com.financeapp.ui.components.CategorySelector
import com.financeapp.ui.components.DatePickerField
import com.financeapp.ui.components.HapticButton
import com.financeapp.ui.viewmodel.AddTransactionViewModel
import com.financeapp.ui.viewmodel.GamificationViewModel
import com.financeapp.ui.theme.financeColors
import com.financeapp.ui.theme.Spacing
import androidx.compose.ui.res.stringResource
import com.financeapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel = hiltViewModel(),
    gamificationViewModel: GamificationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val gamificationState by gamificationViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Update quest progress after successful transaction
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            gamificationViewModel.onTransactionRecorded()
            val txQuest = gamificationState.dailyQuests.find {
                it.template.id == "catat_transaksi" && !it.assignment.isCompleted
            }
            txQuest?.let { gamificationViewModel.completeQuest(it) }
        }
    }

    uiState.successMessage?.let {
        LaunchedEffect(it) {
            snackbarHostState.showSnackbar(it)
            kotlinx.coroutines.delay(1000)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_transaction_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(Spacing.md)
                .verticalScroll(rememberScrollState())
        ) {
            // Transaction type selector
            Text(stringResource(R.string.add_transaction_type), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                FilterChip(
                    selected = uiState.transactionType == TransactionType.EXPENSE,
                    onClick = { viewModel.switchTransactionType(TransactionType.EXPENSE) },
                    label = { Text(stringResource(R.string.common_expense)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.financeColors.expense.copy(alpha = 0.15f),
                        selectedLabelColor = MaterialTheme.colorScheme.financeColors.expense
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.transactionType == TransactionType.INCOME,
                    onClick = { viewModel.switchTransactionType(TransactionType.INCOME) },
                    label = { Text(stringResource(R.string.common_income)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.financeColors.income.copy(alpha = 0.15f),
                        selectedLabelColor = MaterialTheme.colorScheme.financeColors.income
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Amount input
            AmountInput(
                value = uiState.amount,
                onValueChange = { viewModel.updateAmount(it) },
                label = stringResource(R.string.common_amount)
            )

            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text(stringResource(R.string.common_description)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.sm),
                singleLine = true
            )

            // Category selector
            val filteredCategories = uiState.categories.filter { it.type == uiState.transactionType }
            CategorySelector(
                categories = filteredCategories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Date picker
            DatePickerField(
                value = uiState.selectedDate,
                onDateSelected = { viewModel.updateDate(it) }
            )

            // Account selector
            if (uiState.accounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                AccountSelectorRow(
                    accounts = uiState.accounts,
                    selectedAccountId = uiState.selectedAccountId,
                    onAccountSelected = { viewModel.selectAccount(it) }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Error message
            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = Spacing.sm))
            }

            // Save button
            HapticButton(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.submitTransaction()
                },
                enabled = uiState.isFormValid && !uiState.isLoading,
                text = if (uiState.isLoading) stringResource(R.string.common_saving) else stringResource(R.string.common_save)
            )
        }
    }
}
