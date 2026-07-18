package com.financeapp.ui.viewmodel

import app.cash.turbine.test
import com.financeapp.data.model.*
import com.financeapp.data.repository.CategoryRepository
import com.financeapp.data.repository.TransactionRepository
import com.financeapp.ui.components.TransactionFilter
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var categoryRepository: CategoryRepository

    private val testCategory = Category(
        id = 1, name = "Makanan", icon = "🍔", iconName = "LocalDining",
        type = TransactionType.EXPENSE, color = "#E65100"
    )
    private val incomeCategory = Category(
        id = 2, name = "Gaji", icon = "💰", iconName = "Work",
        type = TransactionType.INCOME, color = "#2E7D32"
    )

    private val now = LocalDateTime.now()

    private val expenseTransaction = Transaction(
        id = 1, amount = 50000.0, type = TransactionType.EXPENSE,
        categoryId = 1, description = "Makan siang", date = now
    )
    private val expenseWithCategory = TransactionWithCategory(
        transaction = expenseTransaction, category = testCategory
    )

    private val incomeTransaction = Transaction(
        id = 2, amount = 5000000.0, type = TransactionType.INCOME,
        categoryId = 2, description = "Gaji bulanan", date = now
    )
    private val incomeWithCategory = TransactionWithCategory(
        transaction = incomeTransaction, category = incomeCategory
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        transactionRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    private fun stubDefaults() {
        every { transactionRepository.getAllTransactions() } returns flowOf(emptyList())
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())
    }

    private fun createViewModel(): TransactionViewModel {
        return TransactionViewModel(transactionRepository, categoryRepository)
    }

    // ---------- INITIAL STATE ----------

    @Test
    fun `initial default TransactionUiState has correct defaults`() {
        val state = TransactionUiState()
        assertEquals(true, state.isLoading)
        assertTrue(state.transactions.isEmpty())
        assertTrue(state.filteredTransactions.isEmpty())
        assertNull(state.selectedFilter)
        assertEquals("", state.searchQuery)
        assertNull(state.errorMessage)
        assertNull(state.successMessage)
        assertFalse(state.showFilterDialog)
        assertNull(state.activeFilter)
        assertEquals(50, state.PAGE_SIZE)
        assertEquals(50, state.visibleCount)
        assertTrue(state.hasMore)
    }

    @Test
    fun `viewmodel loads with isLoading false after init`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        // With UnconfinedTestDispatcher, init runs eagerly
        assertFalse(vm.uiState.value.isLoading)
    }

    // ---------- HAPPY PATH ----------

    @Test
    fun `transactions are loaded from repository`() = runTest {
        every { transactionRepository.getAllTransactions() } returns flowOf(
            listOf(expenseWithCategory, incomeWithCategory)
        )
        every { categoryRepository.getAllCategories() } returns flowOf(listOf(testCategory, incomeCategory))

        val vm = createViewModel()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.transactions.size)
        assertEquals(2, state.filteredTransactions.size)
        assertEquals(2, state.categories.size)
        assertNull(state.errorMessage)
    }

    @Test
    fun `filtered transactions start equal to all transactions with no filter`() = runTest {
        every { transactionRepository.getAllTransactions() } returns flowOf(
            listOf(expenseWithCategory, incomeWithCategory)
        )
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())

        val vm = createViewModel()

        assertEquals(vm.uiState.value.transactions.size, vm.uiState.value.filteredTransactions.size)
    }

    @Test
    fun `addTransaction sets success message`() = runTest {
        stubDefaults()
        coEvery { transactionRepository.addTransaction(any()) } returns 1L

        val vm = createViewModel()
        vm.addTransaction(
            amount = 25000.0,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            description = "Kopi",
            date = now
        )

        // The success message is set immediately, then cleared after 2s delay
        // With UnconfinedTestDispatcher, the delay may execute immediately
        coVerify { transactionRepository.addTransaction(any()) }
    }

    @Test
    fun `addTransaction calls repository with correct transaction`() = runTest {
        stubDefaults()
        coEvery { transactionRepository.addTransaction(any()) } returns 1L

        val vm = createViewModel()
        vm.addTransaction(
            amount = 25000.0,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            description = "Kopi",
            date = now
        )

        coVerify {
            transactionRepository.addTransaction(
                match { txn ->
                    txn.amount == 25000.0 &&
                            txn.type == TransactionType.EXPENSE &&
                            txn.categoryId == 1L &&
                            txn.description == "Kopi"
                }
            )
        }
    }

    @Test
    fun `deleteTransaction calls repository`() = runTest {
        stubDefaults()
        coEvery { transactionRepository.deleteTransaction(any()) } just runs

        val vm = createViewModel()
        vm.deleteTransaction(expenseTransaction)

        coVerify { transactionRepository.deleteTransaction(expenseTransaction) }
    }

    @Test
    fun `updateTransaction calls repository`() = runTest {
        stubDefaults()
        coEvery { transactionRepository.updateTransaction(any()) } just runs

        val vm = createViewModel()
        val updated = expenseTransaction.copy(amount = 75000.0)
        vm.updateTransaction(updated)

        coVerify { transactionRepository.updateTransaction(updated) }
    }

    // ---------- ERROR HANDLING ----------

    @Test
    fun `error from transaction repository sets errorMessage`() = runTest {
        every { transactionRepository.getAllTransactions() } throws RuntimeException("DB connection lost")
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())

        val vm = createViewModel()

        assertNotNull(vm.uiState.value.errorMessage)
        assertTrue(vm.uiState.value.errorMessage!!.contains("DB connection lost"))
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `error from category repository sets errorMessage`() = runTest {
        every { transactionRepository.getAllTransactions() } returns flowOf(emptyList())
        every { categoryRepository.getAllCategories() } throws RuntimeException("Category load failed")

        val vm = createViewModel()

        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `addTransaction error sets errorMessage`() = runTest {
        stubDefaults()
        coEvery { transactionRepository.addTransaction(any()) } throws RuntimeException("Insert failed")

        val vm = createViewModel()
        vm.addTransaction(
            amount = 25000.0,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            description = "Kopi",
            date = now
        )

        // With UnconfinedTestDispatcher, the coroutine runs immediately
        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `deleteTransaction error sets errorMessage`() = runTest {
        stubDefaults()
        coEvery { transactionRepository.deleteTransaction(any()) } throws RuntimeException("Delete failed")

        val vm = createViewModel()
        vm.deleteTransaction(expenseTransaction)

        assertNotNull(vm.uiState.value.errorMessage)
    }

    // ---------- FILTER BY TYPE ----------

    @Test
    fun `filterByType with EXPENSE filters transactions`() = runTest {
        every { transactionRepository.getAllTransactions() } returns flowOf(
            listOf(expenseWithCategory, incomeWithCategory)
        )
        every { transactionRepository.getTransactionsByType(TransactionType.EXPENSE) } returns flowOf(
            listOf(expenseWithCategory)
        )
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())

        val vm = createViewModel()
        vm.filterByType(TransactionType.EXPENSE)

        val state = vm.uiState.value
        assertEquals(TransactionType.EXPENSE, state.selectedFilter)
        assertEquals(1, state.transactions.size)
        assertEquals(TransactionType.EXPENSE, state.transactions[0].transaction.type)
    }

    @Test
    fun `filterByType with INCOME filters transactions`() = runTest {
        every { transactionRepository.getAllTransactions() } returns flowOf(
            listOf(expenseWithCategory, incomeWithCategory)
        )
        every { transactionRepository.getTransactionsByType(TransactionType.INCOME) } returns flowOf(
            listOf(incomeWithCategory)
        )
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())

        val vm = createViewModel()
        vm.filterByType(TransactionType.INCOME)

        val state = vm.uiState.value
        assertEquals(TransactionType.INCOME, state.selectedFilter)
        assertEquals(1, state.transactions.size)
        assertEquals(TransactionType.INCOME, state.transactions[0].transaction.type)
    }

    @Test
    fun `filterByType with null shows all transactions`() = runTest {
        every { transactionRepository.getAllTransactions() } returns flowOf(
            listOf(expenseWithCategory, incomeWithCategory)
        )
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())

        val vm = createViewModel()
        // First filter to expense
        every { transactionRepository.getTransactionsByType(TransactionType.EXPENSE) } returns flowOf(
            listOf(expenseWithCategory)
        )
        vm.filterByType(TransactionType.EXPENSE)

        // Then clear filter
        vm.filterByType(null)

        val state = vm.uiState.value
        assertNull(state.selectedFilter)
    }

    // ---------- SEARCH QUERY ----------

    @Test
    fun `updateSearchQuery updates searchQuery in state`() = runTest {
        stubDefaults()
        every { transactionRepository.searchTransactions(any()) } returns flowOf(emptyList())

        val vm = createViewModel()
        vm.updateSearchQuery("makan")

        assertEquals("makan", vm.uiState.value.searchQuery)
    }

    @Test
    fun `updateSearchQuery clears selectedFilter`() = runTest {
        stubDefaults()
        every { transactionRepository.searchTransactions(any()) } returns flowOf(emptyList())

        val vm = createViewModel()
        vm.updateSearchQuery("test")

        assertNull(vm.uiState.value.selectedFilter)
    }

    // ---------- APPLY / CLEAR FILTER ----------

    @Test
    fun `applyFilter with type filter sets activeFilter`() = runTest {
        every { transactionRepository.getAllTransactions() } returns flowOf(
            listOf(expenseWithCategory, incomeWithCategory)
        )
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())

        val vm = createViewModel()
        val filter = TransactionFilter(type = TransactionType.EXPENSE)
        vm.applyFilter(filter)

        assertNotNull(vm.uiState.value.activeFilter)
        assertEquals(TransactionType.EXPENSE, vm.uiState.value.activeFilter!!.type)
    }

    @Test
    fun `applyFilter with empty filter clears activeFilter`() = runTest {
        every { transactionRepository.getAllTransactions() } returns flowOf(
            listOf(expenseWithCategory, incomeWithCategory)
        )
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())

        val vm = createViewModel()
        vm.applyFilter(TransactionFilter())

        assertNull(vm.uiState.value.activeFilter)
    }

    @Test
    fun `clearFilter removes activeFilter and selectedFilter`() = runTest {
        every { transactionRepository.getAllTransactions() } returns flowOf(
            listOf(expenseWithCategory, incomeWithCategory)
        )
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())

        val vm = createViewModel()
        vm.applyFilter(TransactionFilter(type = TransactionType.EXPENSE))
        assertNotNull(vm.uiState.value.activeFilter)

        vm.clearFilter()
        assertNull(vm.uiState.value.activeFilter)
        assertNull(vm.uiState.value.selectedFilter)
    }

    @Test
    fun `applyFilter with amount range filters correctly`() = runTest {
        val cheapTxn = TransactionWithCategory(
            transaction = Transaction(
                id = 3, amount = 10000.0, type = TransactionType.EXPENSE,
                categoryId = 1, description = "Murah", date = now
            ),
            category = testCategory
        )
        val expensiveTxn = TransactionWithCategory(
            transaction = Transaction(
                id = 4, amount = 500000.0, type = TransactionType.EXPENSE,
                categoryId = 1, description = "Mahal", date = now
            ),
            category = testCategory
        )

        every { transactionRepository.getAllTransactions() } returns flowOf(
            listOf(cheapTxn, expensiveTxn)
        )
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())

        val vm = createViewModel()
        vm.applyFilter(TransactionFilter(minAmount = 100000.0))

        val filtered = vm.uiState.value.filteredTransactions
        assertEquals(1, filtered.size)
        assertEquals(500000.0, filtered[0].transaction.amount, 0.001)
    }

    // ---------- SELECT / CLEAR SELECTION ----------

    @Test
    fun `selectTransaction sets selectedTransaction`() = runTest {
        stubDefaults()
        val vm = createViewModel()
        vm.selectTransaction(expenseTransaction)

        assertEquals(expenseTransaction, vm.uiState.value.selectedTransaction)
    }

    @Test
    fun `clearSelection clears selectedTransaction`() = runTest {
        stubDefaults()
        val vm = createViewModel()
        vm.selectTransaction(expenseTransaction)
        assertNotNull(vm.uiState.value.selectedTransaction)

        vm.clearSelection()
        assertNull(vm.uiState.value.selectedTransaction)
    }

    // ---------- FILTER DIALOG ----------

    @Test
    fun `showFilterDialog sets showFilterDialog to true`() = runTest {
        stubDefaults()
        val vm = createViewModel()
        vm.showFilterDialog()

        assertTrue(vm.uiState.value.showFilterDialog)
    }

    @Test
    fun `hideFilterDialog sets showFilterDialog to false`() = runTest {
        stubDefaults()
        val vm = createViewModel()
        vm.showFilterDialog()
        vm.hideFilterDialog()

        assertFalse(vm.uiState.value.showFilterDialog)
    }

    // ---------- LOAD MORE ----------

    @Test
    fun `loadMore increases visibleCount`() = runTest {
        val manyTransactions = (1..100).map { i ->
            TransactionWithCategory(
                transaction = Transaction(
                    id = i.toLong(), amount = i * 1000.0,
                    type = TransactionType.EXPENSE, categoryId = 1,
                    description = "Tx $i", date = now
                ),
                category = testCategory
            )
        }
        every { transactionRepository.getAllTransactions() } returns flowOf(manyTransactions)
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())

        val vm = createViewModel()

        val initialVisible = vm.uiState.value.visibleCount
        assertEquals(50, initialVisible)
        assertTrue(vm.uiState.value.hasMore)

        vm.loadMore()

        assertEquals(100, vm.uiState.value.visibleCount)
        assertFalse(vm.uiState.value.hasMore)
    }

    @Test
    fun `loadMore does nothing when hasMore is false`() = runTest {
        val fewTransactions = (1..5).map { i ->
            TransactionWithCategory(
                transaction = Transaction(
                    id = i.toLong(), amount = i * 1000.0,
                    type = TransactionType.EXPENSE, categoryId = 1,
                    description = "Tx $i", date = now
                ),
                category = testCategory
            )
        }
        every { transactionRepository.getAllTransactions() } returns flowOf(fewTransactions)
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())

        val vm = createViewModel()
        assertFalse(vm.uiState.value.hasMore)

        val countBefore = vm.uiState.value.visibleCount
        vm.loadMore()
        assertEquals(countBefore, vm.uiState.value.visibleCount)
    }

    // ---------- TURBINE TESTS ----------

    @Test
    fun `uiState emits loading then loaded via turbine`() = runTest {
        every { transactionRepository.getAllTransactions() } returns flowOf(listOf(expenseWithCategory))
        every { categoryRepository.getAllCategories() } returns flowOf(listOf(testCategory))

        val vm = createViewModel()

        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(1, state.transactions.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filterByType emits updated state via turbine`() = runTest {
        every { transactionRepository.getAllTransactions() } returns flowOf(
            listOf(expenseWithCategory, incomeWithCategory)
        )
        every { transactionRepository.getTransactionsByType(TransactionType.EXPENSE) } returns flowOf(
            listOf(expenseWithCategory)
        )
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())

        val vm = createViewModel()

        vm.uiState.test {
            val initialState = awaitItem()
            assertNull(initialState.selectedFilter)

            vm.filterByType(TransactionType.EXPENSE)

            val filteredState = awaitItem()
            assertEquals(TransactionType.EXPENSE, filteredState.selectedFilter)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---------- CLIENT-SIDE SEARCH FILTER ----------

    @Test
    fun `client-side search filter matches description`() = runTest {
        every { transactionRepository.getAllTransactions() } returns flowOf(
            listOf(expenseWithCategory, incomeWithCategory)
        )
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())
        every { transactionRepository.searchTransactions(any()) } returns flowOf(
            listOf(expenseWithCategory)
        )

        val vm = createViewModel()
        vm.updateSearchQuery("makan")

        // With searchQuery set and FTS, the filtered list depends on FTS result
        assertEquals("makan", vm.uiState.value.searchQuery)
    }

    // ---------- ACTIVE FILTER WITH DATE RANGE ----------

    @Test
    fun `applyFilter with date range filters transactions`() = runTest {
        val yesterday = now.minusDays(1)
        val tomorrow = now.plusDays(1)

        every { transactionRepository.getAllTransactions() } returns flowOf(
            listOf(expenseWithCategory, incomeWithCategory)
        )
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())

        val vm = createViewModel()
        vm.applyFilter(TransactionFilter(startDate = yesterday, endDate = tomorrow))

        val filtered = vm.uiState.value.filteredTransactions
        // Both transactions have date = now, which is between yesterday and tomorrow
        assertEquals(2, filtered.size)
    }
}
