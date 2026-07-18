package com.financeapp.data.repository

import com.financeapp.data.database.CategoryDao
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

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryRepositoryTest {

    private lateinit var dao: CategoryDao
    private lateinit var repository: CategoryRepository

    private val expenseCategory = Category(
        id = 5, name = "Makanan", icon = "🍔", iconName = "LocalDining",
        type = TransactionType.EXPENSE, color = "#E65100"
    )
    private val incomeCategory = Category(
        id = 1, name = "Gaji", icon = "💰", iconName = "Work",
        type = TransactionType.INCOME, color = "#2E7D32"
    )

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        repository = CategoryRepository(dao)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ---------- GET ALL ----------

    @Test
    fun `getAllCategories returns flow from dao`() = runTest {
        every { dao.getAll() } returns flowOf(listOf(expenseCategory, incomeCategory))

        val result = repository.getAllCategories().first()

        assertEquals(2, result.size)
        assertTrue(result.contains(expenseCategory))
        assertTrue(result.contains(incomeCategory))
    }

    @Test
    fun `getAllCategories returns empty list when no categories`() = runTest {
        every { dao.getAll() } returns flowOf(emptyList())

        val result = repository.getAllCategories().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllCategoriesOnce returns list from dao flow first`() = runTest {
        every { dao.getAll() } returns flowOf(listOf(expenseCategory, incomeCategory))

        val result = repository.getAllCategoriesOnce()

        assertEquals(2, result.size)
    }

    // ---------- GET BY TYPE ----------

    @Test
    fun `getCategoriesByType returns filtered flow from dao`() = runTest {
        every { dao.getByType(TransactionType.EXPENSE) } returns flowOf(listOf(expenseCategory))

        val result = repository.getCategoriesByType(TransactionType.EXPENSE).first()

        assertEquals(1, result.size)
        assertEquals(TransactionType.EXPENSE, result[0].type)
    }

    // ---------- GET BY ID ----------

    @Test
    fun `getCategory returns category from dao`() = runTest {
        coEvery { dao.getById(5) } returns expenseCategory

        val result = repository.getCategory(5)

        assertEquals(expenseCategory, result)
    }

    @Test
    fun `getCategory returns null for nonexistent id`() = runTest {
        coEvery { dao.getById(999) } returns null

        val result = repository.getCategory(999)

        assertNull(result)
    }

    // ---------- ADD / UPDATE / DELETE ----------

    @Test
    fun `addCategory delegates to dao insert`() = runTest {
        coEvery { dao.insert(any()) } returns 13L

        val id = repository.addCategory(expenseCategory)

        assertEquals(13L, id)
        coVerify { dao.insert(expenseCategory) }
    }

    @Test
    fun `updateCategory delegates to dao update`() = runTest {
        coEvery { dao.update(any()) } just runs

        repository.updateCategory(expenseCategory)

        coVerify { dao.update(expenseCategory) }
    }

    @Test
    fun `deleteCategory delegates to dao delete`() = runTest {
        coEvery { dao.delete(any()) } just runs

        repository.deleteCategory(expenseCategory)

        coVerify { dao.delete(expenseCategory) }
    }

    // ---------- INITIALIZE DEFAULTS ----------

    @Test
    fun `initializeDefaultCategories inserts when count is 0`() = runTest {
        coEvery { dao.count() } returns 0
        coEvery { dao.insertAll(any()) } just runs

        repository.initializeDefaultCategories()

        coVerify { dao.insertAll(any()) }
    }

    @Test
    fun `initializeDefaultCategories does nothing when count is greater than 0`() = runTest {
        coEvery { dao.count() } returns 12

        repository.initializeDefaultCategories()

        coVerify(exactly = 0) { dao.insertAll(any()) }
    }
}
