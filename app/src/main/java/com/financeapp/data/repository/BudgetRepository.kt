package com.financeapp.data.repository

import com.financeapp.data.database.BudgetDao
import com.financeapp.data.database.TransactionDao
import com.financeapp.data.model.Budget
import com.financeapp.data.model.BudgetSummary
import com.financeapp.data.model.BudgetWithCategory
import com.financeapp.data.model.Category
import com.financeapp.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.YearMonth

class BudgetRepository(
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
        val budgets = budgetDao.getActiveBudgets()
        val startDate = month.atDay(1).atStartOfDay()
        val endDate = month.atEndOfMonth().atTime(23, 59, 59)

        // We need to collect once - this is a suspend function
        val budgetList = mutableListOf<BudgetWithCategory>()

        for (budget in getBudgetList()) {
            val category = categoryRepository.getCategory(budget.categoryId)
            if (category != null) {
                val spent = getSpentForCategory(budget.categoryId, startDate, endDate)
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

        return BudgetSummary(
            totalBudget = totalBudget,
            totalSpent = totalSpent,
            budgets = budgetList,
            exceedingBudgets = exceeding,
            budgetHealth = health
        )
    }

    private suspend fun getBudgetList(): List<Budget> {
        return budgetDao.getActiveBudgets().first()
    }

    private suspend fun getSpentForCategory(
        categoryId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Double {
        // Sum expenses for this category in the date range
        return transactionDao.sumByTypeAndCategory(
            TransactionType.EXPENSE,
            categoryId,
            startDate,
            endDate
        ) ?: 0.0
    }
}
