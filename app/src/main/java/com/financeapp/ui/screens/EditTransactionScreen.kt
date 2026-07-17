package com.financeapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financeapp.data.model.TransactionType
import com.financeapp.ui.components.AmountInput
import com.financeapp.ui.components.CategorySelector
import com.financeapp.ui.components.DatePickerField
import com.financeapp.ui.viewmodel.EditTransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    viewModel: EditTransactionViewModel,
    transactionId: Long,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadTransaction(transactionId)
    }

    uiState.successMessage?.let {
        LaunchedEffect(it) {
            snackbarHostState.showSnackbar(it)
            onNavigateBack()
        }
    }

    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirmation() },
            title = { Text("Hapus Transaksi") },
            text = { Text("Apakah Anda yakin ingin menghapus transaksi ini?") },
            confirmButton = {
                Button(onClick = { viewModel.deleteTransaction() }) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.hideDeleteConfirmation() }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Transaksi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showDeleteConfirmation() }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Hapus")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Transaction type (read-only)
            Text("Tipe: ${if (uiState.transactionType == TransactionType.EXPENSE) "Pengeluaran" else "Pemasukan"}", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            AmountInput(
                value = uiState.amount,
                onValueChange = { viewModel.updateAmount(it) },
                label = "Jumlah"
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Deskripsi") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true
            )

            val filteredCategories = uiState.categories.filter { it.type == uiState.transactionType }
            CategorySelector(
                categories = filteredCategories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            DatePickerField(
                value = uiState.selectedDate,
                onDateSelected = { viewModel.updateDate(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            uiState.errorMessage?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
            }

            Button(
                onClick = { viewModel.updateTransaction() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isFormValid && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    if (uiState.isLoading) "Menyimpan..." else "Simpan Perubahan",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
