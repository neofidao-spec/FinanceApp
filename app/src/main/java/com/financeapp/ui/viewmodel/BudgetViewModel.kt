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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class BudgetUiState(
    val categories: List<Category> = emptyList(),
    val budgetSummary: BudgetSummary? = null,
    val selectedMonth: YearMonth = YearMonth.now(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val addCategoryId: Long = 0,
    val addMonthlyLimit: String = "",
    val addDescription: String = "",
    val addAlertThreshold: String = "80",
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        // Single coroutine handles all initialization
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Load categories once
                val categories = categoryRepository.getAllCategories().first()
                _uiState.value = _uiState.value.copy(categories = categories)

                // Load budgets for current month
                val summary = budgetRepository.getBudgetSummary(_uiState.value.selectedMonth)
                _uiState.value = _uiState.value.copy(
                    budgetSummary = summary,
                    isLoading = false
                )

                // Observe budget changes in background
                launch {
                    budgetRepository.getActiveBudgets().collect {
                        val newSummary = budgetRepository.getBudgetSummary(_uiState.value.selectedMonth)
                        _uiState.value = _uiState.value.copy(budgetSummary = newSummary)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(
            showAddDialog = false,
            addCategoryId = 0,
            addMonthlyLimit = "",
            addDescription = "",
            addAlertThreshold = "80"
        )
    }

    fun updateAddCategoryId(id: Long) {
        _uiState.value = _uiState.value.copy(addCategoryId = id)
    }

    fun updateAddMonthlyLimit(limit: String) {
        _uiState.value = _uiState.value.copy(addMonthlyLimit = limit)
    }

    fun updateAddDescription(desc: String) {
        _uiState.value = _uiState.value.copy(addDescription = desc)
    }

    fun updateAddAlertThreshold(threshold: String) {
        _uiState.value = _uiState.value.copy(addAlertThreshold = threshold)
    }

    fun addBudget() {
        val state = _uiState.value
        val limit = state.addMonthlyLimit.toDoubleOrNull()
        if (limit == null || limit <= 0 || state.addCategoryId == 0L) {
            _uiState.value = state.copy(errorMessage = "Isi semua field dengan benar")
            return
        }

        viewModelScope.launch {
            try {
                val budget = Budget(
                    categoryId = state.addCategoryId,
                    monthlyLimit = limit,
                    alertThreshold = (state.addAlertThreshold.toDoubleOrNull() ?: 80.0) / 100.0,
                    description = state.addDescription,
                )
                budgetRepository.addBudget(budget)
                _uiState.value = _uiState.value.copy(
                    successMessage = "Budget berhasil ditambahkan",
                    showAddDialog = false,
                    addCategoryId = 0,
                    addMonthlyLimit = "",
                    addDescription = "",
                    addAlertThreshold = "80"
                )
                clearMessages()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
                clearMessages()
            }
        }
    }

    fun deleteBudget(budgetWithCategory: BudgetWithCategory) {
        viewModelScope.launch {
            try {
                budgetRepository.deleteBudget(budgetWithCategory.budget)
                _uiState.value = _uiState.value.copy(successMessage = "Budget berhasil dihapus")
                clearMessages()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
                clearMessages()
            }
        }
    }

    fun previousMonth() {
        val newMonth = _uiState.value.selectedMonth.minusMonths(1)
        _uiState.value = _uiState.value.copy(selectedMonth = newMonth)
        loadBudgets()
    }

    fun nextMonth() {
        val newMonth = _uiState.value.selectedMonth.plusMonths(1)
        _uiState.value = _uiState.value.copy(selectedMonth = newMonth)
        loadBudgets()
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            try {
                val summary = budgetRepository.getBudgetSummary(_uiState.value.selectedMonth)
                _uiState.value = _uiState.value.copy(budgetSummary = summary)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearMessages() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(
                successMessage = null,
                errorMessage = null
            )
        }
    }
}
