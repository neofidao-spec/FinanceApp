package com.financeapp.data.repository

import com.financeapp.data.database.AccountDao
import com.financeapp.data.model.Account
import com.financeapp.data.model.AccountType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private val accountDao: AccountDao
) {

    fun getAllAccounts(): Flow<List<Account>> = accountDao.getAll()

    suspend fun getAccountById(id: Long): Account? = accountDao.getById(id)

    suspend fun getDefaultAccount(): Account? = accountDao.getDefault()

    suspend fun insertAccount(account: Account): Long = accountDao.insert(account)

    suspend fun updateAccount(account: Account) = accountDao.update(account)

    suspend fun deleteAccount(account: Account) = accountDao.delete(account)

    suspend fun getAccountCount(): Int = accountDao.count()

    suspend fun initializeDefaultAccounts() {
        if (accountDao.count() == 0) {
            val defaults = listOf(
                Account(
                    name = "Cash",
                    type = AccountType.CASH,
                    icon = "ic_cash",
                    color = "#4CAF50",
                    isDefault = true
                ),
                Account(
                    name = "Bank Account",
                    type = AccountType.BANK,
                    icon = "ic_bank",
                    color = "#2196F3"
                ),
                Account(
                    name = "E-Wallet",
                    type = AccountType.EWALLET,
                    icon = "ic_ewallet",
                    color = "#FF9800"
                )
            )
            defaults.forEach { accountDao.insert(it) }
        }
    }
}
