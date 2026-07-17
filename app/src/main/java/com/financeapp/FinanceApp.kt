package com.financeapp

import android.app.Application
import com.financeapp.data.database.FinanceDatabase
import com.financeapp.data.repository.BudgetRepository
import com.financeapp.data.repository.CategoryRepository
import com.financeapp.data.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FinanceApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: FinanceDatabase
        private set
    lateinit var transactionRepository: TransactionRepository
        private set
    lateinit var categoryRepository: CategoryRepository
        private set
    lateinit var budgetRepository: BudgetRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = FinanceDatabase.getInstance(this)
        transactionRepository = TransactionRepository(database.transactionDao())
        categoryRepository = CategoryRepository(database.categoryDao())
        budgetRepository = BudgetRepository(
            database.budgetDao(),
            database.transactionDao(),
            categoryRepository
        )

        // Initialize default categories
        applicationScope.launch {
            categoryRepository.initializeDefaultCategories()
        }
    }
}
