# FinanceApp — Database Layer Audit Report
## Entity vs Migration vs Insert Path Comparison

**Scope:** 11 entities, 10 migrations, all DAO/raw SQL insert paths
**Method:** Column-by-column comparison of Kotlin entity, SQL migration, and every INSERT statement

---

## BUGS DITEMUKAN

| # | Tabel | Kolom Bermasalah | Lokasi | Jenis Bug | Severity | Fix |
|---|-------|-----------------|--------|-----------|----------|-----|
| 1 | accounts | createdAt | DatabaseModule.kt:67-71 | INSERT tanpa kolom NOT NULL | CRITICAL | Tambah `createdAt` ke INSERT atau tambah DEFAULT di migration |
| 2 | budgets | createdAt, updatedAt | FinanceDatabase.kt:58-59 | SQL NOT NULL tanpa DEFAULT | MEDIUM | Tambah `DEFAULT ''` atau `DEFAULT datetime('now')` di migration |
| 3 | user_progress | updatedAt | FinanceDatabase.kt:131 | SQL NOT NULL tanpa DEFAULT | LOW | Tambah `DEFAULT ''` di migration |
| 4 | xp_history | createdAt | FinanceDatabase.kt:168 | SQL NOT NULL tanpa DEFAULT | LOW | Tambah `DEFAULT ''` di migration |
| 5 | recurring_transactions | createdAt | FinanceDatabase.kt:235 | SQL NOT NULL tanpa DEFAULT | LOW | Tambah `DEFAULT ''` di migration |
| 6 | (DUPLICATE) | — | FinanceDatabase.kt:258-281 | getInstance() dead code | LOW | Hapus atau tandai @Deprecated |

---

## BUG #1 — CRITICAL: accounts.createdAt missing di INSERT

**Lokasi:** `di/DatabaseModule.kt:67-71`

**Bukti:**

Entity `Account.kt`:
```kotlin
val createdAt: LocalDateTime = LocalDateTime.now()  // NOT NULL, no SQL DEFAULT
```

Migration `MIGRATION_2_3`:
```sql
createdAt TEXT NOT NULL  -- NO DEFAULT
```

Insert di DatabaseModule.onCreate:
```sql
INSERT INTO accounts (name, type, balance, icon, color, isDefault) VALUES (?, ?, ?, ?, ?, ?)
-- MISSING: createdAt column!
```

**Impact:** Fresh install (database baru) akan crash dengan:
`android.database.sqlite.SQLiteConstraintException: NOT NULL constraint failed: accounts.createdAt`

**Kenapa belum terdeteksi:** Jika user sudah pernah install sebelum migration v2→v3 dijalankan, Room membuat tabel dari entity (bukan migration) dan mengisi createdAt dari Kotlin default. Bug ini hanya muncul di FRESH INSTALL yang melewati onCreate callback.

**Fix yang diusulkan:**
```kotlin
db.execSQL("INSERT INTO accounts (name, type, balance, icon, color, isDefault, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?)",
    arrayOf("Cash", "CASH", 0.0, "attach_money", "#4CAF50", 1, java.time.LocalDateTime.now().toString()))
```

---

## BUG #2 — MEDIUM: budgets createdAt/updatedAt tanpa SQL DEFAULT

**Lokasi:** `FinanceDatabase.kt:58-59` (MIGRATION_1_2)

**Bukti:**

Entity `Budget.kt`:
```kotlin
val createdAt: LocalDateTime = LocalDateTime.now()  // NOT NULL
val updatedAt: LocalDateTime = LocalDateTime.now()  // NOT NULL
```

Migration SQL:
```sql
createdAt TEXT NOT NULL,  -- NO DEFAULT
updatedAt TEXT NOT NULL   -- NO DEFAULT
```

**Impact:** Semua INSERT via @Insert aman (Kotlin default mengisi). Tapi raw SQL INSERT tanpa kolom ini akan crash. Saat ini tidak ada raw SQL INSERT ke budgets, jadi risiko rendah.

**Fix yang diusulkan:**
```sql
createdAt TEXT NOT NULL DEFAULT '',
updatedAt TEXT NOT NULL DEFAULT ''
```

