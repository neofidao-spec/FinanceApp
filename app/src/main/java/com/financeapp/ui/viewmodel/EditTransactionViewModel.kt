package com.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.model.Category
import com.financeapp.data.model.Transaction
import com.financeapp.data.model.TransactionType
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
    val isLoading: Boolean = true,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isFormValid: Boolean = false,
    val showDeleteConfirm: Boolean = false
)

@HiltViewModel
class EditTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditTransactionUiState())
    val uiState: StateFlow<EditTransactionUiState> = _uiState.asStateFlow()

    fun loadTransaction(transactionId: Long) {
        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getTransaction(transactionId)
                if (transaction != null) {
                    val category = categoryRepository.getCategory(transaction.categoryId)
                    loadCategories()
                    _uiState.value = _uiState.value.copy(
                        transactionId = transaction.id,
                        amount = transaction.amount.toString(),
                        description = transaction.description,
                        selectedDate = transaction.date,
                        selectedCategory = category,
                        transactionType = transaction.type,
                        isLoading = false
                    )
                    validateForm()
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

    private fun validateForm() {
        val state = _uiState.value
        val isValid = state.amount.isNotEmpty() &&
                state.amount.toDoubleOrNull() != null &&
                state.amount.toDouble() > 0 &&
                state.selectedCategory != null &&
                state.description.isNotEmpty()

        _uiState.value = _uiState.value.copy(isFormValid = isValid)
    }

    fun updateTransaction() {
        if (!_uiState.value.isFormValid) return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val transaction = Transaction(
                    id = _uiState.value.transactionId,
                    amount = _uiState.value.amount.toDouble(),
                    type = _uiState.value.transactionType,
                    categoryId = _uiState.value.selectedCategory!!.id,
                    description = _uiState.value.description,
                    date = _uiState.value.selectedDate
                )

                transactionRepository.updateTransaction(transaction)

                _uiState.value = _uiState.value.copy(
                    successMessage = "Transaksi berhasil diperbarui",
                    isLoading = false
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

    fun showDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = true)
    }

    fun hideDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = false)
    }

    fun deleteTransaction() {
        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    id = _uiState.value.transactionId,
                    amount = _uiState.value.amount.toDouble(),
                    type = _uiState.value.transactionType,
                    categoryId = _uiState.value.selectedCategory!!.id,
                    description = _uiState.value.description,
                    date = _uiState.value.selectedDate
                )

                transactionRepository.deleteTransaction(transaction)

                _uiState.value = _uiState.value.copy(
                    successMessage = "Transaksi berhasil dihapus",
                    showDeleteConfirm = false
                )
                clearMessages()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
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
