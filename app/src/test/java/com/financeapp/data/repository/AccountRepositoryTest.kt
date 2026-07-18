package com.financeapp.data.repository

import com.financeapp.data.database.AccountDao
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
class AccountRepositoryTest {

    private lateinit var dao: AccountDao
    private lateinit var repository: AccountRepository

    private val cashAccount = Account(
        id = 1, name = "Cash", type = AccountType.CASH,
        icon = "ic_cash", color = "#4CAF50", isDefault = true
    )
    private val bankAccount = Account(
        id = 2, name = "Bank Account", type = AccountType.BANK,
        icon = "ic_bank", color = "#2196F3"
    )

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        repository = AccountRepository(dao)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ---------- GET ALL ----------

    @Test
    fun `getAllAccounts returns flow from dao`() = runTest {
        every { dao.getAll() } returns flowOf(listOf(cashAccount, bankAccount))

        val result = repository.getAllAccounts().first()

        assertEquals(2, result.size)
        assertTrue(result.contains(cashAccount))
        assertTrue(result.contains(bankAccount))
    }

    @Test
    fun `getAllAccounts returns empty list when no accounts`() = runTest {
        every { dao.getAll() } returns flowOf(emptyList())

        val result = repository.getAllAccounts().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllAccountsOnce returns list from dao flow first`() = runTest {
        every { dao.getAll() } returns flowOf(listOf(cashAccount, bankAccount))

        val result = repository.getAllAccountsOnce()

        assertEquals(2, result.size)
    }

    // ---------- GET BY ID ----------

    @Test
    fun `getAccountById returns account from dao`() = runTest {
        coEvery { dao.getById(1) } returns cashAccount

        val result = repository.getAccountById(1)

        assertEquals(cashAccount, result)
    }

    @Test
    fun `getAccountById returns null for nonexistent id`() = runTest {
        coEvery { dao.getById(999) } returns null

        val result = repository.getAccountById(999)

        assertNull(result)
    }

    // ---------- GET DEFAULT ----------

    @Test
    fun `getDefaultAccount returns default account from dao`() = runTest {
        coEvery { dao.getDefault() } returns cashAccount

        val result = repository.getDefaultAccount()

        assertNotNull(result)
        assertTrue(result!!.isDefault)
    }

    @Test
    fun `getDefaultAccount returns null when no default exists`() = runTest {
        coEvery { dao.getDefault() } returns null

        val result = repository.getDefaultAccount()

        assertNull(result)
    }

    // ---------- INSERT ----------

    @Test
    fun `insertAccount delegates to dao insert and returns id`() = runTest {
        coEvery { dao.insert(any()) } returns 3L

        val id = repository.insertAccount(bankAccount)

        assertEquals(3L, id)
        coVerify { dao.insert(bankAccount) }
    }

    // ---------- UPDATE ----------

    @Test
    fun `updateAccount delegates to dao update`() = runTest {
        coEvery { dao.update(any()) } just runs

        repository.updateAccount(cashAccount)

        coVerify { dao.update(cashAccount) }
    }

    // ---------- DELETE ----------

    @Test
    fun `deleteAccount delegates to dao delete`() = runTest {
        coEvery { dao.delete(any()) } just runs

        repository.deleteAccount(bankAccount)

        coVerify { dao.delete(bankAccount) }
    }

    // ---------- COUNT ----------

    @Test
    fun `getAccountCount returns count from dao`() = runTest {
        coEvery { dao.count() } returns 3

        val count = repository.getAccountCount()

        assertEquals(3, count)
    }

    // ---------- INITIALIZE DEFAULTS ----------

    @Test
    fun `initializeDefaultAccounts inserts defaults when count is 0`() = runTest {
        coEvery { dao.count() } returns 0
        coEvery { dao.insert(any()) } returns 1L

        repository.initializeDefaultAccounts()

        // Should insert 3 default accounts (Cash, Bank, E-Wallet)
        coVerify(atLeast = 3) { dao.insert(any()) }
    }

    @Test
    fun `initializeDefaultAccounts does nothing when count is greater than 0`() = runTest {
        coEvery { dao.count() } returns 3

        repository.initializeDefaultAccounts()

        coVerify(exactly = 0) { dao.insert(any()) }
    }
}
