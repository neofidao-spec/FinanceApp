package com.financeapp.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_quests", indices = [Index("questDate")])
data class DailyQuest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val xpReward: Int,
    val questType: String,       // TRANSACTION_COUNT, BUDGET_CHECK, DASHBOARD_VISIT
    val targetValue: Int,
    val currentValue: Int = 0,
    val isCompleted: Boolean = false,
    val questDate: LocalDate
) {
    val progress: Float
        get() = if (targetValue > 0) (currentValue.toFloat() / targetValue).coerceIn(0f, 1f) else 0f

    companion object {
        fun generateForDate(date: LocalDate): List<DailyQuest> = listOf(
            DailyQuest(
                name = "Catat Transaksi",
                description = "Catat 1 transaksi hari ini",
                xpReward = 10,
                questType = QuestType.TRANSACTION_COUNT.name,
                targetValue = 1,
                questDate = date
            ),
            DailyQuest(
                name = "Cek Dashboard",
                description = "Lihat dashboard hari ini",
                xpReward = 5,
                questType = QuestType.DASHBOARD_VISIT.name,
                targetValue = 1,
                questDate = date
            ),
            DailyQuest(
                name = "Budget Check",
                description = "Pastikan tidak ada kategori yang melebihi budget",
                xpReward = 20,
                questType = QuestType.BUDGET_CHECK.name,
                targetValue = 1,
                questDate = date
            )
        )
    }
}

enum class QuestType {
    TRANSACTION_COUNT,
    BUDGET_CHECK,
    DASHBOARD_VISIT
}
