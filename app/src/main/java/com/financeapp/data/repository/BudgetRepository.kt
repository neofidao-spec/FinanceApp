package com.financeapp.data.repository

import com.financeapp.data.database.BudgetDao
import com.financeapp.data.database.TransactionDao
import com.financeapp.data.model.Budget
import com.financeapp.data.model.BudgetSummary
import com.financeapp.data.model.BudgetWithCategory
import com.financeapp.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao,
    private val categoryRepository: CategoryRepository
) {
    fun getActiveBudgets(): Flow<List<Budget>> = budgetDao.getActiveBudgets()

    suspend fun getBudget(id: Long): Budget? = budgetDao.getById(id)

    suspend fun addBudget(budget: Budget): Long = budgetDao.insert(budget)

    suspend fun updateBudget(budget: Budget) = budgetDao.update(budget)

    suspend fun deleteBudget(budget: Budget) = budgetDao.delete(budget)

    suspend fun deleteBudgetById(id: Long) = budgetDao.deleteById(id)

    suspend fun deactivateBudget(id: Long) = budgetDao.deactivate(id)

    suspend fun getBudgetSummary(month: YearMonth = YearMonth.now()): BudgetSummary {
        return try {
            val budgets = budgetDao.getActiveBudgets().first()
            val startDate = month.atDay(1).atStartOfDay()
            val endDate = month.atEndOfMonth().atTime(23, 59, 59)

            val budgetList = mutableListOf<BudgetWithCategory>()

            for (budget in budgets) {
                val category = categoryRepository.getCategory(budget.categoryId)
                if (category != null) {
                    val spent = transactionDao.sumByTypeAndCategory(
                        TransactionType.EXPENSE,
                        budget.categoryId,
                        startDate,
                        endDate
                    ) ?: 0.0

                    val remaining = budget.monthlyLimit - spent
                    val percentage = if (budget.monthlyLimit > 0) {
                        (spent / budget.monthlyLimit * 100).toFloat()
                    } else 0f

                    budgetList.add(
                        BudgetWithCategory(
                            budget = budget,
                            category = category,
                            currentSpent = spent,
                            remaining = remaining,
                            percentage = percentage
                        )
                    )
                }
            }

            val totalBudget = budgetList.sumOf { it.budget.monthlyLimit }
            val totalSpent = budgetList.sumOf { it.currentSpent }
            val exceeding = budgetList.filter { it.isExceeded() }
            val health = if (totalBudget > 0) {
                ((totalBudget - totalSpent) / totalBudget * 100).toFloat().coerceIn(0f, 100f)
            } else 100f

            BudgetSummary(
                totalBudget = totalBudget,
                totalSpent = totalSpent,
                budgets = budgetList,
                exceedingBudgets = exceeding,
                budgetHealth = health
            )
        } catch (e: Exception) {
            throw RuntimeException("Gagal memuat ringkasan budget: ${e.message}", e)
        }
    }
}
