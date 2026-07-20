# FinanceApp Source Code Audit Report

**Audit Date:** 2026-07-20
**Files Audited:** 111 Kotlin source files (models, DAOs, database, converters, repositories, use cases, generators, ViewModels, DI, preferences, utilities, seed data)
**Methodology:** Every source file read and analyzed for: null safety, race conditions, incorrect Flow usage, missing error handling, wrong calculations, stale state, memory leaks, data integrity issues.

---

## CRITICAL Issues (3)

### BUG-001: EditTransactionViewModel — Balance update uses wrong (post-update) transaction data
- **File:** `app/src/main/java/com/financeapp/ui/viewmodel/EditTransactionViewModel.kt`
- **Lines:** 167, 171
- **Severity:** CRITICAL
- **Description:** After updating a transaction (line 167), the code reads the transaction back from DB (line 171) to "revert" the old balance effect. But `getTransaction()` now returns the **already-updated** values, not the original. The "revert" applies the NEW amount instead of the OLD amount, so account balance is calculated incorrectly.
- **Example:** Old=EXPENSE 100, New=EXPENSE 200. Correct: balance +100 -200 = -100 net. Actual: balance +200 -200 = 0 net. The user's account balance is wrong by the full difference.
- **Fix:** Read the old transaction BEFORE calling `updateTransaction()`:
```kotlin
val oldTransaction = transactionRepository.getTransaction(transaction.id)
transactionRepository.updateTransaction(transaction)
// Then use oldTransaction for revert
```

### BUG-002: RecurringTransactionWorker — Duplicate transactions on worker interruption
- **File:** `app/src/main/java/com/financeapp/domain/RecurringTransactionWorker.kt`
- **Lines:** 53-54
- **Severity:** CRITICAL
- **Description:** `addTransaction()` and `advanceOccurrence()` are separate non-atomic calls. If the worker is killed (process death, system trim, ANR) between them, the transaction is created but the recurring schedule is NOT advanced. On next worker run, the same transaction is generated again — **duplicate financial transactions**.
- **Fix:** Make `addTransaction` + `advanceOccurrence` atomic in a single Room `@Transaction` method, or use idempotency keys (e.g., check for existing auto-transaction with same recurringId + date before inserting).

### BUG-003: RecurringTransactionRepository.advanceOccurrence — Non-atomic advance + deactivate
- **File:** `app/src/main/java/com/financeapp/data/repository/RecurringTransactionRepository.kt`
- **Lines:** 46-63
- **Severity:** CRITICAL
- **Description:** `advanceOccurrence` (line 47) and `deactivate` (lines 54, 62) are separate DAO calls. If the app crashes between them, a recurring transaction that should be deactivated continues generating transactions indefinitely. For `AFTER_COUNT` type, this means more transactions than the user configured.
- **Fix:** Combine `advanceOccurrence` and conditional `deactivate` into a single `@Transaction` DAO method.

---

## HIGH Issues (3)

### BUG-004: GamificationUseCase — Streak freeze granted repeatedly (exploitable)
- **File:** `app/src/main/java/com/financeapp/domain/GamificationUseCase.kt`
- **Lines:** 91-94, 127-128
- **Severity:** HIGH
- **Description:** When the user is already active today (line 91), `newStreak` equals the existing streak. But the freeze-granting logic at line 127 (`newStreak % 7 == 0`) runs unconditionally. On day 7, 14, 21, etc., every call to `updateStreak()` grants another freeze. Since `updateStreak()` is called on every transaction addition (AddTransactionViewModel line 167), adding multiple transactions on a multiple-of-7 streak day grants extra freezes (up to the cap of 3). This is exploitable.
- **Fix:** Only grant freezes when the streak **increases** (i.e., in the `lastActivity == today.minusDays(1)` branch):
```kotlin
val freezesGained = if (newStreak > 0 && newStreak % 7 == 0 
    && newStreak > progress.currentStreak) 1 else 0
```

### BUG-005: GamificationUseCase — Race condition between addXp and updateStreak
- **File:** `app/src/main/java/com/financeapp/domain/GamificationUseCase.kt`
- **Lines:** 74-141 (updateStreak), 159-184 (addXp)
- **Severity:** HIGH
- **Description:** `addXp()` is protected by `xpMutex`, but `updateStreak()` is NOT. Both perform read-modify-write on `UserProgress`. When called concurrently from different coroutines (e.g., `onTransactionRecorded()` from AddTransactionViewModel and `onDailyLogin()` from app startup), one write can overwrite the other, losing XP or streak data.
- **Fix:** Either use the same mutex for `updateStreak()`, or combine XP + streak updates into a single atomic operation under one lock.

### BUG-006: Transaction.amount uses Double — floating-point precision loss for financial data
- **Files:** `app/src/main/java/com/financeapp/data/model/Transaction.kt` (line 30), `Account.kt` (line 13), `Budget.kt` (line 25), `RecurringTransaction.kt` (line 43)
- **Severity:** HIGH
- **Description:** All monetary amounts use `Double` (IEEE 754 binary floating-point). Binary floats cannot represent decimal fractions exactly (e.g., `0.1 + 0.2 = 0.30000000000000004`). Over many transactions, rounding errors accumulate in sums, balances, and budget calculations. For a financial app, this is a significant data integrity risk.
- **Fix:** Use `BigDecimal` for storage, or store amounts in integer cents/satoshis (Long). At minimum, use `BigDecimal` at calculation boundaries.

