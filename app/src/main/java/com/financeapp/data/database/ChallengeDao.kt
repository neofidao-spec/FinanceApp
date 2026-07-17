package com.financeapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.financeapp.data.model.Challenge
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(challenge: Challenge)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(challenges: List<Challenge>)

    @Query("SELECT * FROM challenges WHERE isCompleted = 0 ORDER BY endDate")
    fun getActiveChallenges(): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE isCompleted = 1 ORDER BY id DESC")
    fun getCompletedChallenges(): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE challengeType = :type AND isCompleted = 0")
    fun getActiveByType(type: String): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE id = :id")
    suspend fun getById(id: Long): Challenge?

    @Query("UPDATE challenges SET currentValue = :value, isCompleted = :completed WHERE id = :id")
    suspend fun updateProgress(id: Long, value: Int, completed: Boolean)

    @Query("SELECT COUNT(*) FROM challenges WHERE isCompleted = 1")
    suspend fun completedCount(): Int
}
