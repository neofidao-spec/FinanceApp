package com.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.financeapp.data.model.Account
import com.financeapp.data.model.Category
import com.financeapp.data.model.Transaction
import com.financeapp.data.model.TransactionType
import com.financeapp.data.repository.AccountRepository
import com.financeapp.data.repository.CategoryRepository
import com.financeapp.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class EditTransactionUiState(
    val transactionId: Long = 0,
    val amount: String = "",
    val description: String = "",
    val selectedDate: LocalDateTime = LocalDateTime.now(),
    val selectedCategory: Category? = null,
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val selectedAccountId: Long = 1,
    val isLoading: Boolean = true,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isFormValid: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val accountName: String = ""
)

@HiltViewModel
class EditTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {
    companion object {
        private const val TAG = "EditTransactionVM"
    }

    private val _uiState = MutableStateFlow(EditTransactionUiState())
    val uiState: StateFlow<EditTransactionUiState> = _uiState.asStateFlow()

    fun loadTransaction(transactionId: Long) {
        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getTransaction(transactionId)
                if (transaction != null) {
                    val category = categoryRepository.getCategory(transaction.categoryId)
                    val account = accountRepository.getAccountById(transaction.accountId)
                    loadCategories()
                    loadAccounts()
                    _uiState.value = _uiState.value.copy(
                        transactionId = transaction.id,
                        amount = transaction.amount.toString(),
                        description = transaction.description,
                        selectedDate = transaction.date,
                        selectedCategory = category,
                        transactionType = transaction.type,
                        selectedAccountId = transaction.accountId,
                        accountName = account?.let { "${it.icon} ${it.name}" } ?: "Unknown",
                        isLoading = false
                    )
                    validateForm()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load transaction details", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Gagal memuat data transaksi. Silakan coba lagi.",
                    isLoading = false
                )
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = categoryRepository.getAllCategoriesOnce()
                    _uiState.value = _uiState.value.copy(categories = categories)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load categories", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Gagal memuat kategori. Silakan coba lagi.")
            }
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            try {
                val accounts = accountRepository.getAllAccountsOnce()
                    val defaultId = accounts.firstOrNull { it.isDefault }?.id
                        ?: accounts.firstOrNull()?.id ?: 1L
                    _uiState.value = _uiState.value.copy(
                        accounts = accounts,
                        selectedAccountId = _uiState.value.selectedAccountId
                            .takeIf { id -> accounts.any { it.id == id } } ?: defaultId
                    )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load accounts", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Gagal memuat akun. Silakan coba lagi.")
            }
        }
    }

    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
        validateForm()
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
        validateForm()
    }

    fun updateDate(date: LocalDateTime) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    fun selectCategory(category: Category) {
        if (category.type == _uiState.value.transactionType) {
            _uiState.value = _uiState.value.copy(selectedCategory = category)
            validateForm()
        }
    }

    fun selectAccount(accountId: Long) {
        _uiState.value = _uiState.value.copy(selectedAccountId = accountId)
    }

    private fun validateForm() {
        val state = _uiState.value
        val isValid = state.amount.isNotEmpty() &&
                state.amount.toDoubleOrNull() != null &&
                state.amount.toDouble() > 0 &&
                state.selectedCategory != null

        _uiState.value = _uiState.value.copy(isFormValid = isValid)
    }

    fun updateTransaction() {
        if (!_uiState.value.isFormValid) return
        val selectedCategory = _uiState.value.selectedCategory ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val transactionId = _uiState.value.transactionId

                // Read old transaction BEFORE updating, for correct balance revert
                val oldTransaction = transactionRepository.getTransaction(transactionId)

                val transaction = Transaction(
                    id = transactionId,
                    amount = _uiState.value.amount.toDouble(),
                    type = _uiState.value.transactionType,
                    categoryId = selectedCategory.id,
                    description = _uiState.value.description,
                    date = _uiState.value.selectedDate,
                    accountId = _uiState.value.selectedAccountId
                )

                transactionRepository.updateTransaction(transaction)

                // Update account balance for edited transaction
                try {
                    val account = accountRepository.getAccountById(_uiState.value.selectedAccountId)
                    account?.let { acc ->
                        // Revert old transaction effect on balance
                        val revertedBalance = when (oldTransaction?.type) {
                            TransactionType.INCOME -> acc.balance - (oldTransaction?.amount ?: 0.0)
                            TransactionType.EXPENSE -> acc.balance + (oldTransaction?.amount ?: 0.0)
                            else -> acc.balance
                        }
                        // Apply new transaction effect on balance
                        val newBalance = when (transaction.type) {
                            TransactionType.INCOME -> revertedBalance + transaction.amount
                            TransactionType.EXPENSE -> revertedBalance - transaction.amount
                        }
                        accountRepository.updateAccount(acc.copy(balance = newBalance))
                    }
                } catch (_: Exception) {
                    // Balance update failure is non-blocking
                }

                _uiState.value = _uiState.value.copy(
                    successMessage = "Transaksi berhasil diperbarui",
                    isLoading = false
                )
                clearMessages()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update transaction", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Gagal memperbarui transaksi. Silakan coba lagi.",
                    isLoading = false
                )
            }
        }
    }

    fun showDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = true)
    }

    fun hideDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = false)
    }

    fun deleteTransaction() {
        viewModelScope.launch {
            try {
                // Room only needs primary key for delete — avoid amount parsing
                val id = _uiState.value.transactionId
                if (id <= 0) {
                    _uiState.value = _uiState.value.copy(errorMessage = "ID transaksi tidak valid")
                    return@launch
                }
                transactionRepository.deleteTransactionById(id)

                _uiState.value = _uiState.value.copy(
                    successMessage = "Transaksi berhasil dihapus",
                    showDeleteConfirm = false
                )
                clearMessages()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete transaction", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Gagal menghapus transaksi. Silakan coba lagi.")
            }
        }
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
