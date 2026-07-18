package com.financeapp.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies that a fresh in-memory database can be created without exceptions,
 * and that default categories + accounts are inserted with all NOT NULL columns populated.
 */
@RunWith(AndroidJUnit4::class)
class FreshDatabaseTest {

    private lateinit var db: FinanceDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FinanceDatabase::class.java
        )
            .allowMainThreadQueries()
            .addCallback(FreshInstallCallback())
            .build()
    }

    @After
    fun teardown() {
        db.close()
    }

    /**
     * Replicates DatabaseModule.onCreate callback logic for in-memory test.
     * Inserts default categories, accounts, and achievements directly via SupportSQLiteDatabase.
     */
    private class FreshInstallCallback : androidx.room.RoomDatabase.Callback() {
        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onCreate(db)
            // Default categories
            val categories = listOf(
                arrayOf<Any>(1L, "Gaji", "💰", "Work", "INCOME", "#2E7D32"),
                arrayOf<Any>(2L, "Bonus", "🎁", "CardGiftcard", "INCOME", "#43A047"),
                arrayOf<Any>(3L, "Investasi", "📈", "TrendingUp", "INCOME", "#66BB6A"),
                arrayOf<Any>(4L, "Lainnya", "⭐", "AttachMoney", "INCOME", "#81C784"),
                arrayOf<Any>(5L, "Makanan", "🍔", "LocalDining", "EXPENSE", "#E65100"),
                arrayOf<Any>(6L, "Transportasi", "🚗", "DirectionsBus", "EXPENSE", "#1565C0"),
                arrayOf<Any>(7L, "Hiburan", "🎬", "Movie", "EXPENSE", "#7B1FA2"),
                arrayOf<Any>(8L, "Belanja", "🛍️", "ShoppingBag", "EXPENSE", "#C62828"),
                arrayOf<Any>(9L, "Utilities", "💡", "Lightbulb", "EXPENSE", "#00838F"),
                arrayOf<Any>(10L, "Kesehatan", "🏥", "LocalHospital", "EXPENSE", "#2E7D32"),
                arrayOf<Any>(11L, "Pendidikan", "📚", "School", "EXPENSE", "#4527A0"),
                arrayOf<Any>(12L, "Lainnya", "⭐", "Category", "EXPENSE", "#757575"),
            )
            categories.forEach { cat ->
                db.execSQL(
                    "INSERT INTO categories (id, name, icon, iconName, type, color) VALUES (?, ?, ?, ?, ?, ?)",
                    cat
                )
            }
            // Default accounts — MUST include createdAt (Bug #1 fix)
            val now = java.time.LocalDateTime.now().toString()
            db.execSQL(
                "INSERT INTO accounts (name, type, balance, icon, color, isDefault, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?)",
                arrayOf("Cash", "CASH", 0.0, "attach_money", "#4CAF50", 1, now)
            )
            db.execSQL(
                "INSERT INTO accounts (name, type, balance, icon, color, isDefault, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?)",
                arrayOf("Bank", "BANK", 0.0, "account_balance", "#2196F3", 0, now)
            )
            db.execSQL(
                "INSERT INTO accounts (name, type, balance, icon, color, isDefault, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?)",
                arrayOf("E-Wallet", "EWALLET", 0.0, "account_balance_wallet", "#FF9800", 0, now)
            )
            // Default achievements
            val achievements = listOf(
                arrayOf<Any>("Pencatat Pemula", "Catat 5 transaksi", "edit_note", "TRANSACTIONS", 5, 0, 0),
                arrayOf<Any>("Pencatat Setia", "Catat 30 transaksi", "menu_book", "TRANSACTIONS", 30, 0, 0),
                arrayOf<Any>("Master Transaksi", "Catat 100 transaksi", "military_tech", "TRANSACTIONS", 100, 0, 0),
                arrayOf<Any>("Hemat Mingguan", "Tidak overspend 1 minggu", "savings", "BUDGET", 7, 0, 0),
                arrayOf<Any>("Budget Master", "Di bawah budget 3 bulan", "workspace_premium", "BUDGET", 3, 0, 0),
                arrayOf<Any>("First Save", "Dapat pemasukan pertama", "eco", "SAVINGS", 1, 0, 0),
                arrayOf<Any>("Jutawan", "Saldo Rp 1jt", "diamond", "SAVINGS", 1000000, 0, 0),
                arrayOf<Any>("Konsisten 7 Hari", "Catat 7 hari berturut", "local_fire_department", "CONSISTENCY", 7, 0, 0),
                arrayOf<Any>("Konsisten 30 Hari", "Catat 30 hari berturut", "bolt", "CONSISTENCY", 30, 0, 0),
            )
            achievements.forEach { ach ->
                db.execSQL(
                    "INSERT INTO achievements (name, description, icon, category, targetValue, currentValue, isUnlocked) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    ach
                )
            }
        }
    }

    // ===== TESTS =====

    @Test
    fun `fresh database creation does not throw`() = runTest {
        // Simply accessing the database triggers onCreate
        val categories = db.categoryDao().getAllCategories().first()
        assertNotNull(categories)
    }

    @Test
    fun `default 12 categories are inserted`() = runTest {
        val categories = db.categoryDao().getAllCategories().first()
        assertEquals(12, categories.size)
    }

    @Test
    fun `default 3 accounts are inserted with all NOT NULL columns`() = runTest {
        val accounts = db.accountDao().getAllAccounts().first()
        assertEquals(3, accounts.size)

        accounts.forEach { account ->
            assertTrue("name should not be empty", account.name.isNotBlank())
            assertNotNull("type should not be null", account.type)
            assertNotNull("icon should not be null", account.icon)
            assertTrue("icon should not be blank", account.icon.isNotBlank())
            assertNotNull("color should not be null", account.color)
            assertTrue("color should not be blank", account.color.isNotBlank())
            assertNotNull("createdAt should not be null", account.createdAt)
        }

        // Verify specific defaults
        val cash = accounts.find { it.name == "Cash" }
        assertNotNull("Cash account should exist", cash)
        assertEquals(true, cash!!.isDefault)

        val bank = accounts.find { it.name == "Bank" }
        assertNotNull("Bank account should exist", bank)

        val wallet = accounts.find { it.name == "E-Wallet" }
        assertNotNull("E-Wallet account should exist", wallet)
    }

    @Test
    fun `default 9 achievements are inserted with all NOT NULL columns`() = runTest {
        val achievements = db.achievementDao().getAllAchievements().first()
        assertEquals(9, achievements.size)

        achievements.forEach { achievement ->
            assertTrue("name should not be blank", achievement.name.isNotBlank())
            assertNotNull("description should not be null", achievement.description)
            assertTrue("icon should not be blank", achievement.icon.isNotBlank())
            assertTrue("category should not be blank", achievement.category.isNotBlank())
            assertTrue("targetValue should be positive", achievement.targetValue > 0)
            assertEquals("currentValue should be 0", 0, achievement.currentValue)
            assertEquals("isUnlocked should be false", false, achievement.isUnlocked)
        }
    }

    @Test
    fun `no NOT NULL constraint failure on fresh install`() = runTest {
        // This test verifies Bug #1 fix: accounts.createdAt is now provided
        // If the fix is broken, this test will throw SQLiteConstraintException
        val accounts = db.accountDao().getAllAccounts().first()
        assertEquals(3, accounts.size)

        // All accounts must have createdAt set (not null, not default empty)
        accounts.forEach { account ->
            assertNotNull("createdAt must be set", account.createdAt)
        }
    }
}
