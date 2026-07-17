package com.financeapp.data.repository

import com.financeapp.data.database.AchievementDao
import com.financeapp.data.model.Achievement
import com.financeapp.data.model.DefaultAchievements
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class AchievementRepository @Inject constructor(private val dao: AchievementDao) {

    fun getAllAchievements(): Flow<List<Achievement>> = dao.getAll()

    fun getUnlockedAchievements(): Flow<List<Achievement>> = dao.getUnlocked()

    suspend fun getUnlockedCount(): Int = dao.unlockedCount()

    suspend fun initializeDefaultAchievements() {
        if (dao.count() == 0) {
            dao.insertAll(DefaultAchievements.getDefault())
        }
    }

    suspend fun updateTransactionCount(count: Int) {
        updateAchievementByCategory("TRANSACTIONS", count)
    }

    suspend fun updateBalance(balance: Double) {
        val achievements = dao.getAll().first()
        achievements.filter { it.name == "Jutawan" && !it.isUnlocked }.forEach { achievement ->
            val newValue = balance.toInt().coerceAtMost(achievement.targetValue)
            dao.updateProgress(achievement.id, newValue)
            if (newValue >= achievement.targetValue) {
                dao.unlock(achievement.id, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            }
        }
    }

    suspend fun updateConsistencyStreak(streakDays: Int) {
        updateAchievementByCategory("CONSISTENCY", streakDays)
    }

    private suspend fun updateAchievementByCategory(category: String, value: Int) {
        val achievements = dao.getByCategory(category).first()
        achievements.filter { !it.isUnlocked }.forEach { achievement ->
            val newValue = value.coerceAtMost(achievement.targetValue)
            if (newValue > achievement.currentValue) {
                dao.updateProgress(achievement.id, newValue)
                if (newValue >= achievement.targetValue) {
                    dao.unlock(achievement.id, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                }
            }
        }
    }
}
