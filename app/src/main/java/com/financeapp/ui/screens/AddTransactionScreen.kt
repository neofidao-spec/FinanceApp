package com.financeapp.ui.screens

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financeapp.data.model.TransactionType
import com.financeapp.ui.components.AmountInput
import com.financeapp.ui.components.CategorySelector
import com.financeapp.ui.components.DatePickerField
import com.financeapp.ui.viewmodel.AddTransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.successMessage?.let {
        androidx.compose.runtime.LaunchedEffect(it) {
            snackbarHostState.showSnackbar(it)
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
            Text("Tipe Transaksi", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TransactionType.values().forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = uiState.transactionType == type,
                        onClick = { viewModel.switchTransactionType(type) },
                        label = { Text(if (type == TransactionType.INCOME) "Pemasukan" else "Pengeluaran") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AmountInput(
                value = uiState.amount,
                onValueChange = { viewModel.updateAmount(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Keterangan") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                minLines = 2,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            DatePickerField(
                value = uiState.selectedDate,
                onDateSelected = { viewModel.updateDate(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            CategorySelector(
                categories = uiState.categories.filter { it.type == uiState.transactionType },
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            uiState.errorMessage?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(bottom = 12.dp))
            }

            Button(
                onClick = { viewModel.submitTransaction() },
                enabled = uiState.isFormValid && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.transactionType == TransactionType.INCOME) Color.Green else Color.Red,
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text(if (uiState.isLoading) "Loading..." else "Simpan Transaksi", color = Color.White)
            }
        }
    }
}
