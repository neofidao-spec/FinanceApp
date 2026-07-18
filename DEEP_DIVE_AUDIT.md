# FinanceApp — DEEP DIVE AUDIT REPORT (FINAL)
**Tanggal:** 18 Juli 2026  
**Auditor:** Hermes Agent + 3 Parallel Subagents  
**Scope:** 85 Kotlin files, 13 doc files, build config, CI, manifest  
**Metodology:** Cover-to-cover read, 4 parallel audits (Data, ViewModel, UI, Build)

---

## RINGKASAN EKSEKUTIF

| Aspek | Rating | Detail |
|-------|--------|--------|
| Arsitektur | GOOD | MVVM + Clean Arch + Hilt, proper layering |
| Database | GOOD | 11 entities, 8 migrations, all converters |
| ViewModel | FAIR | Race conditions in 3 VMs, collect leaks |
| UI/Compose | FAIR | Dead buttons, 60+ hardcoded colors, dark mode broken |
| Security | NEEDS WORK | Hardcoded keystore password regression |
| Testing | CRITICAL | 0 unit tests, 0 integration tests |
| Accessibility | NEEDS WORK | 10+ contentDescription = null |
| Documentation | EXCELLENT | 13 comprehensive .md files |
| **Overall** | **6/10** | |

**Total Issues: 48** (6 CRITICAL, 12 HIGH, 21 MEDIUM, 9 LOW)

---

## CRITICAL (6)

### C1: Hardcoded Keystore Password — REGRESSION
- **File:** `app/build.gradle.kts:28-30`
- **Issue:** `"financeapp123"` masih sebagai fallback KEYSTORE_PASSWORD/KEY_PASSWORD
- **Bukti:** FIXES_SUMMARY klaim di-fix, tapi commit `b3ff8d9` revert ke hardcoded
- **Fix:** `?: throw GradleException("KEYSTORE_PASSWORD not set")`

### C2: Zero Test Coverage
- **File:** `app/src/test/` (0 files), `app/src/androidTest/` (0 files)
- **Issue:** 85 source files, 0 test files
- **Fix:** Minimal ViewModel unit tests + DAO integration tests

### C3: TransactionFtsDao.search() — Redundant JOIN vs @Relation
- **File:** `data/database/TransactionFtsDao.kt:19-28`
- **Issue:** Manual INNER JOIN categories conflicts with Room @Relation. Double query execution. Silently excludes orphaned transactions.
- **Fix:** Simplify query, let @Relation handle category loading

### C4: Transaction.accountId — NO @ForeignKey/@Index
- **File:** `data/model/Transaction.kt:30`
- **Issue:** `accountId: Long = 1` references accounts table tanpa FK constraint. No referential integrity, orphaned transactions on account delete.
- **Fix:** Add @ForeignKey + @Index, new migration v10

### C5: GamificationViewModel.init — Race Condition
- **File:** `ui/viewmodel/GamificationViewModel.kt:46-49`
- **Issue:** Dual launches in init — `initializeGamification()` + `observeGamificationData()` write to _uiState concurrently. Error messages overwritten silently.
- **Fix:** Sequential execution — init must complete before observe starts

### C6: Dead Retry Button — GamificationScreen
- **File:** `ui/screens/GamificationScreen.kt:104`
- **Issue:** `Button(onClick = { })` — user taps "Coba Lagi", nothing happens
- **Fix:** Wire to `viewModel.retry()` or refresh logic

---

## HIGH (12)

### H1: DashboardViewModel — Spawn Storm
- **File:** `ui/viewmodel/DashboardViewModel.kt:60-73`
- 4 concurrent state writers per transaction change
- **Fix:** Merge loads into combine/flatMapLatest

### H2: TransactionViewModel.init — Triple Launch Race
- **File:** `ui/viewmodel/TransactionViewModel.kt:52-74`
- 3 independent coroutines writing to _uiState concurrently
- **Fix:** Use combine() to unify flows

