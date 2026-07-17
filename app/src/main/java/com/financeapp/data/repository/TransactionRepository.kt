package com.financeapp.data.repository

import com.financeapp.data.database.TransactionDao
import com.financeapp.data.model.Transaction
import com.financeapp.data.model.TransactionType
import com.financeapp.data.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class TransactionRepository(private val dao: TransactionDao) {
    fun getAllTransactions(): Flow<List<TransactionWithCategory>> = dao.getAllWithCategory()

    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionWithCategory>> =
        dao.getByType(type)

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
}
