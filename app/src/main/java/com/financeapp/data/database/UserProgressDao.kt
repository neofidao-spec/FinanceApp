package com.financeapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.financeapp.data.model.UserProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: UserProgress)

    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun getProgress(): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress WHERE id = 1")
    suspend fun getProgressOnce(): UserProgress?

    @Query("UPDATE user_progress SET totalXp = :xp, currentLevel = :level, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateXpAndLevel(xp: Int, level: Int, updatedAt: String)

    @Query("UPDATE user_progress SET currentStreak = :streak, bestStreak = :best, lastActivityDate = :date, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateStreak(streak: Int, best: Int, date: String, updatedAt: String)

    @Query("UPDATE user_progress SET healthScore = :score, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateHealthScore(score: Double, updatedAt: String)

    @Query("UPDATE user_progress SET streakFreezes = :freezes WHERE id = 1")
    suspend fun useFreeze(freezes: Int)

    @Query("SELECT COUNT(*) FROM user_progress")
    suspend fun count(): Int
}
