package com.financeapp.di

import android.content.Context
import androidx.room.Room
import com.financeapp.data.database.BudgetDao
import com.financeapp.data.database.CategoryDao
import com.financeapp.data.database.FinanceDatabase
import com.financeapp.data.database.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFinanceDatabase(@ApplicationContext context: Context): FinanceDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            FinanceDatabase::class.java,
            "finance_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideTransactionDao(database: FinanceDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideCategoryDao(database: FinanceDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideBudgetDao(database: FinanceDatabase): BudgetDao {
        return database.budgetDao()
    }
}
