package com.financeapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

enum class RecurringInterval {
    DAILY, WEEKLY, BIWEEKLY, MONTHLY, YEARLY
}

enum class RecurringEndType {
    NEVER, AFTER_COUNT, ON_DATE
}

@Entity(
    tableName = "recurring_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("categoryId"),
        Index("nextDueDate")
    ]
)
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long = 1,
    val interval: RecurringInterval,
    val intervalValue: Int = 1,       // every N intervals (e.g. every 2 months)
    val startDate: LocalDate,
    val endDate: LocalDate? = null,   // null = no end date
    val endType: RecurringEndType = RecurringEndType.NEVER,
    val maxOccurrences: Int = 0,      // 0 = unlimited (for AFTER_COUNT)
    val occurrencesGenerated: Int = 0,
    val nextDueDate: LocalDate,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
