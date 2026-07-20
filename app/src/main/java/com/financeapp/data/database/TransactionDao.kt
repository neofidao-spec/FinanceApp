package com.financeapp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.financeapp.data.model.Category
import com.financeapp.data.model.Transaction
import com.financeapp.data.model.TransactionType
import com.financeapp.data.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllWithCategory(): Flow<List<TransactionWithCategory>>

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getByType(type: TransactionType): Flow<List<TransactionWithCategory>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun sumByType(type: TransactionType, startDate: LocalDateTime, endDate: LocalDateTime): Double?

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactionsOnce(): List<TransactionWithCategory>

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE description LIKE '%' || :query || '%' ORDER BY date DESC")
    suspend fun searchByDescription(query: String): List<TransactionWithCategory>

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE description LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchByDescriptionFlow(query: String): Flow<List<TransactionWithCategory>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND categoryId = :categoryId AND date BETWEEN :startDate AND :endDate")
    suspend fun sumByTypeAndCategory(type: TransactionType, categoryId: Long, startDate: LocalDateTime, endDate: LocalDateTime): Double?

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId AND type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun getByCategoryAndDateRange(categoryId: Long, type: TransactionType, startDate: LocalDateTime, endDate: LocalDateTime): List<Transaction>
}