---

## BUG #3-5 — LOW: NOT NULL tanpa DEFAULT (3 tabel lain)

Pola sama dengan Bug #2:

| Tabel | Kolom | Migration | File:Line |
|-------|-------|-----------|-----------|
| user_progress | updatedAt | MIGRATION_6_7 | FinanceDatabase.kt:131 |
| xp_history | createdAt | MIGRATION_6_7 | FinanceDatabase.kt:168 |
| recurring_transactions | createdAt | MIGRATION_8_9 | FinanceDatabase.kt:235 |

**Impact:** Semua aman karena hanya diisi via @Insert (Kotlin default). Risiko rendah.

---

## BUG #6 — DUPLICATE LOGIC: FinanceDatabase.getInstance() dead code

**Lokasi:** `FinanceDatabase.kt:258-281`

**Bukti:** `FinanceDatabase.getInstance()` membuat database dengan `addMigrations()`. Tapi production path adalah `DatabaseModule.provideFinanceDatabase()` (via Hilt). Tidak ada file yang memanggil `getInstance()` selain FinanceDatabase.kt sendiri.

**Impact:** Tidak ada bug, tapi dua path yang sama membingungkan dan bisa menyebabkan regression jika salah satu di-update tapi bukan yang lain.

**Fix yang diusulkan:** Hapus `getInstance()` atau tandai `@Deprecated`.

---

## VERIFIKASI PER TABEL

### 1. transactions ✅ CLEAN

| Kolom | Kotlin | SQL NOT NULL | SQL DEFAULT | Insert Path |
|-------|--------|-------------|-------------|-------------|
| id | Long = 0 | YES | AUTOINCREMENT | @Insert ✅ |
| amount | Double | YES | — | @Insert ✅ |
| type | TransactionType | YES | — | @Insert ✅ |
| categoryId | Long | YES | — | @Insert ✅ |
| description | String | YES | — | @Insert ✅ |
| date | LocalDateTime | YES | — | @Insert ✅ |
| createdAt | LocalDateTime = now() | YES | — | @Insert ✅ |
| accountId | Long = 1 | YES | DEFAULT 1 | @Insert ✅ |

Migration: v1 (entity-derived) + v4→v5 (ALTER ADD accountId)
Insert: TransactionDao @Insert — entity object, semua kolom terisi ✅

### 2. categories ✅ CLEAN

| Kolom | Kotlin | SQL NOT NULL | SQL DEFAULT | Insert Path |
|-------|--------|-------------|-------------|-------------|
| id | Long = 0 | YES | AUTOINCREMENT | @Insert + raw SQL ✅ |
| name | String | YES | — | @Insert + raw SQL ✅ |
| icon | String | YES | — | @Insert + raw SQL ✅ |
| iconName | String = "" | YES | — | @Insert + raw SQL ✅ |
| type | TransactionType | YES | — | @Insert + raw SQL ✅ |
| color | String | YES | — | @Insert + raw SQL ✅ |

Migration: v1 (entity-derived)
Insert: CategoryDao @Insert + DatabaseModule.onCreate raw SQL — semua kolom terisi ✅

### 3. budgets ✅ CLEAN (lihat Bug #2)

| Kolom | Kotlin | SQL NOT NULL | SQL DEFAULT | Insert Path |
|-------|--------|-------------|-------------|-------------|
| id | Long = 0 | YES | AUTOINCREMENT | @Insert ✅ |
| categoryId | Long | YES | — | @Insert ✅ |
| monthlyLimit | Double | YES | — | @Insert ✅ |
| description | String = "" | YES | DEFAULT '' | @Insert ✅ |
| alertThreshold | Double = 80.0 | YES | DEFAULT 80.0 | @Insert ✅ |
| isActive | Boolean = true | YES | DEFAULT 1 | @Insert ✅ |
| createdAt | LocalDateTime = now() | YES | ⚠️ NO DEFAULT | @Insert ✅ |
| updatedAt | LocalDateTime = now() | YES | ⚠️ NO DEFAULT | @Insert ✅ |

### 4. accounts ❌ BUG #1

