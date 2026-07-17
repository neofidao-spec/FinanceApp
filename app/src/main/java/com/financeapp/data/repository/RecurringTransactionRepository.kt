package com.financeapp.data.repository

import com.financeapp.data.database.RecurringTransactionDao
import com.financeapp.data.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringTransactionRepository @Inject constructor(
    private val recurringDao: RecurringTransactionDao
) {
    fun getAllRecurring(): Flow<List<RecurringTransaction>> = recurringDao.getAll()

    fun getAllActiveRecurring(): Flow<List<RecurringTransaction>> = recurringDao.getAllActive()

    suspend fun getById(id: Long): RecurringTransaction? = recurringDao.getById(id)

    suspend fun insert(recurring: RecurringTransaction): Long = recurringDao.insert(recurring)

    suspend fun update(recurring: RecurringTransaction) = recurringDao.update(recurring)

    suspend fun delete(recurring: RecurringTransaction) = recurringDao.delete(recurring)

    suspend fun deactivate(id: Long) = recurringDao.deactivate(id)

    /** Calculate the next due date based on interval + intervalValue */
    fun calculateNextDue(fromDate: LocalDate, interval: com.financeapp.data.model.RecurringInterval, intervalValue: Int): LocalDate {
        return when (interval) {
            com.financeapp.data.model.RecurringInterval.DAILY -> fromDate.plusDays(intervalValue.toLong())
            com.financeapp.data.model.RecurringInterval.WEEKLY -> fromDate.plusWeeks(intervalValue.toLong())
            com.financeapp.data.model.RecurringInterval.BIWEEKLY -> fromDate.plusWeeks(intervalValue.toLong() * 2)
            com.financeapp.data.model.RecurringInterval.MONTHLY -> fromDate.plusMonths(intervalValue.toLong())
            com.financeapp.data.model.RecurringInterval.YEARLY -> fromDate.plusYears(intervalValue.toLong())
        }
    }

    /** Get all recurring transactions that are due today or earlier */
    suspend fun getDueTransactions(): List<RecurringTransaction> =
        recurringDao.getDueTransactions(LocalDate.now())

    /** Advance one occurrence: increment counter, calculate next due date */
    suspend fun advanceOccurrence(recurring: RecurringTransaction) {
        val nextDue = calculateNextDue(recurring.nextDueDate, recurring.interval, recurring.intervalValue)
        recurringDao.advanceOccurrence(recurring.id, nextDue)

        // Check if max occurrences reached
        if (recurring.endType == com.financeapp.data.model.RecurringEndType.AFTER_COUNT &&
            recurring.maxOccurrences > 0 &&
            recurring.occurrencesGenerated + 1 >= recurring.maxOccurrences
        ) {
            recurringDao.deactivate(recurring.id)
        }

        // Check if past end date
        if (recurring.endType == com.financeapp.data.model.RecurringEndType.ON_DATE &&
            recurring.endDate != null &&
            nextDue.isAfter(recurring.endDate)
        ) {
            recurringDao.deactivate(recurring.id)
        }
    }
}