### H3: TransactionViewModel.filterByType() — Collect Leak
- **File:** `ui/viewmodel/TransactionViewModel.kt:319-339`
- Public method launches new collectLatest on every call, never cancels previous
- **Fix:** Track Job, cancel before relaunch

### H4: GamificationUseCase.addXp() — TOCTOU Race
- **File:** `domain/GamificationUseCase.kt:156-181`
- Non-atomic read-modify-write. Concurrent calls lose XP.
- **Fix:** Room @Transaction or Mutex

### H5: ReportViewModel.init — Cascade Launches
- **File:** `ui/viewmodel/ReportViewModel.kt:37-53`
- Each transaction emission spawns new loadMonthlyReport() coroutine
- **Fix:** Use collectLatest instead of collect

### H6: DatabaseModule.onCreate — Race on Default Data
- **File:** `di/DatabaseModule.kt:58-81`
- Executors.newSingleThreadExecutor makes default insert async. First query may run before defaults populated.
- **Fix:** Remove executor wrapper (onCreate already background)

### H7: Dead Filter Chips — TransactionListScreen
- **File:** `ui/screens/TransactionListScreen.kt:128,134,140,146`
- 4 FilterChip dengan `onClick = {}`
- **Fix:** Implement filter removal or remove chips

### H8: Dark Mode Broken by Hardcoded Colors
- 60+ hardcoded `Color(...)` across screens. Dark mode renders incorrectly.
- **Worst offenders:** BudgetScreen (~20), SettingsScreen (~12), DashboardScreen (~12)
- **Fix:** Replace with MaterialTheme.colorScheme references

### H9: SettingsScreen — No Loading/Error States
- **File:** `ui/screens/SettingsScreen.kt`
- Zero loading/error handling for account/transaction counts
- **Fix:** Add loading indicator + error state

### H10: Missing contentDescription (10+ Icons)
- BudgetScreen, TransactionListScreen, ReportScreen, GamificationScreen, OnboardingScreen
- **Fix:** Add descriptive text for each icon

### H11: Emoji Strings di Default Data
- **File:** `di/DatabaseModule.kt:68-72`
- Default accounts use emoji (💵, 🏦, 📱) as icon field
- **Fix:** Use Material Icon name strings

### H12: Doc Drift — FEATURES.md & STATUS.md Outdated
- Still reference old structure (35 files, 3 entities, no Hilt)
- **Fix:** Update or archive

---

## MEDIUM (21)

### Database (6)
| # | Issue | File |
|---|-------|------|
| M1 | MIGRATION_5_6 no-op (empty) | FinanceDatabase.kt:111 |
| M2 | Inconsistent enum handling (String vs enum) | Challenge, DailyQuest, XpHistory |
| M3 | DAO String params bypassing TypeConverters | DailyQuestDao, UserProgressDao |
| M4 | Missing index on daily_quests.questDate | DailyQuest.kt |
| M5 | Missing index on transactions.date | Transaction.kt |
| M6 | RecurringTransaction.accountId no FK/Index | RecurringTransaction.kt:40 |

### ViewModel (6)
| # | Issue | File |
|---|-------|------|
| M7 | Swallowed exceptions (6x, no logging) | GamificationViewModel.kt |
| M8 | Swallowed exception FTS fallback | TransactionViewModel.kt:66 |
| M9 | Swallowed exception getCategoryBreakdown | ReportViewModel.kt:118 |
| M10 | BudgetRepository missing @Singleton | RepositoryModule.kt |
| M11 | SettingsViewModel.toggleDarkMode no try-catch | SettingsViewModel.kt:61 |
| M12 | suspend calls in combine() transform | SettingsViewModel.kt:43 |

### UI (6)
| # | Issue | File |
|---|-------|------|
| M13 | Color.Gray 12x in SettingsScreen | SettingsScreen.kt |
| M14 | Hardcoded gradient di BalanceCard | DashboardScreen.kt:340 |
| M15 | Hardcoded progress colors (green/yellow/red) | BudgetProgressRing.kt:70 |
| M16 | Stale state AddTransactionScreen | AddTransactionScreen.kt |
| M17 | Error text Color.Red (should be theme.error) | AddTransactionScreen, EditTransaction |
| M18 | Hardcoded category selector colors | CategorySelector.kt:53,114 |

