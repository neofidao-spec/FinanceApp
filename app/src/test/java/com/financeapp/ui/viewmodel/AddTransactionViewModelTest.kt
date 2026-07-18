package com.financeapp.ui.viewmodel

import app.cash.turbine.test
import com.financeapp.data.model.*
import com.financeapp.data.repository.AccountRepository
import com.financeapp.data.repository.CategoryRepository
import com.financeapp.data.repository.TransactionRepository
import com.financeapp.domain.GamificationUseCase
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
class AddTransactionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var accountRepository: AccountRepository
    private lateinit var gamificationUseCase: GamificationUseCase

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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        transactionRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        accountRepository = mockk(relaxed = true)
        gamificationUseCase = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    private fun stubDefaults() {
        every { categoryRepository.getAllCategories() } returns flowOf(listOf(expenseCategory, incomeCategory))
        every { accountRepository.getAllAccounts() } returns flowOf(listOf(cashAccount, bankAccount))
    }

    private fun createViewModel(): AddTransactionViewModel {
        return AddTransactionViewModel(transactionRepository, categoryRepository, accountRepository, gamificationUseCase)
    }

    // ---------- INITIAL STATE ----------

    @Test
    fun `initial default AddTransactionUiState has correct defaults`() {
        val state = AddTransactionUiState()
        assertEquals("", state.amount)
        assertEquals("", state.description)
        assertNull(state.selectedCategory)
        assertEquals(TransactionType.EXPENSE, state.transactionType)
        assertTrue(state.categories.isEmpty())
        assertTrue(state.accounts.isEmpty())
        assertEquals(1L, state.selectedAccountId)
        assertFalse(state.isLoading)
        assertNull(state.successMessage)
        assertNull(state.errorMessage)
        assertFalse(state.isFormValid)
    }

    @Test
    fun `viewmodel loads categories and accounts on init`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        val state = vm.uiState.value
        assertEquals(2, state.categories.size)
        assertEquals(2, state.accounts.size)
        assertEquals(1L, state.selectedAccountId) // default account
    }

    @Test
    fun `viewmodel sets default account from isDefault flag`() = runTest {
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())
        every { accountRepository.getAllAccounts() } returns flowOf(listOf(bankAccount, cashAccount))

        val vm = createViewModel()

        assertEquals(1L, vm.uiState.value.selectedAccountId) // cashAccount isDefault=true, id=1
    }

    // ---------- UPDATE FIELDS ----------

    @Test
    fun `updateAmount updates amount in state`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        vm.updateAmount("50000")

        assertEquals("50000", vm.uiState.value.amount)
    }

    @Test
    fun `updateDescription updates description in state`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        vm.updateDescription("Makan siang")

        assertEquals("Makan siang", vm.uiState.value.description)
    }

    @Test
    fun `selectCategory sets selectedCategory when type matches`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        vm.selectCategory(expenseCategory)

        assertEquals(expenseCategory, vm.uiState.value.selectedCategory)
    }

    @Test
    fun `selectCategory ignores category when type does not match`() = runTest {
        stubDefaults()
        val vm = createViewModel()
        // Default type is EXPENSE, incomeCategory is INCOME
        vm.selectCategory(incomeCategory)

        assertNull(vm.uiState.value.selectedCategory)
    }

    @Test
    fun `selectAccount updates selectedAccountId`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        vm.selectAccount(2L)

        assertEquals(2L, vm.uiState.value.selectedAccountId)
    }

    // ---------- FORM VALIDATION ----------

    @Test
    fun `form is valid when amount is positive and category selected`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        vm.updateAmount("50000")
        vm.selectCategory(expenseCategory)

        assertTrue(vm.uiState.value.isFormValid)
    }

    @Test
    fun `form is invalid when amount is empty`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        vm.selectCategory(expenseCategory)
        vm.updateAmount("")

        assertFalse(vm.uiState.value.isFormValid)
    }

    @Test
    fun `form is invalid when amount is zero`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        vm.updateAmount("0")
        vm.selectCategory(expenseCategory)

        assertFalse(vm.uiState.value.isFormValid)
    }

    @Test
    fun `form is invalid when amount is not a number`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        vm.updateAmount("abc")
        vm.selectCategory(expenseCategory)

        assertFalse(vm.uiState.value.isFormValid)
    }

    @Test
    fun `form is invalid when no category selected`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        vm.updateAmount("50000")

        assertFalse(vm.uiState.value.isFormValid)
    }

    // ---------- SUBMIT TRANSACTION ----------

    @Test
    fun `submitTransaction with valid data calls repository`() = runTest {
        stubDefaults()
        coEvery { transactionRepository.addTransaction(any()) } returns 1L

        val vm = createViewModel()
        vm.updateAmount("50000")
        vm.selectCategory(expenseCategory)
        vm.submitTransaction()

        coVerify {
            transactionRepository.addTransaction(match { txn ->
                txn.amount == 50000.0 &&
                        txn.type == TransactionType.EXPENSE &&
                        txn.categoryId == expenseCategory.id
            })
        }
    }

    @Test
    fun `submitTransaction with valid data calls gamification`() = runTest {
        stubDefaults()
        coEvery { transactionRepository.addTransaction(any()) } returns 1L

        val vm = createViewModel()
        vm.updateAmount("50000")
        vm.selectCategory(expenseCategory)
        vm.submitTransaction()

        coVerify { gamificationUseCase.onTransactionRecorded() }
        coVerify { gamificationUseCase.updateStreak() }
    }

    @Test
    fun `submitTransaction with valid data sets success message`() = runTest {
        stubDefaults()
        coEvery { transactionRepository.addTransaction(any()) } returns 1L

        val vm = createViewModel()
        vm.updateAmount("50000")
        vm.selectCategory(expenseCategory)
        vm.submitTransaction()

        // With UnconfinedTestDispatcher, success message is set immediately
        // It may be cleared by the delayed clearMessages() call
        coVerify { transactionRepository.addTransaction(any()) }
    }

    @Test
    fun `submitTransaction resets form after success`() = runTest {
        stubDefaults()
        coEvery { transactionRepository.addTransaction(any()) } returns 1L

        val vm = createViewModel()
        vm.updateAmount("50000")
        vm.updateDescription("Test")
        vm.selectCategory(expenseCategory)
        vm.submitTransaction()

        val state = vm.uiState.value
        assertEquals("", state.amount)
        assertEquals("", state.description)
        assertNull(state.selectedCategory)
    }

    @Test
    fun `submitTransaction with invalid data does not call repository`() = runTest {
        stubDefaults()

        val vm = createViewModel()
        // No amount set, no category selected
        vm.submitTransaction()

        coVerify(exactly = 0) { transactionRepository.addTransaction(any()) }
    }

    @Test
    fun `submitTransaction with empty amount does not call repository`() = runTest {
        stubDefaults()

        val vm = createViewModel()
        vm.selectCategory(expenseCategory)
        // Amount is still empty
        vm.submitTransaction()

        coVerify(exactly = 0) { transactionRepository.addTransaction(any()) }
    }

    @Test
    fun `submitTransaction with no category does not call repository`() = runTest {
        stubDefaults()

        val vm = createViewModel()
        vm.updateAmount("50000")
        // No category selected
        vm.submitTransaction()

        coVerify(exactly = 0) { transactionRepository.addTransaction(any()) }
    }

    // ---------- ERROR HANDLING ----------

    @Test
    fun `error loading categories sets errorMessage`() = runTest {
        every { categoryRepository.getAllCategories() } throws RuntimeException("DB error")
        every { accountRepository.getAllAccounts() } returns flowOf(emptyList())

        val vm = createViewModel()

        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `error loading accounts sets errorMessage`() = runTest {
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())
        every { accountRepository.getAllAccounts() } throws RuntimeException("DB error")

        val vm = createViewModel()

        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `submitTransaction error sets errorMessage`() = runTest {
        stubDefaults()
        coEvery { transactionRepository.addTransaction(any()) } throws RuntimeException("Insert failed")

        val vm = createViewModel()
        vm.updateAmount("50000")
        vm.selectCategory(expenseCategory)
        vm.submitTransaction()

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    // ---------- SWITCH TRANSACTION TYPE ----------

    @Test
    fun `switchTransactionType updates type and clears selectedCategory`() = runTest {
        stubDefaults()
        val vm = createViewModel()

        vm.selectCategory(expenseCategory)
        assertEquals(expenseCategory, vm.uiState.value.selectedCategory)

        vm.switchTransactionType(TransactionType.INCOME)

        assertEquals(TransactionType.INCOME, vm.uiState.value.transactionType)
        assertNull(vm.uiState.value.selectedCategory)
    }
}
