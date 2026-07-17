package com.financeapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.financeapp.data.database.BudgetDao
import com.financeapp.data.database.CategoryDao
import com.financeapp.data.database.Converters
import com.financeapp.data.database.FinanceDatabase
import com.financeapp.data.database.AccountDao
import com.financeapp.data.database.AchievementDao
import com.financeapp.data.database.TransactionDao
import com.financeapp.data.database.UserProgressDao
import com.financeapp.data.database.DailyQuestDao
import com.financeapp.data.database.ChallengeDao
import com.financeapp.data.database.XpHistoryDao
import com.financeapp.data.database.TransactionFtsDao
import com.financeapp.data.model.Category
import com.financeapp.data.model.DefaultAchievements
import com.financeapp.data.model.DefaultCategories
import com.financeapp.data.model.TransactionType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
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
        )
        .addMigrations(
            FinanceDatabase.MIGRATION_1_2,
            FinanceDatabase.MIGRATION_2_3,
            FinanceDatabase.MIGRATION_3_4,
            FinanceDatabase.MIGRATION_4_5,
            FinanceDatabase.MIGRATION_5_6,
            FinanceDatabase.MIGRATION_6_7,
            FinanceDatabase.MIGRATION_7_8
        )
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Insert default categories on first database creation
                Executors.newSingleThreadExecutor().execute {
                    val defaults = DefaultCategories.getDefault()
                    defaults.forEach { category ->
                        db.execSQL(
                            "INSERT INTO categories (id, name, icon, iconName, type, color) VALUES (?, ?, ?, ?, ?, ?)",
                            arrayOf(category.id, category.name, category.icon, category.iconName, category.type.name, category.color)
                        )
                    }
                    // Insert default accounts
                    db.execSQL("INSERT INTO accounts (name, type, balance, icon, color, isDefault) VALUES (?, ?, ?, ?, ?, ?)",
                        arrayOf("Cash", "CASH", 0.0, "💵", "#4CAF50", 1))
                    db.execSQL("INSERT INTO accounts (name, type, balance, icon, color, isDefault) VALUES (?, ?, ?, ?, ?, ?)",
                        arrayOf("Bank", "BANK", 0.0, "🏦", "#2196F3", 0))
                    db.execSQL("INSERT INTO accounts (name, type, balance, icon, color, isDefault) VALUES (?, ?, ?, ?, ?, ?)",
                        arrayOf("E-Wallet", "EWALLET", 0.0, "📱", "#FF9800", 0))
                    // Insert default achievements
                    val achievements = DefaultAchievements.getDefault()
                    achievements.forEach { achievement ->
                        db.execSQL(
                            "INSERT INTO achievements (name, description, icon, category, targetValue, currentValue, isUnlocked) VALUES (?, ?, ?, ?, ?, ?, ?)",
                            arrayOf(achievement.name, achievement.description, achievement.icon, achievement.category, achievement.targetValue, 0, 0)
                        )
                    }
                }
            }
        })
        .build()
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

    @Provides
    fun provideAccountDao(database: FinanceDatabase): AccountDao {
        return database.accountDao()
    }

    @Provides
    fun provideAchievementDao(database: FinanceDatabase): AchievementDao {
        return database.achievementDao()
    }

    @Provides
    fun provideUserProgressDao(database: FinanceDatabase): UserProgressDao {
        return database.userProgressDao()
    }

    @Provides
    fun provideDailyQuestDao(database: FinanceDatabase): DailyQuestDao {
        return database.dailyQuestDao()
    }

    @Provides
    fun provideChallengeDao(database: FinanceDatabase): ChallengeDao {
        return database.challengeDao()
    }

    @Provides
    fun provideXpHistoryDao(database: FinanceDatabase): XpHistoryDao {
        return database.xpHistoryDao()
    }

    @Provides
    fun provideTransactionFtsDao(database: FinanceDatabase): TransactionFtsDao {
        return database.transactionFtsDao()
    }
}
