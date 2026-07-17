package com.financeapp.data.repository

import com.financeapp.data.database.ChallengeDao
import com.financeapp.data.database.DailyQuestDao
import com.financeapp.data.database.UserProgressDao
import com.financeapp.data.database.XpHistoryDao
import com.financeapp.data.model.Challenge
import com.financeapp.data.model.DailyQuest
import com.financeapp.data.model.UserProgress
import com.financeapp.data.model.XpHistory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GamificationRepository @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val dailyQuestDao: DailyQuestDao,
    private val challengeDao: ChallengeDao,
    private val xpHistoryDao: XpHistoryDao
) {
    // User Progress
    fun getUserProgress(): Flow<UserProgress?> = userProgressDao.getProgress()
    suspend fun getUserProgressOnce(): UserProgress? = userProgressDao.getProgressOnce()
    suspend fun saveUserProgress(progress: UserProgress) = userProgressDao.insert(progress)

    // Daily Quests
    fun getDailyQuests(date: LocalDate): Flow<List<DailyQuest>> =
        dailyQuestDao.getQuestsForDate(date.toString())

    suspend fun getDailyQuestsOnce(date: LocalDate): List<DailyQuest> =
        dailyQuestDao.getQuestsForDateOnce(date.toString())

    suspend fun saveDailyQuests(quests: List<DailyQuest>) = dailyQuestDao.insertAll(quests)

    suspend fun updateQuestProgress(id: Long, value: Int, completed: Boolean) =
        dailyQuestDao.updateProgress(id, value, completed)

    suspend fun completedQuestCount(date: LocalDate): Int =
        dailyQuestDao.completedCount(date.toString())

    suspend fun cleanOldQuests(beforeDate: LocalDate) =
        dailyQuestDao.cleanOldQuests(beforeDate.toString())

    // Challenges
    fun getActiveChallenges(): Flow<List<Challenge>> = challengeDao.getActiveChallenges()
    fun getCompletedChallenges(): Flow<List<Challenge>> = challengeDao.getCompletedChallenges()
    suspend fun saveChallenges(challenges: List<Challenge>) = challengeDao.insertAll(challenges)
    suspend fun getChallengeById(id: Long): Challenge? = challengeDao.getById(id)
    suspend fun updateChallengeProgress(id: Long, value: Int, completed: Boolean) =
        challengeDao.updateProgress(id, value, completed)

    // XP History
    fun getRecentXpHistory(limit: Int = 20): Flow<List<XpHistory>> = xpHistoryDao.getRecent(limit)
    suspend fun addXpEntry(entry: XpHistory) = xpHistoryDao.insert(entry)
    suspend fun totalXp(): Int = xpHistoryDao.totalXp() ?: 0
}
