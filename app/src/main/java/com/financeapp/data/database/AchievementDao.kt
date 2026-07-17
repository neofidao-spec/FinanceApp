package com.financeapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.financeapp.data.model.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(achievement: Achievement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<Achievement>)

    @Update
    suspend fun update(achievement: Achievement)

    @Query("SELECT * FROM achievements ORDER BY category, id")
    fun getAll(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE category = :category ORDER BY id")
    fun getByCategory(category: String): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1 ORDER BY unlockedAt DESC")
    fun getUnlocked(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getById(id: Long): Achievement?

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM achievements WHERE isUnlocked = 1")
    suspend fun unlockedCount(): Int

    @Query("UPDATE achievements SET currentValue = :value WHERE id = :id")
    suspend fun updateProgress(id: Long, value: Int)

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedAt = :unlockedAt, currentValue = targetValue WHERE id = :id")
    suspend fun unlock(id: Long, unlockedAt: String)
}
