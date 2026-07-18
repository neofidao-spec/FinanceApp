package com.financeapp.domain

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.financeapp.data.model.RecurringInterval
import com.financeapp.data.model.RecurringTransaction
import com.financeapp.data.model.Transaction
import com.financeapp.data.model.TransactionType
import com.financeapp.data.repository.RecurringTransactionRepository
import com.financeapp.data.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Periodic worker that runs daily to generate transactions from recurring schedules.
 */
@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val recurringRepo: RecurringTransactionRepository,
    private val transactionRepo: TransactionRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val dueTransactions = recurringRepo.getDueTransactions()
            var generated = 0

            for (recurring in dueTransactions) {
                if (!recurring.isActive) continue

                // Idempotency check: skip if already generated today
                val today = LocalDate.now()
                if (recurring.nextDueDate.isAfter(today)) continue

                val transaction = Transaction(
                    amount = recurring.amount,
                    type = recurring.type,
                    categoryId = recurring.categoryId,
                    description = "[Otomatis] ${recurring.description}",
                    date = LocalDateTime.now(),
                    accountId = recurring.accountId
                )
                transactionRepo.addTransaction(transaction)
                recurringRepo.advanceOccurrence(recurring)
                generated++
            }

            if (generated > 0) {
                Result.success()
            } else {
                Result.success()  // No due transactions — not an error
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "recurring_transaction_worker"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
                24, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
