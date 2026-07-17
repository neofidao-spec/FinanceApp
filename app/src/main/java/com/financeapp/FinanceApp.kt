package com.financeapp

import android.app.Application
import com.financeapp.data.database.FinanceDatabase
import com.financeapp.data.repository.CategoryRepository
import com.financeapp.data.repository.TransactionRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FinanceApp : Application() {
    lateinit var database: FinanceDatabase
    lateinit var transactionRepository: TransactionRepository
    lateinit var categoryRepository: CategoryRepository

    override fun onCreate() {
        super.onCreate()
        database = FinanceDatabase.getInstance(this)
        transactionRepository = TransactionRepository(database.transactionDao())
        categoryRepository = CategoryRepository(database.categoryDao())

        // Initialize default categories
        GlobalScope.launch {
            categoryRepository.initializeDefaultCategories()
        }
    }
}
