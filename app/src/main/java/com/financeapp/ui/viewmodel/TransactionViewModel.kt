package com.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import android.util.Log
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class TransactionUiState(
    val transactions: List<TransactionWithCategory> = emptyList(),
    val filteredTransactions: List<TransactionWithCategory> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedTransaction: Transaction? = null,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val selectedFilter: TransactionType? = null,
    val searchQuery: String = "",
    val showFilterDialog: Boolean = false,
    val activeFilter: TransactionFilter? = null,
    val PAGE_SIZE: Int = 50,
    val visibleCount: Int = 50,
    val hasMore: Boolean = true,
    val deletedTransactionId: Long? = null,
    val deletedTransaction: TransactionWithCategory? = null
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    companion object {
        private const val TAG = "TransactionVM"
    }

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()
    private val _searchFlow = MutableStateFlow("")
    private var filterJob: Job? = null

    init {
        loadTransactions()
        loadCategories()
        // Search debounce: apply FTS search only after 300ms idle
        viewModelScope.launch {
            _searchFlow.debounce(300).collectLatest { query ->
                if (query.isNotBlank()) {
                    // Use FTS4 for full-text search
                    try {
                        transactionRepository.searchTransactions(query).collectLatest { results ->
                            val state = _uiState.value
                            _uiState.value = state.copy(filteredTransactions = results)
                            applyFiltersFromFts(results)
                        }
                    } catch (e: Exception) {
                        Log.w("TransactionVM", "FTS search failed, falling back", e)
                        applyFilters()
                    }
                } else {
                    applyFilters()
                }
            }
        }
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
                Log.e(TAG, "Failed to load transactions", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Gagal memuat transaksi. Silakan coba lagi.",
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
                Log.e(TAG, "Failed to load categories", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Gagal memuat kategori. Silakan coba lagi.")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, selectedFilter = null)
        _searchFlow.value = query
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
        _uiState.value = _uiState.value.copy(
            activeFilter = null,
            selectedFilter = null
        )
        applyFilters()
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return
        _uiState.value = state.copy(isLoadingMore = true)
        // Simulate async load for visibility — triggers recomposition
        viewModelScope.launch {
            val newCount = minOf(state.visibleCount + state.PAGE_SIZE, state.filteredTransactions.size)
            _uiState.value = _uiState.value.copy(
                visibleCount = newCount,
                hasMore = newCount < state.filteredTransactions.size,
                isLoadingMore = false
            )
        }
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

        // Apply search filter (client-side fallback)
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

        _uiState.value = state.copy(
            filteredTransactions = result,
            visibleCount = minOf(state.PAGE_SIZE, result.size),
            hasMore = result.size > state.PAGE_SIZE
        )
    }

    /**
     * Apply type/date/amount filters on FTS results (search already done by FTS4).
     */
    private fun applyFiltersFromFts(ftsResults: List<TransactionWithCategory>) {
        val state = _uiState.value
        var result = ftsResults

        state.activeFilter?.type?.let { type ->
            result = result.filter { it.transaction.type == type }
        }
        state.activeFilter?.startDate?.let { start ->
            result = result.filter { it.transaction.date >= start }
        }
        state.activeFilter?.endDate?.let { end ->
            result = result.filter { it.transaction.date <= end }
        }
        state.activeFilter?.minAmount?.let { min ->
            result = result.filter { it.transaction.amount >= min }
        }
        state.activeFilter?.maxAmount?.let { max ->
            result = result.filter { it.transaction.amount <= max }
        }

        _uiState.value = state.copy(
            filteredTransactions = result,
            visibleCount = minOf(state.PAGE_SIZE, result.size),
            hasMore = result.size > state.PAGE_SIZE
        )
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
                Log.e(TAG, "Failed to add transaction", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Gagal menyimpan transaksi. Silakan coba lagi.")
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
                Log.e(TAG, "Failed to update transaction", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Gagal memperbarui transaksi. Silakan coba lagi.")
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
                Log.e(TAG, "Failed to delete transaction", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Gagal menghapus transaksi. Silakan coba lagi.")
            }
        }
    }

    private var undoTimerJob: Job? = null

    /** Swipe-to-delete: save for undo, then remove from DB */
    fun swipeDeleteTransaction(txn: TransactionWithCategory) {
        viewModelScope.launch {
            try {
                // Cancel any previous undo timer to prevent stomping
                undoTimerJob?.cancel()

                val state = _uiState.value
                _uiState.value = state.copy(
                    deletedTransactionId = txn.transaction.id,
                    deletedTransaction = txn
                )
                transactionRepository.deleteTransaction(txn.transaction)
                // Auto-clear undo state after 4 seconds
                undoTimerJob = coroutineContext[Job]
                kotlinx.coroutines.delay(4000)
                if (undoTimerJob?.isActive == true) {
                    _uiState.value = _uiState.value.copy(
                        deletedTransactionId = null,
                        deletedTransaction = null
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to swipe-delete transaction", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Gagal menghapus transaksi. Silakan coba lagi.")
            }
        }
    }

    /** Undo swipe delete — re-insert the saved transaction */
    fun undoDelete() {
        val txn = _uiState.value.deletedTransaction ?: return
        viewModelScope.launch {
            try {
                transactionRepository.addTransaction(txn.transaction)
                _uiState.value = _uiState.value.copy(
                    deletedTransactionId = null,
                    deletedTransaction = null,
                    successMessage = "Penghapusan dibatalkan"
                )
                clearMessages()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to undo delete", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Gagal memulihkan transaksi. Silakan coba lagi.")
            }
        }
    }

    fun filterByType(type: TransactionType?) {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
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
                Log.e(TAG, "Failed to filter transactions by type", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Gagal memuat transaksi. Silakan coba lagi.")
            }
        }
    }

    fun selectTransaction(transaction: Transaction) {
        _uiState.value = _uiState.value.copy(selectedTransaction = transaction)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedTransaction = null)
    }

    fun retry() {
        _uiState.value = TransactionUiState(isLoading = true)
        loadTransactions()
        loadCategories()
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
