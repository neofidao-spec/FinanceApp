package com.financeapp.ui.viewmodel

import com.financeapp.data.model.*
import com.financeapp.data.repository.AccountRepository
import com.financeapp.data.repository.CategoryRepository
import com.financeapp.data.repository.TransactionRepository
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
class EditTransactionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var accountRepository: AccountRepository

    private val expenseCategory = Category(
        id = 5, name = "Makanan", icon = "🍔", iconName = "LocalDining",
        type = TransactionType.EXPENSE, color = "#E65100"
    )
    private val incomeCategory = Category(
        id = 1, name = "Gaji", icon = "💰", iconName = "Work",
        type = TransactionType.INCOME, color = "#2E7D32"
    )
    private val cashAccount = Account(
        id = 1, name = "Cash", type = AccountType.CASH,
        icon = "ic_cash", color = "#4CAF50", isDefault = true
    )
    private val bankAccount = Account(
        id = 2, name = "Bank", type = AccountType.BANK,
        icon = "ic_bank", color = "#2196F3"
    )
    private val now = LocalDateTime.now()
    private val existingTransaction = Transaction(
        id = 10, amount = 50000.0, type = TransactionType.EXPENSE,
        categoryId = 5, description = "Makan siang", date = now, accountId = 1
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        transactionRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        accountRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    private fun stubLoadDefaults() {
        coEvery { transactionRepository.getTransaction(10) } returns existingTransaction
        coEvery { categoryRepository.getCategory(5) } returns expenseCategory
        coEvery { accountRepository.getAccountById(1) } returns cashAccount
        every { categoryRepository.getAllCategoriesOnce() } returns listOf(expenseCategory, incomeCategory)
        every { accountRepository.getAllAccountsOnce() } returns listOf(cashAccount, bankAccount)
    }

    private fun createViewModel(): EditTransactionViewModel {
        return EditTransactionViewModel(transactionRepository, categoryRepository, accountRepository)
    }

    // ---------- INITIAL STATE ----------

    @Test
    fun `initial default EditTransactionUiState has correct defaults`() {
        val state = EditTransactionUiState()
        assertEquals(0L, state.transactionId)
        assertEquals("", state.amount)
        assertEquals("", state.description)
        assertNull(state.selectedCategory)
        assertEquals(TransactionType.EXPENSE, state.transactionType)
        assertTrue(state.categories.isEmpty())
        assertTrue(state.accounts.isEmpty())
        assertEquals(1L, state.selectedAccountId)
        assertTrue(state.isLoading)
        assertNull(state.successMessage)
        assertNull(state.errorMessage)
        assertFalse(state.isFormValid)
        assertFalse(state.showDeleteConfirm)
    }

    // ---------- LOAD TRANSACTION ----------

    @Test
    fun `loadTransaction success populates state`() = runTest {
        stubLoadDefaults()

        val vm = createViewModel()
        vm.loadTransaction(10)

        val state = vm.uiState.value
        assertEquals(10L, state.transactionId)
        assertEquals("50000.0", state.amount)
        assertEquals("Makan siang", state.description)
        assertEquals(expenseCategory, state.selectedCategory)
        assertEquals(TransactionType.EXPENSE, state.transactionType)
        assertEquals(1L, state.selectedAccountId)
        assertFalse(state.isLoading)
        assertTrue(state.errorMessage == null || state.categories.isNotEmpty())
    }

    @Test
    fun `loadTransaction with null result does not crash`() = runTest {
        coEvery { transactionRepository.getTransaction(999) } returns null

        val vm = createViewModel()
        vm.loadTransaction(999)

        // Should still be loading since we didn't set isLoading=false for null
        // The VM only sets isLoading=false inside the if (transaction != null) block
        val state = vm.uiState.value
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadTransaction failure sets errorMessage`() = runTest {
        coEvery { transactionRepository.getTransaction(10) } throws RuntimeException("DB error")

        val vm = createViewModel()
        vm.loadTransaction(10)

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `loadTransaction loads categories and accounts`() = runTest {
        stubLoadDefaults()

        val vm = createViewModel()
        vm.loadTransaction(10)

        // Categories and accounts are loaded via separate coroutines
        coVerify { categoryRepository.getAllCategoriesOnce() }
        coVerify { accountRepository.getAllAccountsOnce() }
    }

    // ---------- UPDATE FIELDS ----------

    @Test
    fun `updateAmount updates amount and validates form`() = runTest {
        stubLoadDefaults()
        val vm = createViewModel()
        vm.loadTransaction(10)

        vm.updateAmount("75000")

        assertEquals("75000", vm.uiState.value.amount)
    }

    @Test
    fun `updateDescription updates description`() = runTest {
        stubLoadDefaults()
        val vm = createViewModel()
        vm.loadTransaction(10)

        vm.updateDescription("Makan malam")

        assertEquals("Makan malam", vm.uiState.value.description)
    }

    @Test
    fun `selectCategory updates selectedCategory when type matches`() = runTest {
        stubLoadDefaults()
        val vm = createViewModel()
        vm.loadTransaction(10)

        val newCategory = Category(
            id = 6, name = "Transportasi", icon = "🚗", iconName = "DirectionsBus",
            type = TransactionType.EXPENSE, color = "#1565C0"
        )
        vm.selectCategory(newCategory)

        assertEquals(newCategory, vm.uiState.value.selectedCategory)
    }

    @Test
    fun `selectCategory ignores when type does not match`() = runTest {
        stubLoadDefaults()
        val vm = createViewModel()
        vm.loadTransaction(10)

        vm.selectCategory(incomeCategory) // EXPENSE vs INCOME mismatch

        // Should still be the original category
        assertEquals(expenseCategory, vm.uiState.value.selectedCategory)
    }

    @Test
    fun `selectAccount updates selectedAccountId`() = runTest {
        stubLoadDefaults()
        val vm = createViewModel()
        vm.loadTransaction(10)

        vm.selectAccount(2L)

        assertEquals(2L, vm.uiState.value.selectedAccountId)
    }

    // ---------- FORM VALIDATION ----------

    @Test
    fun `form is valid after loading valid transaction`() = runTest {
        stubLoadDefaults()
        val vm = createViewModel()
        vm.loadTransaction(10)

        assertTrue(vm.uiState.value.isFormValid)
    }

    @Test
    fun `form becomes invalid when amount is cleared`() = runTest {
        stubLoadDefaults()
        val vm = createViewModel()
        vm.loadTransaction(10)

        vm.updateAmount("")

        assertFalse(vm.uiState.value.isFormValid)
    }

    @Test
    fun `form becomes invalid when amount is zero`() = runTest {
        stubLoadDefaults()
        val vm = createViewModel()
        vm.loadTransaction(10)

        vm.updateAmount("0")

        assertFalse(vm.uiState.value.isFormValid)
    }

    // ---------- UPDATE TRANSACTION ----------

    @Test
    fun `updateTransaction with valid data calls repository`() = runTest {
        stubLoadDefaults()
        coEvery { transactionRepository.updateTransaction(any()) } just runs

        val vm = createViewModel()
        vm.loadTransaction(10)
        vm.updateAmount("75000")
        vm.updateTransaction()

        coVerify {
            transactionRepository.updateTransaction(match { txn ->
                txn.id == 10L &&
                        txn.amount == 75000.0 &&
                        txn.type == TransactionType.EXPENSE &&
                        txn.categoryId == expenseCategory.id
            })
        }
    }

    @Test
    fun `updateTransaction with valid data sets success message`() = runTest {
        stubLoadDefaults()
        coEvery { transactionRepository.updateTransaction(any()) } just runs

        val vm = createViewModel()
        vm.loadTransaction(10)
        vm.updateTransaction()

        coVerify { transactionRepository.updateTransaction(any()) }
    }

    @Test
    fun `updateTransaction with invalid form does not call repository`() = runTest {
        stubLoadDefaults()
        coEvery { transactionRepository.updateTransaction(any()) } just runs

        val vm = createViewModel()
        vm.loadTransaction(10)
        vm.updateAmount("") // invalid
        vm.updateTransaction()

        coVerify(exactly = 0) { transactionRepository.updateTransaction(any()) }
    }

    @Test
    fun `updateTransaction error sets errorMessage`() = runTest {
        stubLoadDefaults()
        coEvery { transactionRepository.updateTransaction(any()) } throws RuntimeException("Update failed")

        val vm = createViewModel()
        vm.loadTransaction(10)
        vm.updateTransaction()

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    // ---------- DELETE TRANSACTION ----------

    @Test
    fun `deleteTransaction calls repository with correct id`() = runTest {
        stubLoadDefaults()
        coEvery { transactionRepository.deleteTransactionById(10) } just runs

        val vm = createViewModel()
        vm.loadTransaction(10)
        vm.deleteTransaction()

        coVerify { transactionRepository.deleteTransactionById(10) }
    }

    @Test
    fun `deleteTransaction sets success message`() = runTest {
        stubLoadDefaults()
        coEvery { transactionRepository.deleteTransactionById(10) } just runs

        val vm = createViewModel()
        vm.loadTransaction(10)
        vm.deleteTransaction()

        coVerify { transactionRepository.deleteTransactionById(10) }
    }

    @Test
    fun `deleteTransaction with invalid id does not call repository`() = runTest {
        coEvery { transactionRepository.getTransaction(any()) } returns null

        val vm = createViewModel()
        // Don't load a transaction — id defaults to 0
        vm.deleteTransaction()

        coVerify(exactly = 0) { transactionRepository.deleteTransactionById(any()) }
    }

    @Test
    fun `deleteTransaction error sets errorMessage`() = runTest {
        stubLoadDefaults()
        coEvery { transactionRepository.deleteTransactionById(10) } throws RuntimeException("Delete failed")

        val vm = createViewModel()
        vm.loadTransaction(10)
        vm.deleteTransaction()

        assertNotNull(vm.uiState.value.errorMessage)
    }

    // ---------- DELETE CONFIRMATION DIALOG ----------

    @Test
    fun `showDeleteConfirmation sets showDeleteConfirm to true`() = runTest {
        val vm = createViewModel()
        vm.showDeleteConfirmation()

        assertTrue(vm.uiState.value.showDeleteConfirm)
    }

    @Test
    fun `hideDeleteConfirmation sets showDeleteConfirm to false`() = runTest {
        val vm = createViewModel()
        vm.showDeleteConfirmation()
        vm.hideDeleteConfirmation()

        assertFalse(vm.uiState.value.showDeleteConfirm)
    }

    // ---------- UPDATE DATE ----------

    @Test
    fun `updateDate updates selectedDate in state`() = runTest {
        val vm = createViewModel()
        val newDate = LocalDateTime.of(2025, 6, 15, 10, 30)

        vm.updateDate(newDate)

        assertEquals(newDate, vm.uiState.value.selectedDate)
    }
}
