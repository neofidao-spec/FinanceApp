package com.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.model.CategorySummary
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
import java.time.YearMonth
import javax.inject.Inject

data class DashboardUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val recentTransactions: List<TransactionWithCategory> = emptyList(),
    val topExpenses: List<CategorySummary> = emptyList(),
    val isLoading: Boolean = true,
    val selectedMonth: YearMonth = YearMonth.now(),
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        observeTransactions()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { transactions ->
                updateDashboardStats(transactions)
            }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                val month = _uiState.value.selectedMonth
                val startDate = month.atDay(1).atStartOfDay()
                val endDate = month.atEndOfMonth().atTime(23, 59, 59)

                val income = transactionRepository.getTotalIncome(startDate, endDate)
                val expense = transactionRepository.getTotalExpense(startDate, endDate)

                _uiState.value = _uiState.value.copy(
                    totalIncome = income,
                    totalExpense = expense,
                    balance = income - expense,
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

    private fun updateDashboardStats(transactions: List<TransactionWithCategory>) {
        val month = _uiState.value.selectedMonth
        val startDate = month.atDay(1).atStartOfDay()
        val endDate = month.atEndOfMonth().atTime(23, 59, 59)

        val monthTransactions = transactions.filter { it.transaction.date in startDate..endDate }
        val expenses = monthTransactions.filter { it.transaction.type == TransactionType.EXPENSE }

        val categoryExpenseMap = expenses.groupingBy { it.category }.fold(0.0) { acc, item ->
            acc + item.transaction.amount
        }

        val totalExpense = categoryExpenseMap.values.sum()
        val topExpenses = categoryExpenseMap.map { (category, amount) ->
            CategorySummary(
                category = category,
                total = amount,
                percentage = if (totalExpense > 0) (amount / totalExpense * 100).toFloat() else 0f
            )
        }.sortedByDescending { it.total }.take(5)

        val recent = monthTransactions.sortedByDescending { it.transaction.date }.take(10)

        _uiState.value = _uiState.value.copy(
            recentTransactions = recent,
            topExpenses = topExpenses
        )
    }

    fun selectMonth(month: YearMonth) {
        _uiState.value = _uiState.value.copy(selectedMonth = month)
        loadDashboardData()
    }
}
