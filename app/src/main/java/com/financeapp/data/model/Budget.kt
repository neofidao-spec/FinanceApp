package com.financeapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val monthlyLimit: Double,
    val description: String = "",
    val alertThreshold: Double = 80.0,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class BudgetWithCategory(
    val budget: Budget,
    val category: Category,
    val currentSpent: Double = 0.0,
    val remaining: Double = 0.0,
    val percentage: Float = 0f
) {
    fun isExceeded(): Boolean = currentSpent > budget.monthlyLimit
    fun isAlertThreshold(): Boolean = (currentSpent / budget.monthlyLimit * 100) >= budget.alertThreshold
}

data class BudgetSummary(
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val budgets: List<BudgetWithCategory> = emptyList(),
    val exceedingBudgets: List<BudgetWithCategory> = emptyList(),
    val budgetHealth: Float = 100f
)
