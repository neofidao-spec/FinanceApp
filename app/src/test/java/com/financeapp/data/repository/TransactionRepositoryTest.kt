package com.financeapp.data.repository

import com.financeapp.data.database.TransactionDao
import com.financeapp.data.database.TransactionFtsDao
import com.financeapp.data.model.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionRepositoryTest {

    private lateinit var dao: TransactionDao
    private lateinit var ftsDao: TransactionFtsDao
    private lateinit var repository: TransactionRepository

    private val now = LocalDateTime.now()
    private val expenseCategory = Category(
        id = 5, name = "Makanan", icon = "🍔", iconName = "LocalDining",
        type = TransactionType.EXPENSE, color = "#E65100"
    )
    private val incomeCategory = Category(
        id = 1, name = "Gaji", icon = "💰", iconName = "Work",
        type = TransactionType.INCOME, color = "#2E7D32"
    )
    private val expenseTransaction = Transaction(
        id = 1, amount = 50000.0, type = TransactionType.EXPENSE,
        categoryId = 5, description = "Makan siang", date = now
    )
    private val incomeTransaction = Transaction(
        id = 2, amount = 5000000.0, type = TransactionType.INCOME,
        categoryId = 1, description = "Gaji bulanan", date = now
    )

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        ftsDao = mockk(relaxed = true)
        repository = TransactionRepository(dao, ftsDao)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ---------- ADD ----------

    @Test
    fun `addTransaction delegates to dao insert and returns id`() = runTest {
        coEvery { dao.insert(any()) } returns 1L

        val id = repository.addTransaction(expenseTransaction)

        assertEquals(1L, id)
        coVerify { dao.insert(expenseTransaction) }
    }

    // ---------- UPDATE ----------

    @Test
    fun `updateTransaction delegates to dao update`() = runTest {
        coEvery { dao.update(any()) } just runs

        repository.updateTransaction(expenseTransaction)

        coVerify { dao.update(expenseTransaction) }
    }

    // ---------- DELETE ----------

    @Test
    fun `deleteTransaction delegates to dao delete`() = runTest {
        coEvery { dao.delete(any()) } just runs

        repository.deleteTransaction(expenseTransaction)

        coVerify { dao.delete(expenseTransaction) }
    }

    @Test
    fun `deleteTransactionById delegates to dao deleteById`() = runTest {
        coEvery { dao.deleteById(any()) } just runs

        repository.deleteTransactionById(1L)

        coVerify { dao.deleteById(1L) }
    }

    // ---------- GET ----------

    @Test
    fun `getTransaction returns transaction from dao`() = runTest {
        coEvery { dao.getById(1) } returns expenseTransaction

        val result = repository.getTransaction(1)

        assertEquals(expenseTransaction, result)
        coVerify { dao.getById(1) }
    }

    @Test
    fun `getTransaction returns null for nonexistent id`() = runTest {
        coEvery { dao.getById(999) } returns null

        val result = repository.getTransaction(999)

        assertNull(result)
    }

    // ---------- GET ALL ----------

    @Test
    fun `getAllTransactions returns flow from dao`() = runTest {
        val twc = listOf(
            TransactionWithCategory(expenseTransaction, expenseCategory),
            TransactionWithCategory(incomeTransaction, incomeCategory)
        )
        every { dao.getAllWithCategory() } returns flowOf(twc)

        val result = repository.getAllTransactions().first()

        assertEquals(2, result.size)
    }

    @Test
    fun `getAllTransactions returns empty list when no transactions`() = runTest {
        every { dao.getAllWithCategory() } returns flowOf(emptyList())

        val result = repository.getAllTransactions().first()

        assertTrue(result.isEmpty())
    }

    // ---------- GET BY TYPE ----------

    @Test
    fun `getTransactionsByType returns filtered flow from dao`() = runTest {
        val twc = listOf(TransactionWithCategory(expenseTransaction, expenseCategory))
        every { dao.getByType(TransactionType.EXPENSE) } returns flowOf(twc)

        val result = repository.getTransactionsByType(TransactionType.EXPENSE).first()

        assertEquals(1, result.size)
        assertEquals(TransactionType.EXPENSE, result[0].transaction.type)
    }

    // ---------- TOTALS ----------

    @Test
    fun `getTotalIncome returns sum from dao`() = runTest {
        val startDate = now.minusDays(30)
        coEvery { dao.sumByType(TransactionType.INCOME, startDate, now) } returns 5000000.0

        val total = repository.getTotalIncome(startDate, now)

        assertEquals(5000000.0, total, 0.01)
    }

    @Test
    fun `getTotalIncome returns 0 when dao returns null`() = runTest {
        val startDate = now.minusDays(30)
        coEvery { dao.sumByType(TransactionType.INCOME, startDate, now) } returns null

        val total = repository.getTotalIncome(startDate, now)

        assertEquals(0.0, total, 0.01)
    }

    @Test
    fun `getTotalExpense returns sum from dao`() = runTest {
        val startDate = now.minusDays(30)
        coEvery { dao.sumByType(TransactionType.EXPENSE, startDate, now) } returns 150000.0

        val total = repository.getTotalExpense(startDate, now)

        assertEquals(150000.0, total, 0.01)
    }

    @Test
    fun `getTotalExpense returns 0 when dao returns null`() = runTest {
        val startDate = now.minusDays(30)
        coEvery { dao.sumByType(TransactionType.EXPENSE, startDate, now) } returns null

        val total = repository.getTotalExpense(startDate, now)

        assertEquals(0.0, total, 0.01)
    }

    // ---------- SEARCH ----------

    @Test
    fun `searchTransactions uses ftsDao when available`() = runTest {
        val twc = listOf(TransactionWithCategory(expenseTransaction, expenseCategory))
        every { ftsDao.search("makan*") } returns flowOf(twc)

        val result = repository.searchTransactions("makan").first()

        assertEquals(1, result.size)
        verify { ftsDao.search("makan*") }
    }

    @Test
    fun `searchTransactions falls back to dao when ftsDao is null`() = runTest {
        val repoNoFts = TransactionRepository(dao, null)
        val twc = listOf(TransactionWithCategory(expenseTransaction, expenseCategory))
        every { dao.getAllWithCategory() } returns flowOf(twc)

        val result = repoNoFts.searchTransactions("makan").first()

        assertEquals(1, result.size)
        verify { dao.getAllWithCategory() }
    }

    // ---------- COUNT ----------

    @Test
    fun `getTransactionCount returns count from dao`() = runTest {
        coEvery { dao.count() } returns 42

        val count = repository.getTransactionCount()

        assertEquals(42, count)
    }
}