### Build (3)
| # | Issue | File |
|---|-------|------|
| M19 | Lint abortOnError = false | app/build.gradle.kts:78 |
| M20 | Room exportSchema = false | FinanceDatabase.kt:28 |
| M21 | Material3 1.1.2 pinned (vs BOM) | app/build.gradle.kts:92 |

---

## LOW (9)

| # | Issue | File |
|---|-------|------|
| L1 | Room 2.6.1 bisa di-update | build.gradle.kts |
| L2 | Hilt 2.48.1 bisa di-update | build.gradle.kts |
| L3 | Kotlin 1.9.22 bisa di-update | build.gradle.kts |
| L4 | Dead code in RecurringTransactionWorker | RecurringTransactionWorker.kt:54 |
| L5 | Form validation inconsistency (Add vs Budget) | Multiple VMs |
| L6 | clearMessages() race (success vs error clear) | Multiple VMs |
| L7 | Missing index on achievements.category | Achievement.kt |
| L8 | BalanceCard.kt may be unused (dead code) | BalanceCard.kt |
| L9 | Default color params in MonthlyTrendChart | MonthlyTrendChart.kt:58 |

---

## POSITIVE FINDINGS ✅

| Check | Status |
|-------|--------|
| No fallbackToDestructiveMigration | ✅ |
| No runBlocking | ✅ |
| No GlobalScope | ✅ |
| No unsafe `!!` in ViewModels | ✅ |
| All 11 DAOs provided via Hilt | ✅ |
| All 7 Repositories wired | ✅ |
| Converters complete (6 enum + LocalDate + LocalDateTime) | ✅ |
| Migration chain v1→v9 complete | ✅ |
| No emojis in UI (all Material Icons) | ✅ |
| Proper scrollability (no weight in scrollable Column) | ✅ |
| Loading/empty/error states on most screens | ✅ |
| Dark mode Theme.kt properly defined | ✅ |
| allowBackup = false | ✅ |
| Screen transitions + animations | ✅ |
| Shimmer/skeleton loading | ✅ |
| Haptic feedback | ✅ |
| FTS4 full-text search | ✅ |
| WorkManager recurring transactions | ✅ |

---

## STATISTIK

| Layer | Files | LOC |
|-------|-------|-----|
| data/model | 12 | 583 |
| data/database | 13 | 764 |
| data/repository | 7 | 396 |
| data/preferences | 1 | 57 |
| di | 2 | 215 |
| domain | 3 | 377 |
| ui/screens | 10 | 3,336 |
| ui/components | 20 | 2,771 |
| ui/viewmodel | 8 | 1,543 |
| ui/navigation | 2 | 160 |
| ui/theme | 2 | 98 |
| ui/utils | 2 | 148 |
| util | 1 | 27 |
| **TOTAL** | **85** | **10,475** |

---

## REKOMENDASI PRIORITAS

### Immediate (Before Next Commit)
1. Fix keystore password regression (C1)
2. Fix dead retry button GamificationScreen (C6)
3. Fix dead filter chips TransactionListScreen (H7)
4. Tambah contentDescription (H10)

### Short-term (1 Sprint)
5. Fix GamificationViewModel init race (C5)
6. Fix DashboardViewModel spawn storm (H1)
7. Fix TransactionViewModel collect leak (H3)
8. Fix DatabaseModule.onCreate race (H6)
9. Tambah unit test minimal (C2)

### Medium-term (2-3 Sprint)
10. Fix TransactionFtsDao query (C3)
11. Add FK+Index on Transaction.accountId (C4)
12. Replace 60+ hardcoded colors (H8)
13. Fix dark mode rendering (H8)
14. Update Compose BOM + Material3 (M21)

---

*Audit: 85 .kt files + 13 .md files + build config + CI + manifest*
*4 parallel audits: Data Layer, ViewModel, UI, Build/Security*
