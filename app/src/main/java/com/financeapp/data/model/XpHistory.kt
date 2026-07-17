package com.financeapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "xp_history")
data class XpHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Int,
    val source: String,      // TRANSACTION, STREAK, QUEST, CHALLENGE, ACHIEVEMENT, DAILY_LOGIN
    val description: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class XpSource {
    TRANSACTION,
    STREAK,
    QUEST,
    CHALLENGE,
    ACHIEVEMENT,
    DAILY_LOGIN,
    BUDGET_ADHERENCE
}
