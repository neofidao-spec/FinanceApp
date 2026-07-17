package com.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.model.BudgetWithCategory
import com.financeapp.data.model.CategorySummary
import com.financeapp.data.model.TransactionType
import com.financeapp.data.model.TransactionWithCategory
import com.financeapp.data.repository.BudgetRepository
import com.financeapp.data.repository.CategoryRepository
import com.financeapp.data.repository.TransactionRepository
import com.financeapp.domain.GetHealthScoreUseCase
import com.financeapp.domain.HealthScore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Data class representing monthly trend data for the line chart.
 */
data class MonthlyTrendData(
    val month: String,
    val income: Double,
    val expense: Double
)

data class DashboardUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val recentTransactions: List<TransactionWithCategory> = emptyList(),
    val topExpenses: List<CategorySummary> = emptyList(),
    val categoryBreakdown: List<CategorySummary> = emptyList(),
    val monthlyTrend: List<MonthlyTrendData> = emptyList(),
    val spendingRate: Float = 0f,
    val budgetSummaries: List<BudgetWithCategory> = emptyList(),
    val healthScore: HealthScore? = null,
    val isLoading: Boolean = true,
    val selectedMonth: YearMonth = YearMonth.now(),
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val getHealthScoreUseCase: GetHealthScoreUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    private var allTransactions: List<TransactionWithCategory> = emptyList()
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
        loadMonthlyTrend()
        loadBudgetSummaries()
        observeBudgets()
        loadHealthScore()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { transactions ->
                allTransactions = transactions
                updateDashboardStats(transactions)
                loadMonthlyTrend()
            }
        }
    }


    private fun updateDashboardStats(transactions: List<TransactionWithCategory>) {
        val month = _uiState.value.selectedMonth
        val startDate = month.atDay(1).atStartOfDay()
        val endDate = month.atEndOfMonth().atTime(23, 59, 59)

        val monthTransactions = transactions.filter { it.transaction.date in startDate..endDate }.ifEmpty { transactions }

        // Calculate income and expense from the list directly
        val income = monthTransactions
            .filter { it.transaction.type == TransactionType.INCOME }
            .sumOf { it.transaction.amount }
        val expense = monthTransactions
            .filter { it.transaction.type == TransactionType.EXPENSE }
            .sumOf { it.transaction.amount }
        val balance = income - expense
        val rate = if (income > 0) (expense / income).toFloat() else 0f

        // Category breakdown
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
            totalIncome = income,
            totalExpense = expense,
            balance = balance,
            spendingRate = rate,
            recentTransactions = recent,
            topExpenses = topExpenses,
            categoryBreakdown = topExpenses,
            isLoading = false
        )
    }
    private fun loadMonthlyTrend() {
        viewModelScope.launch {
            try {
                val monthFormatter = DateTimeFormatter.ofPattern("MMM")
                val trendData = mutableListOf<MonthlyTrendData>()
                val currentMonth = YearMonth.now()

                // Load last 6 months of data
                for (i in 5 downTo 0) {
                    val month = currentMonth.minusMonths(i.toLong())
                    val startDate = month.atDay(1).atStartOfDay()
                    val endDate = month.atEndOfMonth().atTime(23, 59, 59)

                    val income = transactionRepository.getTotalIncome(startDate, endDate)
                    val expense = transactionRepository.getTotalExpense(startDate, endDate)

                    trendData.add(
                        MonthlyTrendData(
                            month = month.atDay(1).format(monthFormatter),
                            income = income,
                            expense = expense
                        )
                    )
                }

                _uiState.value = _uiState.value.copy(monthlyTrend = trendData)
            } catch (e: Exception) {
                // Silently fail for trend data
            }
        }
    }

    private fun loadBudgetSummaries() {
        viewModelScope.launch {
            try {
                val summary = budgetRepository.getBudgetSummary(_uiState.value.selectedMonth)
                _uiState.value = _uiState.value.copy(budgetSummaries = summary.budgets)
            } catch (e: Exception) {
                // Silently fail for budget summaries
            }
        }
    }

    fun selectMonth(month: YearMonth) {
        _uiState.value = _uiState.value.copy(selectedMonth = month)
        updateDashboardStats(allTransactions)
        loadBudgetSummaries()

    }

    fun retry() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        updateDashboardStats(allTransactions)
        loadMonthlyTrend()
        loadBudgetSummaries()

        loadHealthScore()
    }

    private fun observeBudgets() {
        viewModelScope.launch {
            budgetRepository.getActiveBudgets().collect {
                loadBudgetSummaries()
            }
        }
    }

    private fun loadHealthScore() {
        viewModelScope.launch {
            try {
                val score = getHealthScoreUseCase()
                _uiState.value = _uiState.value.copy(healthScore = score)
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
}