---

## MEDIUM Issues (6)

### BUG-007: FTS query injection in TransactionRepository.searchTransactions
- **File:** `app/src/main/java/com/financeapp/data/repository/TransactionRepository.kt`
- **Line:** 22
- **Severity:** MEDIUM
- **Description:** User search input is directly interpolated into an FTS4 query (`"$query*"`). FTS4 special characters (`"`, `AND`, `OR`, `*`, `(`, `)`, `+`, `-`, `^`, `:`) are not escaped. A query like `foo"bar` or `test AND` will cause FTS syntax errors and crash or return unexpected results.
- **Fix:** Escape FTS special characters before interpolation:
```kotlin
fun searchTransactions(query: String): Flow<List<TransactionWithCategory>> {
    val sanitized = query.replace(Regex("[\"*+^():]"), " ").trim()
    val ftsQuery = "$sanitized*"
    return ftsDao?.search(ftsQuery) ?: dao.getAllWithCategory()
}
```

### BUG-008: Converters — Enum valueOf() crashes on unknown values
- **File:** `app/src/main/java/com/financeapp/data/database/Converters.kt`
- **Lines:** 45, 56, 67, 78, 89
- **Severity:** MEDIUM
- **Description:** All enum TypeConverters use `Enum.valueOf(value)` which throws `IllegalArgumentException` if the stored string doesn't match any enum constant. If the database has a stale enum value (e.g., after a code update removes/renames an enum), the entire app crashes on launch with no recovery path.
- **Fix:** Use safe parsing with fallback:
```kotlin
@TypeConverter
fun toTransactionType(value: String): TransactionType {
    return try { TransactionType.valueOf(value) } catch (_: IllegalArgumentException) {
        TransactionType.EXPENSE // safe default
    }
}
```

### BUG-009: CsvExporter loses decimal precision in amounts
- **File:** `app/src/main/java/com/financeapp/util/CsvExporter.kt`
- **Line:** 20
- **Severity:** MEDIUM
- **Description:** `String.format("%.0f", amount)` rounds amounts to 0 decimal places. Any transaction with decimal amounts (e.g., Rp 15.500,50) loses the decimal portion in the exported CSV. This is data loss for users who export their financial data.
- **Fix:** Use `"%.2f"` or match the locale's decimal formatting:
```kotlin
val amount = String.format(indonesiaLocale, "%,.2f", twc.transaction.amount)
```

### BUG-010: TransactionViewModel.undoDelete — re-inserted transaction gets new ID
- **File:** `app/src/main/java/com/financeapp/ui/viewmodel/TransactionViewModel.kt`
- **Line:** 318
- **Severity:** MEDIUM
- **Description:** When undoing a swipe-delete, `addTransaction()` calls `dao.insert()` which auto-generates a new ID. The original transaction ID is lost. Any external references to the old ID (budget tracking, recurring links, export records) become orphaned.
- **Fix:** Use `@Insert(onConflict = OnConflictStrategy.REPLACE)` with the original ID preserved, or create a `restoreTransaction` DAO method that inserts with the original primary key.

### BUG-011: GamificationViewModel.autoCompleteQuest — busy-wait polling
- **File:** `app/src/main/java/com/financeapp/ui/viewmodel/GamificationViewModel.kt`
- **Lines:** 179-183
- **Severity:** MEDIUM
- **Description:** `autoCompleteQuest` polls with `delay(100)` up to 50 times (5 seconds) waiting for quests to load. This is a spin-wait anti-pattern that wastes CPU and battery. If quests never load (network error, DB error), it wastes 5 seconds for nothing.
- **Fix:** Use `combine()` or `StateFlow` to reactively wait for quests to be loaded, or use a `CompletableDeferred` that the initialization signals.

### BUG-012: DashboardViewModel — redundant combine, wasted Flow collection
- **File:** `app/src/main/java/com/financeapp/ui/viewmodel/DashboardViewModel.kt`
- **Lines:** 67-70
- **Severity:** MEDIUM
- **Description:** The `combine` collects `budgetRepository.getActiveBudgets()` but ignores the result (parameter named `_`). This means every budget change triggers a full dashboard refresh including health score recalculation and monthly trend reload (6 DB queries per emission). Budget changes are frequent when budgets are added/edited, causing unnecessary heavy recomputation.
- **Fix:** Use `transactionRepository.getAllTransactions().distinctUntilChanged()` as the sole trigger, and load budgets separately only when needed.

---

## LOW Issues (8)

### BUG-013: Transaction.createdAt default uses LocalDateTime.now() at construction
- **File:** `app/src/main/java/com/financeapp/data/model/Transaction.kt`
- **Line:** 35
- **Severity:** LOW
- **Description:** `createdAt: LocalDateTime = LocalDateTime.now()` evaluates at object construction time, not at database insert time. If a Transaction object is created but inserted later, `createdAt` won't reflect the actual insert time.
- **Fix:** Use `@ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")` or set createdAt explicitly at insert time.

