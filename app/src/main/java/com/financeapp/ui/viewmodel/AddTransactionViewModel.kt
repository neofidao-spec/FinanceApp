package com.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
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

data class AddTransactionUiState(
    val amount: String = "",
    val description: String = "",
    val selectedDate: LocalDateTime = LocalDateTime.now(),
    val selectedCategory: Category? = null,
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val selectedAccountId: Long = 1,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isFormValid: Boolean = false
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadAccounts()
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

    private fun loadAccounts() {
        viewModelScope.launch {
            try {
                accountRepository.getAllAccounts().collect { accounts ->
                    val defaultId = accounts.firstOrNull { it.isDefault }?.id ?: accounts.firstOrNull()?.id ?: 1L
                    _uiState.value = _uiState.value.copy(
                        accounts = accounts,
                        selectedAccountId = _uiState.value.selectedAccountId.takeIf { id -> accounts.any { it.id == id } } ?: defaultId
                    )
                }
            } catch (e: Exception) {
                // Silent fail
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
        val categoryOfType = category.takeIf { it.type == _uiState.value.transactionType }
        if (categoryOfType != null) {
            _uiState.value = _uiState.value.copy(selectedCategory = categoryOfType)
            validateForm()
        }
    }

    fun selectAccount(accountId: Long) {
        _uiState.value = _uiState.value.copy(selectedAccountId = accountId)
    }

    fun switchTransactionType(type: TransactionType) {
        _uiState.value = _uiState.value.copy(
            transactionType = type,
            selectedCategory = null
        )
        validateForm()
    }

    private fun validateForm() {
        val state = _uiState.value
        val isValid = state.amount.isNotEmpty() &&
                state.amount.toDoubleOrNull() != null &&
                state.amount.toDouble() > 0 &&
                state.selectedCategory != null

        _uiState.value = _uiState.value.copy(isFormValid = isValid)
    }

    fun submitTransaction() {
        if (!_uiState.value.isFormValid) return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val transaction = Transaction(
                    amount = _uiState.value.amount.toDouble(),
                    type = _uiState.value.transactionType,
                    categoryId = _uiState.value.selectedCategory!!.id,
                    description = _uiState.value.description.ifEmpty { _uiState.value.selectedCategory?.name ?: "Transaksi" },
                    date = _uiState.value.selectedDate,
                    accountId = _uiState.value.selectedAccountId
                )

                transactionRepository.addTransaction(transaction)

                _uiState.value = _uiState.value.copy(
                    successMessage = "Transaksi berhasil ditambahkan",
                    isLoading = false,
                    amount = "",
                    description = "",
                    selectedCategory = null,
                    selectedDate = LocalDateTime.now()
                )
                clearMessages()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message,
                    isLoading = false
                )
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
