package com.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.model.Budget
import com.financeapp.data.model.BudgetSummary
import com.financeapp.data.model.BudgetWithCategory
import com.financeapp.data.model.Category
import com.financeapp.data.repository.BudgetRepository
import com.financeapp.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class BudgetUiState(
    val budgetSummary: BudgetSummary? = null,
    val categories: List<Category> = emptyList(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val editingBudget: BudgetWithCategory? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    // Add form state
    val addCategoryId: Long? = null,
    val addMonthlyLimit: String = "",
    val addDescription: String = "",
    val addAlertThreshold: String = "80"
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // Load categories
                categoryRepository.getAllCategories().collect { categories ->
                    _uiState.value = _uiState.value.copy(categories = categories)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }

        loadBudgets()
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val summary = budgetRepository.getBudgetSummary(_uiState.value.selectedMonth)
                _uiState.value = _uiState.value.copy(
                    budgetSummary = summary,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(
            showAddDialog = true,
            addCategoryId = null,
            addMonthlyLimit = "",
            addDescription = "",
            addAlertThreshold = "80"
        )
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun updateAddCategoryId(categoryId: Long) {
        _uiState.value = _uiState.value.copy(addCategoryId = categoryId)
    }

    fun updateAddMonthlyLimit(limit: String) {
        if (limit.isEmpty() || limit.all { it.isDigit() || it == '.' }) {
            _uiState.value = _uiState.value.copy(addMonthlyLimit = limit)
        }
    }

    fun updateAddDescription(description: String) {
        _uiState.value = _uiState.value.copy(addDescription = description)
    }

    fun updateAddAlertThreshold(threshold: String) {
        if (threshold.isEmpty() || threshold.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(addAlertThreshold = threshold)
        }
    }

    fun addBudget() {
        val state = _uiState.value
        val categoryId = state.addCategoryId
        val limit = state.addMonthlyLimit.toDoubleOrNull()

        if (categoryId == null || limit == null || limit <= 0) {
            _uiState.value = state.copy(errorMessage = "Pilih kategori dan masukkan batas budget")
            return
        }

        viewModelScope.launch {
            try {
                val budget = Budget(
                    categoryId = categoryId,
                    monthlyLimit = limit,
                    description = state.addDescription,
                    alertThreshold = state.addAlertThreshold.toDoubleOrNull() ?: 80.0
                )
                budgetRepository.addBudget(budget)
                _uiState.value = _uiState.value.copy(
                    showAddDialog = false,
                    successMessage = "Budget berhasil ditambahkan"
                )
                loadBudgets()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun deleteBudget(budgetWithCategory: BudgetWithCategory) {
        viewModelScope.launch {
            try {
                budgetRepository.deleteBudget(budgetWithCategory.budget)
                _uiState.value = _uiState.value.copy(
                    successMessage = "Budget berhasil dihapus"
                )
                loadBudgets()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun previousMonth() {
        _uiState.value = _uiState.value.copy(
            selectedMonth = _uiState.value.selectedMonth.minusMonths(1)
        )
        loadBudgets()
    }

    fun nextMonth() {
        _uiState.value = _uiState.value.copy(
            selectedMonth = _uiState.value.selectedMonth.plusMonths(1)
        )
        loadBudgets()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
}
