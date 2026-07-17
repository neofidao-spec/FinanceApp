package com.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.model.CategorySummary
import com.financeapp.data.model.MonthlyReport
import com.financeapp.data.model.TransactionType
import com.financeapp.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class ReportUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val monthlyReport: MonthlyReport? = null,
    val previousMonths: List<MonthlyReport> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class ReportViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        loadMonthlyReport()
    }

    fun loadMonthlyReport() {
        viewModelScope.launch {
            try {
                val month = _uiState.value.currentMonth
                val startDate = month.atDay(1).atStartOfDay()
                val endDate = month.atEndOfMonth().atTime(23, 59, 59)

                val income = transactionRepository.getTotalIncome(startDate, endDate)
                val expense = transactionRepository.getTotalExpense(startDate, endDate)
                val balance = income - expense

                val report = MonthlyReport(
                    month = month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    income = income,
                    expense = expense,
                    balance = balance,
                    categoryBreakdown = emptyList()
                )

                _uiState.value = _uiState.value.copy(
                    monthlyReport = report,
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

    fun selectMonth(month: YearMonth) {
        _uiState.value = _uiState.value.copy(currentMonth = month)
        loadMonthlyReport()
    }

    fun previousMonth() {
        val newMonth = _uiState.value.currentMonth.minusMonths(1)
        selectMonth(newMonth)
    }

    fun nextMonth() {
        val newMonth = _uiState.value.currentMonth.plusMonths(1)
        selectMonth(newMonth)
    }
}
