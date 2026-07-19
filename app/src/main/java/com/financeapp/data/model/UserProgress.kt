package com.financeapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Long = 1, // single row — only one user
    val totalXp: Int = 0,
    val currentLevel: Int = 1,
    val bestStreak: Int = 0,
    val currentStreak: Int = 0,
    val streakFreezes: Int = 0,
    val lastActivityDate: LocalDateTime? = null,
    val healthScore: Double = 0.0,
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val levelTitle: String
        get() = when (currentLevel) {
            1 -> "Pemula Finansial"
            2 -> "Pencatat Rajin"
            3 -> "Pengelola Budget"
            4 -> "Hemat Bijaksana"
            5 -> "Perencana Keuangan"
            6 -> "Ahli Keuangan"
            7 -> "Master Finansial"
            8 -> "Financial Guru"
            9 -> "Money Sensei"
            10 -> "Financial Legend"
            else -> "Financial Legend"
        }

    val xpForNextLevel: Int
        get() = when (currentLevel) {
            1 -> 200
            2 -> 500
            3 -> 1000
            4 -> 2000
            5 -> 4000
            6 -> 8000
            7 -> 15000
            8 -> 25000
            9 -> 50000
            else -> 50000
        }

    val xpForCurrentLevel: Int
        get() = when (currentLevel) {
            1 -> 0
            2 -> 200
            3 -> 500
            4 -> 1000
            5 -> 2000
            6 -> 4000
            7 -> 8000
            8 -> 15000
            9 -> 25000
            else -> 50000
        }

    val levelProgress: Float
        get() {
            val range = xpForNextLevel - xpForCurrentLevel
            if (range <= 0) return 1f
            val progress = totalXp - xpForCurrentLevel
            return (progress.toFloat() / range).coerceIn(0f, 1f)
        }

    val isMaxLevel: Boolean
        get() = currentLevel >= 10
}
