package com.financeapp.domain

import com.financeapp.data.repository.TransactionRepository
import java.time.YearMonth
import javax.inject.Inject

data class HealthScore(
    val score: Int,
    val trend: Trend,
    val description: String,
    val category: String
) {
    enum class Trend { UP, DOWN, STABLE }
}

class GetHealthScoreUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(): HealthScore {
        val currentMonth = YearMonth.now()
        val previousMonth = currentMonth.minusMonths(1)

        val currentIncome = transactionRepository.getTotalIncome(
            currentMonth.atDay(1).atStartOfDay(),
            currentMonth.atEndOfMonth().atTime(23, 59, 59)
        )
        val currentExpense = transactionRepository.getTotalExpense(
            currentMonth.atDay(1).atStartOfDay(),
            currentMonth.atEndOfMonth().atTime(23, 59, 59)
        )

        val previousIncome = transactionRepository.getTotalIncome(
            previousMonth.atDay(1).atStartOfDay(),
            previousMonth.atEndOfMonth().atTime(23, 59, 59)
        )
        val previousExpense = transactionRepository.getTotalExpense(
            previousMonth.atDay(1).atStartOfDay(),
            previousMonth.atEndOfMonth().atTime(23, 59, 59)
        )

        // Calculate score: (income - expense) / income * 100
        val score = if (currentIncome > 0) {
            ((currentIncome - currentExpense) / currentIncome * 100).toInt().coerceIn(0, 100)
        } else if (currentExpense == 0.0) {
            100 // No transactions = neutral good
        } else {
            0 // Only expenses, no income
        }

        // Calculate trend
        val currentRate = if (currentIncome > 0) (currentIncome - currentExpense) / currentIncome else 0.0
        val previousRate = if (previousIncome > 0) (previousIncome - previousExpense) / previousIncome else 0.0

        val trend = when {
            currentRate > previousRate + 0.05 -> HealthScore.Trend.UP
            currentRate < previousRate - 0.05 -> HealthScore.Trend.DOWN
            else -> HealthScore.Trend.STABLE
        }

        // Category and description
        val (category, description) = when {
            score >= 80 -> "Excellent" to "Keuanganmu sangat sehat! Pertahankan!"
            score >= 60 -> "Good" to "Keuanganmu dalam kondisi baik."
            score >= 40 -> "Fair" to "Keuanganmu cukup, tapi bisa lebih baik."
            score >= 20 -> "Poor" to "Pengeluaranmu terlalu tinggi."
            else -> "Critical" to "Keuanganmu perlu perhatian serius."
        }

        return HealthScore(
            score = score,
            trend = trend,
            description = description,
            category = category
        )
    }
}