### BUG-014: Budget.isExceeded() uses strict greater-than
- **File:** `app/src/main/java/com/financeapp/data/model/Budget.kt`
- **Line:** 40
- **Severity:** LOW
- **Description:** `isExceeded()` uses `currentSpent > budget.monthlyLimit`. If spent equals limit exactly, it's NOT considered exceeded. Most users expect "at or over budget" to be flagged.
- **Fix:** Consider using `>=` for `isExceeded()`.

### BUG-015: UserProgressDao update methods accept String instead of LocalDateTime
- **File:** `app/src/main/java/com/financeapp/data/database/UserProgressDao.kt`
- **Lines:** 22, 25, 28
- **Severity:** LOW
- **Description:** `updateXpAndLevel`, `updateStreak`, `updateHealthScore` accept `updatedAt: String`. This bypasses type safety — callers must manually format dates. Currently works because callers use `LocalDateTime.now().toString()` which matches `ISO_LOCAL_DATE_TIME`, but this is fragile.
- **Fix:** Use `LocalDateTime` parameters and let Room's TypeConverters handle formatting.

### BUG-016: DailyQuestDao methods accept String instead of LocalDate
- **File:** `app/src/main/java/com/financeapp/data/database/DailyQuestDao.kt`
- **Lines:** 19, 22, 25, 31, 34
- **Severity:** LOW
- **Description:** Same pattern as BUG-015. DAO methods accept `date: String` instead of `LocalDate`, requiring manual conversion in repositories. Fragile and error-prone.
- **Fix:** Use `LocalDate` parameters.

### BUG-017: AchievementDao.unlock accepts String instead of LocalDateTime
- **File:** `app/src/main/java/com/financeapp/data/database/AchievementDao.kt`
- **Line:** 44
- **Severity:** LOW
- **Description:** Same pattern. `unlock(id: Long, unlockedAt: String)` should accept `LocalDateTime`.
- **Fix:** Use `LocalDateTime` parameter.

### BUG-018: DatabaseModule onCreate — duplicate default data insertion path
- **File:** `app/src/main/java/com/financeapp/di/DatabaseModule.kt`
- **Lines:** 60-86 vs. CategoryRepository.initializeDefaultCategories(), AccountRepository.initializeDefaultAccounts(), AchievementRepository.initializeDefaultAchievements()
- **Severity:** LOW
- **Description:** Default data is seeded in two places: `RoomDatabase.Callback.onCreate()` (raw SQL) and repository `initializeDefault*()` methods (via DAO). The raw SQL in `onCreate` inserts hardcoded IDs, while the repository methods check count. This duplication could lead to inconsistent behavior if one path is changed without the other.
- **Fix:** Consolidate to one seeding path (preferably the repository methods called from Application.onCreate).

### BUG-019: TransactionDao.getByDateRange returns Transaction without Category
- **File:** `app/src/main/java/com/financeapp/data/database/TransactionDao.kt`
- **Line:** 38
- **Severity:** LOW
- **Description:** `getByDateRange` returns `Flow<List<Transaction>>` while most other queries return `Flow<List<TransactionWithCategory>>`. This inconsistency means callers must handle raw Transactions separately, potentially missing category information.
- **Fix:** Add a `getByDateRangeWithCategory()` method returning `TransactionWithCategory` for consistency.

### BUG-020: TransactionViewModel.addTransaction doesn't update account balance
- **File:** `app/src/main/java/com/financeapp/ui/viewmodel/TransactionViewModel.kt`
- **Lines:** 233-251
- **Severity:** LOW
- **Description:** The `addTransaction` method in TransactionViewModel (used from the swipe-to-add shortcut) doesn't update the account balance, unlike `AddTransactionViewModel.submitTransaction()` which does. This creates an inconsistency depending on which path is used to add a transaction.
- **Fix:** Apply the same account balance update logic as in `AddTransactionViewModel.submitTransaction()`.

---

## Summary

| Severity | Count | Key Areas |
|----------|-------|-----------|
| CRITICAL | 3 | Balance calculation on edit (BUG-001), duplicate recurring transactions (BUG-002, BUG-003) |
| HIGH | 3 | Exploitable freeze granting (BUG-004), race condition in gamification (BUG-005), Double precision for money (BUG-006) |
| MEDIUM | 6 | FTS injection (BUG-007), enum crash on bad data (BUG-008), CSV data loss (BUG-009), undo ID mismatch (BUG-010), polling anti-pattern (BUG-011), wasteful recompute (BUG-012) |
| LOW | 8 | Type safety in DAOs, duplicate seeding paths, inconsistent patterns |
| **Total** | **20** | |

### Top 3 Fixes (Highest Impact)
1. **BUG-001** — Read old transaction BEFORE update in EditTransactionViewModel
2. **BUG-002** — Make transaction generation + schedule advancement atomic in RecurringTransactionWorker
3. **BUG-004** — Only grant streak freezes when streak actually increases
