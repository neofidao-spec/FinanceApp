package com.financeapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.financeapp.data.model.DailyQuest
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyQuestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quest: DailyQuest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quests: List<DailyQuest>)

    @Query("SELECT * FROM daily_quests WHERE questDate = :date ORDER BY id")
    fun getQuestsForDate(date: String): Flow<List<DailyQuest>>

    @Query("SELECT * FROM daily_quests WHERE questDate = :date ORDER BY id")
    suspend fun getQuestsForDateOnce(date: String): List<DailyQuest>

    @Query("SELECT * FROM daily_quests WHERE isCompleted = 0 AND questDate = :date ORDER BY id")
    fun getIncompleteQuests(date: String): Flow<List<DailyQuest>>

    @Query("UPDATE daily_quests SET currentValue = :value, isCompleted = :completed WHERE id = :id")
    suspend fun updateProgress(id: Long, value: Int, completed: Boolean)

    @Query("SELECT COUNT(*) FROM daily_quests WHERE questDate = :date AND isCompleted = 1")
    suspend fun completedCount(date: String): Int

    @Query("DELETE FROM daily_quests WHERE questDate < :date")
    suspend fun cleanOldQuests(date: String)
}
