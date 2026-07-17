package com.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.model.Category
import com.financeapp.data.model.Transaction
import com.financeapp.data.model.TransactionType
import com.financeapp.data.model.TransactionWithCategory
import com.financeapp.data.repository.CategoryRepository
import com.financeapp.data.repository.TransactionRepository
import com.financeapp.ui.components.TransactionFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class TransactionUiState(
    val transactions: List<TransactionWithCategory> = emptyList(),
    val filteredTransactions: List<TransactionWithCategory> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedTransaction: Transaction? = null,
    val isLoading: Boolean = true,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val selectedFilter: TransactionType? = null,
    val searchQuery: String = "",
    val showFilterDialog: Boolean = false,
    val activeFilter: TransactionFilter? = null
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
                transactionRepository.getAllTransactions().collectLatest { transactions ->
                    val state = _uiState.value
                    _uiState.value = state.copy(
                        transactions = transactions,
                        isLoading = false
                    )
                    applyFilters()
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
                categoryRepository.getAllCategories().collectLatest { categories ->
                    _uiState.value = _uiState.value.copy(categories = categories)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun applyFilter(filter: TransactionFilter) {
        val hasAnyFilter = filter.type != null || filter.startDate != null ||
                filter.endDate != null || filter.minAmount != null || filter.maxAmount != null
        _uiState.value = _uiState.value.copy(
            activeFilter = if (hasAnyFilter) filter else null
        )
        applyFilters()
    }

    fun clearFilter() {
        _uiState.value = _uiState.value.copy(activeFilter = null)
        applyFilters()
    }

    fun showFilterDialog() {
        _uiState.value = _uiState.value.copy(showFilterDialog = true)
    }

    fun hideFilterDialog() {
        _uiState.value = _uiState.value.copy(showFilterDialog = false)
    }

    private fun applyFilters() {
        val state = _uiState.value
        var result = state.transactions

        // Apply search filter
        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            result = result.filter { txn ->
                txn.transaction.description.lowercase().contains(query) ||
                        txn.category.name.lowercase().contains(query)
            }
        }

        // Apply type filter
        state.activeFilter?.type?.let { type ->
            result = result.filter { it.transaction.type == type }
        }

        // Apply date range filter
        state.activeFilter?.startDate?.let { start ->
            result = result.filter { it.transaction.date >= start }
        }
        state.activeFilter?.endDate?.let { end ->
            result = result.filter { it.transaction.date <= end }
        }

        // Apply amount range filter
        state.activeFilter?.minAmount?.let { min ->
            result = result.filter { it.transaction.amount >= min }
        }
        state.activeFilter?.maxAmount?.let { max ->
            result = result.filter { it.transaction.amount <= max }
        }

        _uiState.value = state.copy(filteredTransactions = result)
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

                filtered.collectLatest { transactions ->
                    _uiState.value = _uiState.value.copy(
                        transactions = transactions,
                        selectedFilter = type
                    )
                    applyFilters()
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
