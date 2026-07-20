package com.financeapp.domain

import android.util.Log
import com.financeapp.data.repository.BudgetRepository
import com.financeapp.data.repository.TransactionRepository
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

class QuestConditionEvaluator @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend fun evaluate(condition: String): Boolean {
        return when (condition) {
            "NO_BUDGET_EXISTS" -> {
                val summary = budgetRepository.getBudgetSummary(YearMonth.now())
                summary.budgets.isEmpty()
            }
            "HAS_BUDGET_NEAR_LIMIT" -> {
                val summary = budgetRepository.getBudgetSummary(YearMonth.now())
                summary.budgets.any { it.percentage >= 70f && !it.isExceeded() }
            }
            "MIN_3_MONTHS_DATA" -> {
                val now = YearMonth.now()
                val threeMonthsAgo = now.minusMonths(2)
                val start = threeMonthsAgo.atDay(1).atStartOfDay()
                val end = now.atEndOfMonth().atTime(23, 59, 59)
                val transactions = transactionRepository.getAllTransactionsOnce()
                val months = transactions
                    .filter { !it.transaction.date.isBefore(start) && !it.transaction.date.isAfter(end) }
                    .map { YearMonth.from(it.transaction.date) }.distinct()
                months.size >= 3
            }
            "MIN_2_MONTHS_DATA" -> {
                val now = YearMonth.now()
                val twoMonthsAgo = now.minusMonths(1)
                val start = twoMonthsAgo.atDay(1).atStartOfDay()
                val end = now.atEndOfMonth().atTime(23, 59, 59)
                val transactions = transactionRepository.getAllTransactionsOnce()
                val months = transactions
                    .filter { !it.transaction.date.isBefore(start) && !it.transaction.date.isAfter(end) }
                    .map { YearMonth.from(it.transaction.date) }.distinct()
                months.size >= 2
            }
            "HAS_SPENDING_HISTORY" -> {
                val now = YearMonth.now()
                val start = now.atDay(1).atStartOfDay()
                val end = now.atEndOfMonth().atTime(23, 59, 59)
                val transactions = transactionRepository.getAllTransactionsOnce()
                transactions.any { !it.transaction.date.isBefore(start) && !it.transaction.date.isAfter(end) }
            }
            else -> {
                Log.w("QuestConditionEvaluator", "Unknown condition: $condition")
                false
            }
        }
    }
}
