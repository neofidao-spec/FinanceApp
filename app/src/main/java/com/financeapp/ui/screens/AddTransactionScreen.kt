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
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

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
                title = { Text("Tambah Transaksi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Transaction type selector
            Text("Tipe Transaksi", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.transactionType == TransactionType.EXPENSE,
                    onClick = { viewModel.switchTransactionType(TransactionType.EXPENSE) },
                    label = { Text("Pengeluaran") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF44336).copy(alpha = 0.15f),
                        selectedLabelColor = Color(0xFFF44336)
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.transactionType == TransactionType.INCOME,
                    onClick = { viewModel.switchTransactionType(TransactionType.INCOME) },
                    label = { Text("Pemasukan") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4CAF50).copy(alpha = 0.15f),
                        selectedLabelColor = Color(0xFF4CAF50)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount input
            AmountInput(
                value = uiState.amount,
                onValueChange = { viewModel.updateAmount(it) },
                label = "Jumlah"
            )

            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Deskripsi") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true
            )

            // Category selector
            val filteredCategories = uiState.categories.filter { it.type == uiState.transactionType }
            CategorySelector(
                categories = filteredCategories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Date picker
            DatePickerField(
                value = uiState.selectedDate,
                onDateSelected = { viewModel.updateDate(it) }
            )

            // Account selector
            if (uiState.accounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AccountSelectorRow(
                    accounts = uiState.accounts,
                    selectedAccountId = uiState.selectedAccountId,
                    onAccountSelected = { viewModel.selectAccount(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error message
            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
            }

            // Save button
            HapticButton(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.submitTransaction()
                },
                enabled = uiState.isFormValid && !uiState.isLoading,
                text = if (uiState.isLoading) "Menyimpan..." else "Simpan"
            )
        }
    }
}