| Kolom | Kotlin | SQL NOT NULL | SQL DEFAULT | Insert Path |
|-------|--------|-------------|-------------|-------------|
| id | Long = 0 | YES | AUTOINCREMENT | @Insert + raw SQL ✅ |
| name | String | YES | — | @Insert + raw SQL ✅ |
| type | AccountType | YES | DEFAULT 'CASH' | @Insert + raw SQL ✅ |
| balance | Double = 0.0 | YES | DEFAULT 0.0 | @Insert + raw SQL ✅ |
| icon | String | YES | DEFAULT 'account_balance_wallet' | @Insert + raw SQL ✅ |
| color | String | YES | DEFAULT '#4CAF50' | @Insert + raw SQL ✅ |
| isDefault | Boolean = false | YES | DEFAULT 0 | @Insert + raw SQL ✅ |
| createdAt | LocalDateTime = now() | YES | ⚠️ NO DEFAULT | @Insert ✅ / raw SQL ❌ MISSING |

### 5. achievements ✅ CLEAN

| Kolom | Kotlin | SQL NOT NULL | SQL DEFAULT | Insert Path |
|-------|--------|-------------|-------------|-------------|
| id | Long = 0 | YES | AUTOINCREMENT | @Insert + raw SQL ✅ |
| name | String | YES | — | @Insert + raw SQL ✅ |
| description | String | YES | DEFAULT '' | @Insert + raw SQL ✅ |
| icon | String | YES | DEFAULT 'emoji_events' | @Insert + raw SQL ✅ |
| category | String | YES | — | @Insert + raw SQL ✅ |
| targetValue | Int | YES | DEFAULT 0 | @Insert + raw SQL ✅ |
| currentValue | Int = 0 | YES | DEFAULT 0 | @Insert + raw SQL ✅ |
| isUnlocked | Boolean = false | YES | DEFAULT 0 | @Insert + raw SQL ✅ |
| unlockedAt | LocalDateTime? = null | NO | — | @Insert ✅ |

### 6. user_progress ✅ CLEAN (lihat Bug #3)

| Kolom | Kotlin | SQL NOT NULL | SQL DEFAULT | Insert Path |
|-------|--------|-------------|-------------|-------------|
| id | Long = 1 | YES | — (PK, no auto) | @Insert ✅ |
| totalXp | Int = 0 | YES | DEFAULT 0 | @Insert ✅ |
| currentLevel | Int = 1 | YES | DEFAULT 1 | @Insert ✅ |
| bestStreak | Int = 0 | YES | DEFAULT 0 | @Insert ✅ |
| currentStreak | Int = 0 | YES | DEFAULT 0 | @Insert ✅ |
| streakFreezes | Int = 1 | YES | DEFAULT 1 | @Insert ✅ |
| lastActivityDate | LocalDateTime? = null | NO | — | @Insert ✅ |
| healthScore | Double = 0.0 | YES | DEFAULT 0.0 | @Insert ✅ |
| updatedAt | LocalDateTime = now() | YES | ⚠️ NO DEFAULT | @Insert ✅ |

### 7. daily_quests ✅ CLEAN

| Kolom | Kotlin | SQL NOT NULL | SQL DEFAULT | Insert Path |
|-------|--------|-------------|-------------|-------------|
| id | Long = 0 | YES | AUTOINCREMENT | @Insert ✅ |
| name | String | YES | — | @Insert ✅ |
| description | String | YES | — | @Insert ✅ |
| xpReward | Int | YES | — | @Insert ✅ |
| questType | String | YES | — | @Insert ✅ |
| targetValue | Int | YES | — | @Insert ✅ |
| currentValue | Int = 0 | YES | DEFAULT 0 | @Insert ✅ |
| isCompleted | Boolean = false | YES | DEFAULT 0 | @Insert ✅ |
| questDate | LocalDate | YES | — | @Insert ✅ |

### 8. challenges ✅ CLEAN

