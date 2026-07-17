package com.financeapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val xpReward: Int,
    val challengeType: String,   // WEEKLY, MONTHLY, SPECIAL
    val startDate: LocalDate,
    val endDate: LocalDate,
    val targetType: String,      // BUDGET_ADHERENCE, TRANSACTION_COUNT, SAVINGS_RATE
    val targetValue: Int,
    val currentValue: Int = 0,
    val isCompleted: Boolean = false
) {
    val progress: Float
        get() = if (targetValue > 0) (currentValue.toFloat() / targetValue).coerceIn(0f, 1f) else 0f

    val isActive: Boolean
        get() {
            val today = LocalDate.now()
            return !isCompleted && !today.isBefore(startDate) && !today.isAfter(endDate)
        }

    companion object {
        fun generateWeeklyChallenges(startDate: LocalDate): List<Challenge> = listOf(
            Challenge(
                name = "Hemat Week",
                description = "Pengeluaran di bawah 80% budget minggu ini",
                xpReward = 200,
                challengeType = ChallengeType.WEEKLY.name,
                startDate = startDate,
                endDate = startDate.plusDays(6),
                targetType = "BUDGET_ADHERENCE",
                targetValue = 7 // 7 hari di bawah budget
            ),
            Challenge(
                name = "Tracker Week",
                description = "Catat minimal 1 transaksi setiap hari minggu ini",
                xpReward = 150,
                challengeType = ChallengeType.WEEKLY.name,
                startDate = startDate,
                endDate = startDate.plusDays(6),
                targetType = "TRANSACTION_COUNT",
                targetValue = 7
            )
        )

        fun generateMonthlyChallenges(yearMonth: java.time.YearMonth): List<Challenge> = listOf(
            Challenge(
                name = "Savings Master",
                description = "Tabung minimal 20% income bulan ini",
                xpReward = 1000,
                challengeType = ChallengeType.MONTHLY.name,
                startDate = yearMonth.atDay(1),
                endDate = yearMonth.atEndOfMonth(),
                targetType = "SAVINGS_RATE",
                targetValue = 20
            ),
            Challenge(
                name = "Budget Hero",
                description = "Semua kategori di bawah budget bulan ini",
                xpReward = 750,
                challengeType = ChallengeType.MONTHLY.name,
                startDate = yearMonth.atDay(1),
                endDate = yearMonth.atEndOfMonth(),
                targetType = "BUDGET_ADHERENCE",
                targetValue = 30 // semua hari di bawah budget
            )
        )
    }
}

enum class ChallengeType {
    WEEKLY,
    MONTHLY,
    SPECIAL
}
