package com.financeapp.di

import com.financeapp.data.database.BudgetDao
import com.financeapp.data.database.CategoryDao
import com.financeapp.data.database.TransactionDao
import com.financeapp.data.repository.BudgetRepository
import com.financeapp.data.repository.CategoryRepository
import com.financeapp.data.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTransactionRepository(transactionDao: TransactionDao): TransactionRepository {
        return TransactionRepository(transactionDao)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(categoryDao: CategoryDao): CategoryRepository {
        return CategoryRepository(categoryDao)
    }

    @Provides
    @Singleton
    fun provideBudgetRepository(
        budgetDao: BudgetDao,
        transactionDao: TransactionDao,
        categoryRepository: CategoryRepository
    ): BudgetRepository {
        return BudgetRepository(budgetDao, transactionDao, categoryRepository)
    }
}
