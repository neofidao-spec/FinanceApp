package com.financeapp.data.model

import androidx.room.Embedded
import androidx.room.Relation
import java.time.LocalDateTime

data class TransactionWithCategory(
    @Embedded
    val transaction: Transaction,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category
)

data class DashboardStats(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val recentTransactions: List<TransactionWithCategory> = emptyList(),
    val topExpenseCategories: List<CategorySummary> = emptyList()
)

data class CategorySummary(
    val category: Category,
    val total: Double,
    val percentage: Float
)

data class MonthlyReport(
    val month: String,
    val income: Double,
    val expense: Double,
    val balance: Double,
    val categoryBreakdown: List<CategorySummary>
)
