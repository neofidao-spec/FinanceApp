package com.financeapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.financeapp.data.model.XpHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface XpHistoryDao {
    @Insert
    suspend fun insert(entry: XpHistory)

    @Query("SELECT * FROM xp_history ORDER BY createdAt DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<XpHistory>>

    @Query("SELECT * FROM xp_history ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentOnce(limit: Int): List<XpHistory>

    @Query("SELECT SUM(amount) FROM xp_history WHERE source = :source")
    suspend fun totalBySource(source: String): Int?

    @Query("SELECT SUM(amount) FROM xp_history")
    suspend fun totalXp(): Int?

    @Query("SELECT COUNT(*) FROM xp_history")
    suspend fun count(): Int
}
