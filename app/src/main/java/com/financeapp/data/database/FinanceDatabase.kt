package com.financeapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.financeapp.data.model.Achievement
import com.financeapp.data.model.Budget
import com.financeapp.data.model.Account
import com.financeapp.data.model.Category
import com.financeapp.data.model.Transaction
import com.financeapp.data.model.TransactionFts
import com.financeapp.data.model.UserProgress
import com.financeapp.data.model.DailyQuest
import com.financeapp.data.model.Challenge
import com.financeapp.data.model.XpHistory
import com.financeapp.data.model.RecurringTransaction

@Database(
    entities = [Transaction::class, Category::class, Budget::class, Account::class, Achievement::class,
               UserProgress::class, DailyQuest::class, Challenge::class, XpHistory::class,
               TransactionFts::class, RecurringTransaction::class],
    version = 12,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun accountDao(): AccountDao
    abstract fun achievementDao(): AchievementDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun dailyQuestDao(): DailyQuestDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun xpHistoryDao(): XpHistoryDao
    abstract fun transactionFtsDao(): TransactionFtsDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao

    companion object {
        internal val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migration from v1 to v2 - add Budget table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS budgets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        categoryId INTEGER NOT NULL,
                        monthlyLimit REAL NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        alertThreshold REAL NOT NULL DEFAULT 80.0,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        createdAt TEXT NOT NULL,
                        updatedAt TEXT NOT NULL,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS index_budgets_categoryId ON budgets(categoryId)")
            }
        }

        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migration from v2 to v3 - add Account table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS accounts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL DEFAULT 'CASH',
                        balance REAL NOT NULL DEFAULT 0.0,
                        icon TEXT NOT NULL DEFAULT 'account_balance_wallet',
                        color TEXT NOT NULL DEFAULT '#4CAF50',
                        isDefault INTEGER NOT NULL DEFAULT 0,
                        createdAt TEXT NOT NULL
                    )
                """)
            }
        }

        internal val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migration from v3 to v4 - add Achievement table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS achievements (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        icon TEXT NOT NULL DEFAULT 'emoji_events',
                        category TEXT NOT NULL,
                        targetValue INTEGER NOT NULL DEFAULT 0,
                        currentValue INTEGER NOT NULL DEFAULT 0,
                        isUnlocked INTEGER NOT NULL DEFAULT 0,
                        unlockedAt TEXT
                    )
                """)
            }
        }

        internal val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migration from v4 to v5 - add accountId to transactions
                database.execSQL("ALTER TABLE transactions ADD COLUMN accountId INTEGER DEFAULT 1")
            }
        }

        internal val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migration from v5 to v6 - schema refinements
                // This version keeps all existing data
            }
        }

        internal val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Gamification tables
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_progress (
                        id INTEGER PRIMARY KEY NOT NULL,
                        totalXp INTEGER NOT NULL DEFAULT 0,
                        currentLevel INTEGER NOT NULL DEFAULT 1,
                        bestStreak INTEGER NOT NULL DEFAULT 0,
                        currentStreak INTEGER NOT NULL DEFAULT 0,
                        streakFreezes INTEGER NOT NULL DEFAULT 1,
                        lastActivityDate TEXT,
                        healthScore REAL NOT NULL DEFAULT 0.0,
                        updatedAt TEXT NOT NULL
                    )
                """)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS daily_quests (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        xpReward INTEGER NOT NULL,
                        questType TEXT NOT NULL,
                        targetValue INTEGER NOT NULL,
                        currentValue INTEGER NOT NULL DEFAULT 0,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        questDate TEXT NOT NULL
                    )
                """)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS challenges (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        xpReward INTEGER NOT NULL,
                        challengeType TEXT NOT NULL,
                        startDate TEXT NOT NULL,
                        endDate TEXT NOT NULL,
                        targetType TEXT NOT NULL,
                        targetValue INTEGER NOT NULL,
                        currentValue INTEGER NOT NULL DEFAULT 0,
                        isCompleted INTEGER NOT NULL DEFAULT 0
                    )
                """)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS xp_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amount INTEGER NOT NULL,
                        source TEXT NOT NULL,
                        description TEXT NOT NULL,
                        createdAt TEXT NOT NULL
                    )
                """)
            }
        }

        internal val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // FTS4 virtual table for full-text search on transaction descriptions
                database.execSQL("""
                    CREATE VIRTUAL TABLE IF NOT EXISTS `transactions_fts` USING FTS4(
                        `description`,
                        content='transactions',
                        content_rowid='id'
                    )
                """)
                // Populate FTS index from existing transactions
                database.execSQL("""
                    INSERT INTO transactions_fts(rowid, description)
                    SELECT id, description FROM transactions
                """)
                // Triggers to keep FTS index in sync
                database.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS room_fts_auto_update_transactions_insert
                    AFTER INSERT ON transactions BEGIN
                        INSERT INTO transactions_fts(rowid, description)
                        VALUES (new.id, new.description);
                    END
                """)
                database.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS room_fts_auto_update_transactions_delete
                    AFTER DELETE ON transactions BEGIN
                        INSERT INTO transactions_fts(transactions_fts, rowid, description)
                        VALUES('delete', old.id, old.description);
                    END
                """)
                database.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS room_fts_auto_update_transactions_update
                    AFTER UPDATE ON transactions BEGIN
                        INSERT INTO transactions_fts(transactions_fts, rowid, description)
                        VALUES('delete', old.id, old.description);
                        INSERT INTO transactions_fts(rowid, description)
                        VALUES (new.id, new.description);
                    END
                """)
            }
        }

        internal val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `recurring_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `amount` REAL NOT NULL,
                        `description` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `categoryId` INTEGER NOT NULL,
                        `accountId` INTEGER NOT NULL DEFAULT 1,
                        `interval` TEXT NOT NULL,
                        `intervalValue` INTEGER NOT NULL DEFAULT 1,
                        `startDate` TEXT NOT NULL,
                        `endDate` TEXT,
                        `endType` TEXT NOT NULL DEFAULT 'NEVER',
                        `maxOccurrences` INTEGER NOT NULL DEFAULT 0,
                        `occurrencesGenerated` INTEGER NOT NULL DEFAULT 0,
                        `nextDueDate` TEXT NOT NULL,
                        `isActive` INTEGER NOT NULL DEFAULT 1,
                        `createdAt` TEXT NOT NULL,
                        FOREIGN KEY (`categoryId`) REFERENCES `categories`(`id`) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_categoryId` ON `recurring_transactions` (`categoryId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_nextDueDate` ON `recurring_transactions` (`nextDueDate`)")
            }
        }

        internal val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_accountId` ON `transactions` (`accountId`)")
            }
        }

        internal val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_quests_questDate` ON `daily_quests` (`questDate`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_date` ON `transactions` (`date`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_accountId` ON `recurring_transactions` (`accountId`)")
            }
        }

        internal val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add DEFAULT '' to createdAt/updatedAt columns that were NOT NULL without DEFAULT
                // SQLite doesn't support ALTER COLUMN, so we rebuild each table.

                // budgets: createdAt, updatedAt
                database.execSQL("""
                    CREATE TABLE budgets_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        categoryId INTEGER NOT NULL,
                        monthlyLimit REAL NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        alertThreshold REAL NOT NULL DEFAULT 80.0,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        createdAt TEXT NOT NULL DEFAULT '',
                        updatedAt TEXT NOT NULL DEFAULT '',
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE CASCADE
                    )
                """)
                database.execSQL("INSERT INTO budgets_new SELECT * FROM budgets")
                database.execSQL("DROP TABLE budgets")
                database.execSQL("ALTER TABLE budgets_new RENAME TO budgets")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_budgets_categoryId ON budgets(categoryId)")

                // user_progress: updatedAt
                database.execSQL("""
                    CREATE TABLE user_progress_new (
                        id INTEGER PRIMARY KEY NOT NULL,
                        totalXp INTEGER NOT NULL DEFAULT 0,
                        currentLevel INTEGER NOT NULL DEFAULT 1,
                        bestStreak INTEGER NOT NULL DEFAULT 0,
                        currentStreak INTEGER NOT NULL DEFAULT 0,
                        streakFreezes INTEGER NOT NULL DEFAULT 1,
                        lastActivityDate TEXT,
                        healthScore REAL NOT NULL DEFAULT 0.0,
                        updatedAt TEXT NOT NULL DEFAULT ''
                    )
                """)
                database.execSQL("INSERT INTO user_progress_new SELECT * FROM user_progress")
                database.execSQL("DROP TABLE user_progress")
                database.execSQL("ALTER TABLE user_progress_new RENAME TO user_progress")

                // xp_history: createdAt
                database.execSQL("""
                    CREATE TABLE xp_history_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amount INTEGER NOT NULL,
                        source TEXT NOT NULL,
                        description TEXT NOT NULL,
                        createdAt TEXT NOT NULL DEFAULT ''
                    )
                """)
                database.execSQL("INSERT INTO xp_history_new SELECT * FROM xp_history")
                database.execSQL("DROP TABLE xp_history")
                database.execSQL("ALTER TABLE xp_history_new RENAME TO xp_history")

                // recurring_transactions: createdAt
                database.execSQL("""
                    CREATE TABLE recurring_transactions_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amount REAL NOT NULL,
                        description TEXT NOT NULL,
                        type TEXT NOT NULL,
                        categoryId INTEGER NOT NULL,
                        accountId INTEGER NOT NULL DEFAULT 1,
                        interval TEXT NOT NULL,
                        intervalValue INTEGER NOT NULL DEFAULT 1,
                        startDate TEXT NOT NULL,
                        endDate TEXT,
                        endType TEXT NOT NULL DEFAULT 'NEVER',
                        maxOccurrences INTEGER NOT NULL DEFAULT 0,
                        occurrencesGenerated INTEGER NOT NULL DEFAULT 0,
                        nextDueDate TEXT NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        createdAt TEXT NOT NULL DEFAULT '',
                        FOREIGN KEY (categoryId) REFERENCES categories(id) ON DELETE CASCADE
                    )
                """)
                database.execSQL("INSERT INTO recurring_transactions_new SELECT * FROM recurring_transactions")
                database.execSQL("DROP TABLE recurring_transactions")
                database.execSQL("ALTER TABLE recurring_transactions_new RENAME TO recurring_transactions")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_categoryId ON recurring_transactions(categoryId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_nextDueDate ON recurring_transactions(nextDueDate)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_accountId ON recurring_transactions(accountId)")
            }
        }
    }
}
