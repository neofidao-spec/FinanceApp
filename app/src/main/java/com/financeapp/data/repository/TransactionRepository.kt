package com.financeapp.data.repository

import com.financeapp.data.database.TransactionDao
import com.financeapp.data.database.TransactionFtsDao
import com.financeapp.data.model.Transaction
import com.financeapp.data.model.TransactionType
import com.financeapp.data.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class TransactionRepository(
    private val dao: TransactionDao,
    private val ftsDao: TransactionFtsDao? = null
) {
    fun getAllTransactions(): Flow<List<TransactionWithCategory>> = dao.getAllWithCategory()

    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionWithCategory>> =
        dao.getByType(type)

    /** FTS4 full-text search on transaction descriptions. Falls back to LIKE if FTS unavailable. */
    fun searchTransactions(query: String): Flow<List<TransactionWithCategory>> {
        val ftsQuery = "$query*"
        return ftsDao?.search(ftsQuery) ?: dao.searchByDescription(query).let { results ->
            kotlinx.coroutines.flow.flowOf(results)
        }
    }

    suspend fun addTransaction(transaction: Transaction): Long = dao.insert(transaction)

    suspend fun updateTransaction(transaction: Transaction) = dao.update(transaction)

    suspend fun deleteTransaction(transaction: Transaction) = dao.delete(transaction)

    suspend fun deleteTransactionById(id: Long) = dao.deleteById(id)

    suspend fun getTransaction(id: Long): Transaction? = dao.getById(id)

    suspend fun getTotalIncome(startDate: LocalDateTime, endDate: LocalDateTime): Double =
        dao.sumByType(TransactionType.INCOME, startDate, endDate) ?: 0.0

    suspend fun getTotalExpense(startDate: LocalDateTime, endDate: LocalDateTime): Double =
        dao.sumByType(TransactionType.EXPENSE, startDate, endDate) ?: 0.0

    suspend fun getTransactionCount(): Int = dao.count()

    suspend fun getAllTransactionsOnce(): List<TransactionWithCategory> = dao.getAllTransactionsOnce()
}
