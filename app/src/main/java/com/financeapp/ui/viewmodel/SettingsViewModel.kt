package com.financeapp.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.preferences.AppPreferences
import com.financeapp.data.repository.AccountRepository
import com.financeapp.data.repository.TransactionRepository
import com.financeapp.util.CsvExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val transactionCount: Int = 0,
    val accountCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                combine(
                    appPreferences.isDarkMode,
                    transactionRepository.getAllTransactions(),
                    accountRepository.getAllAccounts()
                ) { dark, transactions, accounts ->
                    SettingsUiState(
                        isDarkMode = dark,
                        transactionCount = transactions.size,
                        accountCount = accounts.size,
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                appPreferences.setDarkMode(enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun exportTransactions(onExportReady: (Intent?) -> Unit) {
        viewModelScope.launch {
            try {
                val transactions = transactionRepository.getAllTransactions().first()
                if (transactions.isEmpty()) {
                    onExportReady(null)
                    return@launch
                }

                val csv = CsvExporter.exportTransactions(transactions)
                val file = File(context.cacheDir, "transactions_export.csv")
                file.writeText(csv)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Export Transaksi FinanceApp")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                onExportReady(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                onExportReady(null)
            }
        }
    }
}