| Kolom | Kotlin | SQL NOT NULL | SQL DEFAULT | Insert Path |
|-------|--------|-------------|-------------|-------------|
| id | Long = 0 | YES | AUTOINCREMENT | @Insert ✅ |
| name | String | YES | — | @Insert ✅ |
| description | String | YES | — | @Insert ✅ |
| xpReward | Int | YES | — | @Insert ✅ |
| challengeType | String | YES | — | @Insert ✅ |
| startDate | LocalDate | YES | — | @Insert ✅ |
| endDate | LocalDate | YES | — | @Insert ✅ |
| targetType | String | YES | — | @Insert ✅ |
| targetValue | Int | YES | — | @Insert ✅ |
| currentValue | Int = 0 | YES | DEFAULT 0 | @Insert ✅ |
| isCompleted | Boolean = false | YES | DEFAULT 0 | @Insert ✅ |

### 9. xp_history ✅ CLEAN (lihat Bug #4)

| Kolom | Kotlin | SQL NOT NULL | SQL DEFAULT | Insert Path |
|-------|--------|-------------|-------------|-------------|
| id | Long = 0 | YES | AUTOINCREMENT | @Insert ✅ |
| amount | Int | YES | — | @Insert ✅ |
| source | String | YES | — | @Insert ✅ |
| description | String | YES | — | @Insert ✅ |
| createdAt | LocalDateTime = now() | YES | ⚠️ NO DEFAULT | @Insert ✅ |

### 10. recurring_transactions ✅ CLEAN (lihat Bug #5)

| Kolom | Kotlin | SQL NOT NULL | SQL DEFAULT | Insert Path |
|-------|--------|-------------|-------------|-------------|
| id | Long = 0 | YES | AUTOINCREMENT | @Insert ✅ |
| amount | Double | YES | — | @Insert ✅ |
| description | String | YES | — | @Insert ✅ |
| type | TransactionType | YES | — | @Insert ✅ |
| categoryId | Long | YES | — | @Insert ✅ |
| accountId | Long = 1 | YES | DEFAULT 1 | @Insert ✅ |
| interval | RecurringInterval | YES | — | @Insert ✅ |
| intervalValue | Int = 1 | YES | DEFAULT 1 | @Insert ✅ |
| startDate | LocalDate | YES | — | @Insert ✅ |
| endDate | LocalDate? = null | NO | — | @Insert ✅ |
| endType | RecurringEndType | YES | DEFAULT 'NEVER' | @Insert ✅ |
| maxOccurrences | Int = 0 | YES | DEFAULT 0 | @Insert ✅ |
| occurrencesGenerated | Int = 0 | YES | DEFAULT 0 | @Insert ✅ |
| nextDueDate | LocalDate | YES | — | @Insert ✅ |
| isActive | Boolean = true | YES | DEFAULT 1 | @Insert ✅ |
| createdAt | LocalDateTime = now() | YES | ⚠️ NO DEFAULT | @Insert ✅ |

### 11. transactions_fts ✅ CLEAN

FTS4 virtual table, managed by Room triggers. No manual inserts.

---

## MIGRATION CHAIN INTEGRITY

| Migration | Status | Notes |
|-----------|--------|-------|
| v1→v2 | ✅ | Budget table, all columns match entity |
| v2→v3 | ✅ | Account table, all columns match entity |
| v3→v4 | ✅ | Achievement table, all columns match entity |
| v4→v5 | ✅ | ALTER TABLE transactions ADD accountId |
| v5→v6 | ⚠️ | No-op (empty migration) |
| v6→v7 | ✅ | UserProgress + DailyQuest + Challenge + XpHistory |
| v7→v8 | ✅ | FTS4 virtual table + triggers |
| v8→v9 | ✅ | RecurringTransaction table |
| v9→v10 | ✅ | Index on transactions.accountId |
| v10→v11 | ✅ | Indexes on daily_quests.questDate, transactions.date, recurring_transactions.accountId |

All migrations registered in both FinanceDatabase AND DatabaseModule ✅

---

## RINGKASAN

| Severity | Count | Status |
|----------|-------|--------|
| CRITICAL | 1 | accounts.createdAt missing di raw SQL INSERT |
| MEDIUM | 1 | budgets createdAt/updatedAt tanpa SQL DEFAULT |
| LOW | 4 | 3x NOT NULL tanpa DEFAULT + 1x dead code |

**Total: 6 temuan**
