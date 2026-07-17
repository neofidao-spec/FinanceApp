package com.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.model.Category
import com.financeapp.data.model.Transaction
import com.financeapp.data.model.TransactionType
import com.financeapp.data.model.TransactionWithCategory
import com.financeapp.data.repository.CategoryRepository
import com.financeapp.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class TransactionUiState(
    val transactions: List<TransactionWithCategory> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedTransaction: Transaction? = null,
    val isLoading: Boolean = true,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val selectedFilter: TransactionType? = null
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
        loadCategories()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            try {
                transactionRepository.getAllTransactions().collect { transactions ->
                    _uiState.value = _uiState.value.copy(
                        transactions = transactions,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message,
                    isLoading = false
                )
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                categoryRepository.getAllCategories().collect { categories ->
                    _uiState.value = _uiState.value.copy(categories = categories)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun addTransaction(amount: Double, type: TransactionType, categoryId: Long, description: String, date: LocalDateTime) {
        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    description = description,
                    date = date
                )
                transactionRepository.addTransaction(transaction)
                _uiState.value = _uiState.value.copy(successMessage = "Transaksi berhasil ditambahkan")
                clearMessages()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionRepository.updateTransaction(transaction)
                _uiState.value = _uiState.value.copy(
                    successMessage = "Transaksi berhasil diperbarui",
                    selectedTransaction = null
                )
                clearMessages()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transaction)
                _uiState.value = _uiState.value.copy(successMessage = "Transaksi berhasil dihapus")
                clearMessages()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun filterByType(type: TransactionType?) {
        viewModelScope.launch {
            try {
                val filtered = if (type != null) {
                    transactionRepository.getTransactionsByType(type)
                } else {
                    transactionRepository.getAllTransactions()
                }

                filtered.collect { transactions ->
                    _uiState.value = _uiState.value.copy(
                        transactions = transactions,
                        selectedFilter = type
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun selectTransaction(transaction: Transaction) {
        _uiState.value = _uiState.value.copy(selectedTransaction = transaction)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedTransaction = null)
    }

    private fun clearMessages() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(
                successMessage = null,
                errorMessage = null
            )
        }
    }
}
