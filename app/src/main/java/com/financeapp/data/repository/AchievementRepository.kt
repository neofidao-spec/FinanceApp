package com.financeapp.data.repository

import com.financeapp.data.database.AchievementDao
import com.financeapp.data.model.Achievement
import com.financeapp.data.model.DefaultAchievements
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AchievementRepository(private val dao: AchievementDao) {

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
        val achievements = listOf("Jutawan")
        achievements.forEach { name ->
            val achievement = dao.getAll().let { flow ->
                var result: Achievement? = null
                flow.collect { list ->
                    result = list.find { it.name == name && !it.isUnlocked }
                    return@collect
                }
                result
            }
            achievement?.let {
                val newValue = balance.toInt().coerceAtMost(it.targetValue)
                dao.updateProgress(it.id, newValue)
                if (newValue >= it.targetValue) {
                    dao.unlock(it.id, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                }
            }
        }
    }

    suspend fun updateConsistencyStreak(streakDays: Int) {
        updateAchievementByCategory("CONSISTENCY", streakDays)
    }

    private suspend fun updateAchievementByCategory(category: String, value: Int) {
        // This is a simplified version - in production you'd use proper Flow collection
        dao.getByCategory(category).collect { achievements ->
            achievements.filter { !it.isUnlocked }.forEach { achievement ->
                val newValue = value.coerceAtMost(achievement.targetValue)
                if (newValue > achievement.currentValue) {
                    dao.updateProgress(achievement.id, newValue)
                    if (newValue >= achievement.targetValue) {
                        dao.unlock(achievement.id, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    }
                }
            }
            return@collect
        }
    }
}
