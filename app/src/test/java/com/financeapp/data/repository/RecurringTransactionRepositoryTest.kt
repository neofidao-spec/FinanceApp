package com.financeapp.data.repository

import com.financeapp.data.database.RecurringTransactionDao
import com.financeapp.data.model.RecurringEndType
import com.financeapp.data.model.RecurringInterval
import com.financeapp.data.model.RecurringTransaction
import com.financeapp.data.model.TransactionType
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class RecurringTransactionRepositoryTest {

    private lateinit var recurringDao: RecurringTransactionDao
    private lateinit var repository: RecurringTransactionRepository

    private val today = LocalDate.now()

    @Before
    fun setup() {
        recurringDao = mockk(relaxed = true)
        repository = RecurringTransactionRepository(recurringDao)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ===================== calculateNextDue =====================

    @Test
    fun `calculateNextDue DAILY adds intervalValue days`() {
        val fromDate = LocalDate.of(2026, 1, 1)

        val result = repository.calculateNextDue(fromDate, RecurringInterval.DAILY, 1)

        assertEquals(LocalDate.of(2026, 1, 2), result)
    }

    @Test
    fun `calculateNextDue DAILY with intervalValue 3 adds 3 days`() {
        val fromDate = LocalDate.of(2026, 1, 1)

        val result = repository.calculateNextDue(fromDate, RecurringInterval.DAILY, 3)

        assertEquals(LocalDate.of(2026, 1, 4), result)
    }

    @Test
    fun `calculateNextDue WEEKLY adds intervalValue weeks`() {
        val fromDate = LocalDate.of(2026, 1, 1)

        val result = repository.calculateNextDue(fromDate, RecurringInterval.WEEKLY, 1)

        assertEquals(LocalDate.of(2026, 1, 8), result)
    }

    @Test
    fun `calculateNextDue WEEKLY with intervalValue 2 adds 2 weeks`() {
        val fromDate = LocalDate.of(2026, 1, 1)

        val result = repository.calculateNextDue(fromDate, RecurringInterval.WEEKLY, 2)

        assertEquals(LocalDate.of(2026, 1, 15), result)
    }

    @Test
    fun `calculateNextDue BIWEEKLY adds intervalValue times 2 weeks`() {
        val fromDate = LocalDate.of(2026, 1, 1)

        val result = repository.calculateNextDue(fromDate, RecurringInterval.BIWEEKLY, 1)

        assertEquals(LocalDate.of(2026, 1, 15), result)
    }

    @Test
    fun `calculateNextDue MONTHLY adds intervalValue months`() {
        val fromDate = LocalDate.of(2026, 1, 1)

        val result = repository.calculateNextDue(fromDate, RecurringInterval.MONTHLY, 1)

        assertEquals(LocalDate.of(2026, 2, 1), result)
    }

    @Test
    fun `calculateNextDue MONTHLY with intervalValue 3 adds 3 months`() {
        val fromDate = LocalDate.of(2026, 1, 1)

        val result = repository.calculateNextDue(fromDate, RecurringInterval.MONTHLY, 3)

        assertEquals(LocalDate.of(2026, 4, 1), result)
    }

    @Test
    fun `calculateNextDue YEARLY adds intervalValue years`() {
        val fromDate = LocalDate.of(2026, 1, 1)

        val result = repository.calculateNextDue(fromDate, RecurringInterval.YEARLY, 1)

        assertEquals(LocalDate.of(2027, 1, 1), result)
    }

    @Test
    fun `calculateNextDue YEARLY with intervalValue 2 adds 2 years`() {
        val fromDate = LocalDate.of(2026, 6, 15)

        val result = repository.calculateNextDue(fromDate, RecurringInterval.YEARLY, 2)

        assertEquals(LocalDate.of(2028, 6, 15), result)
    }

    // ===================== advanceOccurrence =====================

    @Test
    fun `advanceOccurrence calls dao advanceOccurrence with correct nextDue`() = runTest {
        val recurring = RecurringTransaction(
            id = 1, amount = 100.0, description = "Test",
            type = TransactionType.EXPENSE, categoryId = 5,
            interval = RecurringInterval.MONTHLY, intervalValue = 1,
            startDate = today, nextDueDate = today,
            endType = RecurringEndType.NEVER
        )
        coEvery { recurringDao.advanceOccurrence(any(), any()) } just runs

        repository.advanceOccurrence(recurring)

        val expectedNextDue = today.plusMonths(1)
        coVerify { recurringDao.advanceOccurrence(1L, expectedNextDue) }
    }

    @Test
    fun `advanceOccurrence deactivates when maxOccurrences reached`() = runTest {
        val recurring = RecurringTransaction(
            id = 1, amount = 100.0, description = "Test",
            type = TransactionType.EXPENSE, categoryId = 5,
            interval = RecurringInterval.MONTHLY, intervalValue = 1,
            startDate = today, nextDueDate = today,
            endType = RecurringEndType.AFTER_COUNT,
            maxOccurrences = 5,
            occurrencesGenerated = 4  // 4 + 1 = 5 >= 5 -> deactivate
        )
        coEvery { recurringDao.advanceOccurrence(any(), any()) } just runs
        coEvery { recurringDao.deactivate(any()) } just runs

        repository.advanceOccurrence(recurring)

        coVerify { recurringDao.deactivate(1L) }
    }

    @Test
    fun `advanceOccurrence does not deactivate when below maxOccurrences`() = runTest {
        val recurring = RecurringTransaction(
            id = 1, amount = 100.0, description = "Test",
            type = TransactionType.EXPENSE, categoryId = 5,
            interval = RecurringInterval.MONTHLY, intervalValue = 1,
            startDate = today, nextDueDate = today,
            endType = RecurringEndType.AFTER_COUNT,
            maxOccurrences = 10,
            occurrencesGenerated = 3  // 3 + 1 = 4 < 10 -> no deactivation
        )
        coEvery { recurringDao.advanceOccurrence(any(), any()) } just runs

        repository.advanceOccurrence(recurring)

        coVerify(exactly = 0) { recurringDao.deactivate(any()) }
    }

    @Test
    fun `advanceOccurrence deactivates when nextDue is past endDate`() = runTest {
        val endDate = today.plusDays(5)
        val recurring = RecurringTransaction(
            id = 1, amount = 100.0, description = "Test",
            type = TransactionType.EXPENSE, categoryId = 5,
            interval = RecurringInterval.MONTHLY, intervalValue = 1,
            startDate = today, nextDueDate = today,
            endType = RecurringEndType.ON_DATE,
            endDate = endDate
        )
        coEvery { recurringDao.advanceOccurrence(any(), any()) } just runs
        coEvery { recurringDao.deactivate(any()) } just runs

        repository.advanceOccurrence(recurring)

        // nextDue = today + 1 month > endDate (today + 5 days)
        coVerify { recurringDao.deactivate(1L) }
    }

    @Test
    fun `advanceOccurrence does not deactivate when nextDue is before endDate`() = runTest {
        val endDate = today.plusYears(1)
        val recurring = RecurringTransaction(
            id = 1, amount = 100.0, description = "Test",
            type = TransactionType.EXPENSE, categoryId = 5,
            interval = RecurringInterval.DAILY, intervalValue = 1,
            startDate = today, nextDueDate = today,
            endType = RecurringEndType.ON_DATE,
            endDate = endDate
        )
        coEvery { recurringDao.advanceOccurrence(any(), any()) } just runs

        repository.advanceOccurrence(recurring)

        // nextDue = today + 1 day, which is before endDate (today + 1 year)
        coVerify(exactly = 0) { recurringDao.deactivate(any()) }
    }

    @Test
    fun `advanceOccurrence with NEVER endType does not deactivate`() = runTest {
        val recurring = RecurringTransaction(
            id = 1, amount = 100.0, description = "Test",
            type = TransactionType.EXPENSE, categoryId = 5,
            interval = RecurringInterval.MONTHLY, intervalValue = 1,
            startDate = today, nextDueDate = today,
            endType = RecurringEndType.NEVER,
            maxOccurrences = 1, occurrencesGenerated = 10
        )
        coEvery { recurringDao.advanceOccurrence(any(), any()) } just runs

        repository.advanceOccurrence(recurring)

        coVerify(exactly = 0) { recurringDao.deactivate(any()) }
    }

    // ===================== getDueTransactions =====================

    @Test
    fun `getDueTransactions delegates to dao with today date`() = runTest {
        val dueList = listOf(
            RecurringTransaction(
                id = 1, amount = 50.0, description = "Due",
                type = TransactionType.EXPENSE, categoryId = 5,
                interval = RecurringInterval.DAILY, intervalValue = 1,
                startDate = today, nextDueDate = today
            )
        )
        coEvery { recurringDao.getDueTransactions(today) } returns dueList

        val result = repository.getDueTransactions()

        assertEquals(1, result.size)
        assertEquals("Due", result[0].description)
        coVerify { recurringDao.getDueTransactions(today) }
    }

    @Test
    fun `getDueTransactions returns empty list when none are due`() = runTest {
        coEvery { recurringDao.getDueTransactions(today) } returns emptyList()

        val result = repository.getDueTransactions()

        assertTrue(result.isEmpty())
    }

    // ===================== DAO delegation =====================

    @Test
    fun `insert delegates to dao`() = runTest {
        val recurring = RecurringTransaction(
            amount = 100.0, description = "Test",
            type = TransactionType.EXPENSE, categoryId = 5,
            interval = RecurringInterval.MONTHLY,
            startDate = today, nextDueDate = today
        )
        coEvery { recurringDao.insert(recurring) } returns 1L

        val id = repository.insert(recurring)

        assertEquals(1L, id)
        coVerify { recurringDao.insert(recurring) }
    }

    @Test
    fun `deactivate delegates to dao`() = runTest {
        coEvery { recurringDao.deactivate(1L) } just runs

        repository.deactivate(1L)

        coVerify { recurringDao.deactivate(1L) }
    }
}
