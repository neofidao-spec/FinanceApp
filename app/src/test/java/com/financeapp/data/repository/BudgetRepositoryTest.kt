package com.financeapp.data.repository

import com.financeapp.data.database.BudgetDao
import com.financeapp.data.database.TransactionDao
import com.financeapp.data.model.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetRepositoryTest {

    private lateinit var budgetDao: BudgetDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var repository: BudgetRepository

    private val foodCategory = Category(
        id = 5, name = "Makanan", icon = "🍔", iconName = "LocalDining",
        type = TransactionType.EXPENSE, color = "#E65100"
    )
    private val transportCategory = Category(
        id = 6, name = "Transportasi", icon = "🚗", iconName = "DirectionsBus",
        type = TransactionType.EXPENSE, color = "#1565C0"
    )

    @Before
    fun setup() {
        budgetDao = mockk(relaxed = true)
        transactionDao = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        repository = BudgetRepository(budgetDao, transactionDao, categoryRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ===================== getBudgetSummary =====================

    @Test
    fun `getBudgetSummary with normal budgets returns correct summary`() = runTest {
        val budgets = listOf(
            Budget(id = 1, categoryId = 5, monthlyLimit = 1000.0, isActive = true),
            Budget(id = 2, categoryId = 6, monthlyLimit = 500.0, isActive = true)
        )
        every { budgetDao.getActiveBudgets() } returns flowOf(budgets)
        coEvery { categoryRepository.getCategory(5) } returns foodCategory
        coEvery { categoryRepository.getCategory(6) } returns transportCategory
        coEvery { transactionDao.sumByTypeAndCategory(any(), 5, any(), any()) } returns 300.0
        coEvery { transactionDao.sumByTypeAndCategory(any(), 6, any(), any()) } returns 200.0

        val summary = repository.getBudgetSummary()

        assertEquals(1500.0, summary.totalBudget, 0.01)
        assertEquals(500.0, summary.totalSpent, 0.01)
        assertEquals(2, summary.budgets.size)
        assertTrue(summary.exceedingBudgets.isEmpty())
    }

    @Test
    fun `getBudgetSummary with empty budgets returns empty summary`() = runTest {
        every { budgetDao.getActiveBudgets() } returns flowOf(emptyList())

        val summary = repository.getBudgetSummary()

        assertEquals(0.0, summary.totalBudget, 0.01)
        assertEquals(0.0, summary.totalSpent, 0.01)
        assertTrue(summary.budgets.isEmpty())
        assertTrue(summary.exceedingBudgets.isEmpty())
        assertEquals(100f, summary.budgetHealth, 0.01f)
    }

    @Test
    fun `getBudgetSummary with exceeded budget lists it in exceedingBudgets`() = runTest {
        val budgets = listOf(
            Budget(id = 1, categoryId = 5, monthlyLimit = 500.0, isActive = true)
        )
        every { budgetDao.getActiveBudgets() } returns flowOf(budgets)
        coEvery { categoryRepository.getCategory(5) } returns foodCategory
        coEvery { transactionDao.sumByTypeAndCategory(any(), 5, any(), any()) } returns 700.0

        val summary = repository.getBudgetSummary()

        assertEquals(1, summary.exceedingBudgets.size)
        assertEquals(700.0, summary.exceedingBudgets[0].currentSpent, 0.01)
        assertTrue(summary.exceedingBudgets[0].isExceeded())
    }

    @Test
    fun `percentage calculation is correct`() = runTest {
        val budgets = listOf(
            Budget(id = 1, categoryId = 5, monthlyLimit = 1000.0, isActive = true)
        )
        every { budgetDao.getActiveBudgets() } returns flowOf(budgets)
        coEvery { categoryRepository.getCategory(5) } returns foodCategory
        coEvery { transactionDao.sumByTypeAndCategory(any(), 5, any(), any()) } returns 750.0

        val summary = repository.getBudgetSummary()

        assertEquals(1, summary.budgets.size)
        assertEquals(75f, summary.budgets[0].percentage, 0.01f)
    }

    @Test
    fun `percentage is 0 when monthlyLimit is 0`() = runTest {
        val budgets = listOf(
            Budget(id = 1, categoryId = 5, monthlyLimit = 0.0, isActive = true)
        )
        every { budgetDao.getActiveBudgets() } returns flowOf(budgets)
        coEvery { categoryRepository.getCategory(5) } returns foodCategory
        coEvery { transactionDao.sumByTypeAndCategory(any(), 5, any(), any()) } returns 100.0

        val summary = repository.getBudgetSummary()

        assertEquals(1, summary.budgets.size)
        assertEquals(0f, summary.budgets[0].percentage, 0.01f)
    }

    @Test
    fun `budgetHealth is 100 when totalBudget is 0`() = runTest {
        every { budgetDao.getActiveBudgets() } returns flowOf(emptyList())

        val summary = repository.getBudgetSummary()

        assertEquals(100f, summary.budgetHealth, 0.01f)
    }

    @Test
    fun `budgetHealth is calculated correctly with spending`() = runTest {
        val budgets = listOf(
            Budget(id = 1, categoryId = 5, monthlyLimit = 1000.0, isActive = true)
        )
        every { budgetDao.getActiveBudgets() } returns flowOf(budgets)
        coEvery { categoryRepository.getCategory(5) } returns foodCategory
        coEvery { transactionDao.sumByTypeAndCategory(any(), 5, any(), any()) } returns 300.0

        val summary = repository.getBudgetSummary()

        // health = (1000 - 300) / 1000 * 100 = 70
        assertEquals(70f, summary.budgetHealth, 0.01f)
    }

    @Test
    fun `budgetHealth is clamped to minimum 0`() = runTest {
        val budgets = listOf(
            Budget(id = 1, categoryId = 5, monthlyLimit = 100.0, isActive = true)
        )
        every { budgetDao.getActiveBudgets() } returns flowOf(budgets)
        coEvery { categoryRepository.getCategory(5) } returns foodCategory
        coEvery { transactionDao.sumByTypeAndCategory(any(), 5, any(), any()) } returns 500.0

        val summary = repository.getBudgetSummary()

        // health = (100 - 500) / 100 * 100 = -400 -> clamped to 0
        assertEquals(0f, summary.budgetHealth, 0.01f)
    }

    @Test
    fun `budget with null category is excluded from summary`() = runTest {
        val budgets = listOf(
            Budget(id = 1, categoryId = 999, monthlyLimit = 500.0, isActive = true)
        )
        every { budgetDao.getActiveBudgets() } returns flowOf(budgets)
        coEvery { categoryRepository.getCategory(999) } returns null

        val summary = repository.getBudgetSummary()

        assertTrue(summary.budgets.isEmpty())
    }

    @Test
    fun `spending of null from DAO is treated as 0`() = runTest {
        val budgets = listOf(
            Budget(id = 1, categoryId = 5, monthlyLimit = 500.0, isActive = true)
        )
        every { budgetDao.getActiveBudgets() } returns flowOf(budgets)
        coEvery { categoryRepository.getCategory(5) } returns foodCategory
        coEvery { transactionDao.sumByTypeAndCategory(any(), 5, any(), any()) } returns null

        val summary = repository.getBudgetSummary()

        assertEquals(0.0, summary.budgets[0].currentSpent, 0.01)
        assertEquals(500.0, summary.budgets[0].remaining, 0.01)
    }

    // ===================== CRUD delegation =====================

    @Test
    fun `addBudget delegates to dao insert`() = runTest {
        val budget = Budget(categoryId = 5, monthlyLimit = 1000.0)
        coEvery { budgetDao.insert(budget) } returns 1L

        val id = repository.addBudget(budget)

        assertEquals(1L, id)
        coVerify { budgetDao.insert(budget) }
    }

    @Test
    fun `deleteBudget delegates to dao delete`() = runTest {
        val budget = Budget(id = 1, categoryId = 5, monthlyLimit = 1000.0)
        coEvery { budgetDao.delete(budget) } just runs

        repository.deleteBudget(budget)

        coVerify { budgetDao.delete(budget) }
    }
}
