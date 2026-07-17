# FinanceApp — MASTER PLAN
## Senior Developer Execution Document

---

## AUDIT KONDISI SAAT INI

### Codebase:
- 37 Kotlin files, 3.355 lines
- MVVM pattern, manual DI (ViewModelFactory di MainActivity)
- Compose BOM 2023.10 → Material 3 1.1.2 (banyak API tidak tersedia)
- Room v2.6.0, kapt (bukan KSP)
- 3 entities: Transaction, Category, Budget
- 3 DAOs, 3 repositories
- 8 screens, 6 viewmodels, 4 components
- Tidak ada dependency injection framework
- Tidak ada unit test
- Tidak ada chart/visualization library
- Tidak ada DataStore (SharedPreferences)
- Tidak ada WorkManager (notifications)

### Masalah Arsitektur:
1. MainActivity membuat semua ViewModel dengan manual factory — fragile
2. ViewModel tidak terisolasi — saling bergantung pada repository yang sama
3. Tidak ada use-case layer — business logic langsung di ViewModel
4. Flow collection tidak menggunakan WhileSubscribed(5000) — memory leak risk
5. Error handling tidak konsisten
6. Tidak ada loading states yang proper (shimmer, skeleton)

---

## ARSITEKTUR TARGET

```
┌─────────────────────────────────────────────┐
│                   UI LAYER                   │
│  Screen (Composable) ← UiState (data class) │
│  Screen → Event → ViewModel                 │
├─────────────────────────────────────────────┤
│               DOMAIN LAYER                  │
│  UseCase (suspend fun / Flow)               │
│  Business logic, validation, mapping        │
├─────────────────────────────────────────────┤
│                DATA LAYER                   │
│  Repository ← DAO (Room) ← Database         │
│  Repository ← DataStore (preferences)       │
│  Repository ← WorkManager (scheduled)       │
├─────────────────────────────────────────────┤
│              DI LAYER (Hilt)                │
│  @HiltViewModel, @Module, @Provides         │
└─────────────────────────────────────────────┘
```

### Pola MVI per Screen:
```kotlin
// 1. UiState — single source of truth
data class DashboardUiState(
    val balance: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null
)

// 2. UiEvent — semua aksi user
sealed interface DashboardUiEvent {
    data class MonthChanged(val month: YearMonth) : DashboardUiEvent
    data object Refresh : DashboardUiEvent
}

// 3. ViewModel — handle event, produce state
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardData: GetDashboardDataUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()
    
    fun onEvent(event: DashboardUiEvent) { ... }
}

// 4. Composable — render state, emit event
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // render...
}
```

---

## DEPENDENCY GRAPH

```
Phase 1 (Foundation)
  ├── Upgrade Compose BOM → 2024.x (unlock Material3 1.2+)
  ├── Tambah Hilt DI
  ├── Migrate kapt → KSP
  ├── Tambah DataStore
  └── Refactor ViewModel → MVI pattern

Phase 2 (Dashboard) — depends on Phase 1
  ├── Tambah Vico chart library
  ├── Animated number counter component
  ├── Donut chart component
  ├── Line chart component
  └── Revamp DashboardScreen

Phase 3 (Transactions) — depends on Phase 1
  ├── FTS4 search di Room
  ├── Swipe actions component
  ├── Search bar component
  ├── Filter dialog component
  └── Revamp TransactionListScreen

Phase 4 (Multi-Account) — depends on Phase 1
  ├── Account entity + DAO + repository
  ├── Recurring transaction system
  ├── Account selector component
  └── Revamp AddTransactionScreen

Phase 5 (Onboarding) — depends on Phase 1
  ├── Onboarding screens (3-4 pages)
  ├── DataStore preferences
  ├── Export CSV utility
  └── SettingsScreen revamp

Phase 6 (Gamification) — depends on Phase 2,3,4
  ├── Achievement system
  ├── Streak tracker
  ├── Financial health score
  └── Challenge system

Phase 7 (Polish) — depends on all above
  ├── Animations everywhere
  ├── Empty states
  ├── Error states
  ├── Edge cases
  └── Performance optimization
```

---

## EKSEKUSI PLAN

### Setiap Phase punya siklus:
1. **PLAN** — tulis task detail dengan file yang diubah
2. **BUILD** — tulis kode
3. **REVIEW** — baca ulang semua file yang diubah, cek:
   - Import benar
   - Logic benar
   - Edge cases tertangani
   - Tidak ada compile error
   - Tidak ada runtime error risk
4. **VERIFY** — push ke GitHub, cek CI build
5. **FIX** — jika CI gagal, fix segera

### Verification Checklist per Task:
- [ ] Semua file yang diubah sudah di-review manual
- [ ] Tidak ada unused import
- [ ] Tidak ada hardcoded string (pakai strings.xml atau constant)
- [ ] Error handling lengkap (try-catch, null check)
- [ ] Loading state ada
- [ ] Empty state ada
- [ ] Dark mode compatible
- [ ] Accessibility: contentDescription pada semua Icon/Image

---

## PHASE 1 DETAIL: FOUNDATION UPGRADE

### Task 1.1: Upgrade Dependencies
**File:** `build.gradle.kts` (root + app)
**Changes:**
- Compose BOM → 2024.02.00 (Material 3 1.2.0)
- Kotlin → 1.9.22
- Room → 2.6.1
- Tambah Hilt 2.50
- Tambah KSP plugin
- Tambah DataStore
- Tambah Vico chart library
- Tambah Accompanist
**Verify:** CI build pass

### Task 1.2: Setup Hilt
**File:** `FinanceApp.kt`, `MainActivity.kt`, `build.gradle.kts`
**Changes:**
- @HiltAndroidApp di Application
- @AndroidEntryPoint di MainActivity
- Hilt plugin di build.gradle
- Hapus manual ViewModelFactory
**Verify:** CI build pass

### Task 1.3: Migrate kapt → KSP (Room)
**File:** `build.gradle.kts`
**Changes:**
- Ganti `kapt` → `ksp` untuk Room compiler
- Tambah KSP plugin
**Verify:** CI build pass

### Task 1.4: Create Hilt Modules
**File:** `di/DatabaseModule.kt`, `di/RepositoryModule.kt`
**Changes:**
- @Module @InstallIn(SingletonComponent)
- Provide Database, DAOs, Repositories
**Verify:** CI build pass

### Task 1.5: Refactor ViewModels ke Hilt + MVI
**File:** Semua ViewModel (6 files)
**Changes:**
- @HiltViewModel pada semua ViewModel
- @Inject constructor
- UiState data class per screen
- UiEvent sealed interface per screen
- onEvent() handler
- Hapus manual factory
**Verify:** CI build pass

### Task 1.6: Update MainActivity
**File:** `MainActivity.kt`
**Changes:**
- @AndroidEntryPoint
- Hapus semua manual ViewModel creation
- Simplify ke single setContent block
**Verify:** CI build pass

---

## PHASE 2 DETAIL: DASHBOARD REVAMP

### Task 2.1: AnimatedNumberCounter Component
**File:** `ui/components/AnimatedNumber.kt` (NEW)
**Description:** Angka yang slide/fade saat berubah
**Implementation:** AnimatedContent + slideInVertically/slideOutVertically
**Verify:** Component renders correctly

### Task 2.2: DonutChart Component
**File:** `ui/components/DonutChart.kt` (NEW)
**Description:** Donut/pie chart untuk expense breakdown
**Implementation:** Canvas drawArc dengan animated sweepAngle
**Verify:** Chart renders dengan data sample

### Task 2.3: LineChart Component
**File:** `ui/components/LineChart.kt` (NEW)
**Description:** Line chart income vs expense trend
**Implementation:** Canvas drawLine/drawPath
**Verify:** Chart renders dengan data sample

### Task 2.4: CircularBudgetProgress Component
**File:** `ui/components/CircularProgress.kt` (NEW)
**Description:** Circular progress ring untuk budget
**Implementation:** Canvas drawArc dengan gradient color
**Verify:** Progress animates dari 0 ke value

### Task 2.5: Revamp DashboardScreen
**File:** `ui/screens/DashboardScreen.kt`
**Description:** Redesign dashboard dengan semua komponen baru
**Layout:**
- Top: Balance card dengan animated number
- Middle: Income/Expense cards dengan animated numbers
- Donut chart expense breakdown
- Line chart trend (3 bulan terakhir)
- Budget progress rings
- Recent transactions list
**Verify:** All components render, data flows correctly

### Task 2.6: DashboardViewModel Enhancement
**File:** `ui/viewmodel/DashboardViewModel.kt`
**Changes:**
- Tambah trend data (3 bulan)
- Tambah category breakdown data
- MVI pattern
**Verify:** Data loads correctly

---

## PHASE 3 DETAIL: TRANSACTION UX

### Task 3.1: FTS4 Search
**File:** `data/database/FinanceDatabase.kt`, `TransactionDao.kt`
**Description:** Full-text search untuk transaksi
**Verify:** Search returns correct results

### Task 3.2: SearchBar Component
**File:** `ui/components/SearchBar.kt` (NEW)
**Description:** Search input dengan debounce
**Verify:** Search triggers after 300ms delay

### Task 3.3: FilterDialog Component
**File:** `ui/components/FilterDialog.kt` (NEW)
**Description:** Filter by type, category, date range, amount range
**Verify:** Filters apply correctly

### Task 3.4: SwipeActions Component
**File:** `ui/components/SwipeActions.kt` (NEW)
**Description:** Swipe-to-edit (right), swipe-to-delete (left)
**Verify:** Swipes work, delete has undo

### Task 3.5: Revamp TransactionListScreen
**File:** `ui/screens/TransactionListScreen.kt`
**Description:** Redesign dengan search, filter, swipe, group by date
**Verify:** All features work together

---

## PHASE 4 DETAIL: MULTI-ACCOUNT & RECURRING

### Task 4.1: Account Entity & System
**File:** `data/model/Account.kt`, `data/database/AccountDao.kt`, `data/repository/AccountRepository.kt`
**Description:** Multi-account/wallet system
**Verify:** CRUD works

### Task 4.2: Add Account to Transaction
**File:** `data/model/Transaction.kt`
**Description:** Tambah accountId field ke Transaction
**Verify:** Migration works

### Task 4.3: Recurring Transaction System
**File:** `data/model/RecurringTransaction.kt`, `data/database/RecurringTransactionDao.kt`
**Description:** Template transaksi berulang
**Verify:** Auto-add works

### Task 4.4: AccountSelector Component
**File:** `ui/components/AccountSelector.kt` (NEW)
**Description:** Dropdown/tab selector untuk account
**Verify:** Selection works

---

## PHASE 5 DETAIL: ONBOARDING & SETTINGS

### Task 5.1: Onboarding Flow
**File:** `ui/screens/OnboardingScreen.kt` (NEW), `ui/navigation/AppNavigation.kt`
**Description:** 3-4 screen HorizontalPager
**Verify:** Shows on first launch only

### Task 5.2: DataStore Preferences
**File:** `data/preferences/AppPreferences.kt` (NEW)
**Description:** Dark mode, currency, onboarding status
**Verify:** Preferences persist

### Task 5.3: Export CSV
**File:** `util/CsvExporter.kt` (NEW)
**Description:** Export transactions to CSV
**Verify:** File created correctly

### Task 5.4: Revamp SettingsScreen
**File:** `ui/screens/SettingsScreen.kt`
**Description:** Full settings dengan toggle, export, about
**Verify:** All toggles work

---

## PHASE 6 DETAIL: GAMIFICATION

### Task 6.1: Achievement System
**File:** `data/model/Achievement.kt`, `data/database/AchievementDao.kt`
**Description:** Badge system dengan unlock conditions
**Verify:** Achievements unlock correctly

### Task 6.2: Streak Tracker
**File:** `data/model/Streak.kt`, `data/database/StreakDao.kt`
**Description:** Track consecutive days of logging
**Verify:** Streak increments, freeze works

### Task 6.3: Financial Health Score
**File:** `domain/GetHealthScoreUseCase.kt` (NEW)
**Description:** Score 0-100 based on financial metrics
**Verify:** Score calculates correctly

### Task 6.4: Gamification UI
**File:** `ui/components/AchievementBadge.kt`, `ui/components/StreakCounter.kt`
**Description:** Visual components for gamification
**Verify:** Renders correctly

---

## PHASE 7 DETAIL: POLISH

### Task 7.1: Animation Pass
- Entry animations (staggered cards)
- Transition animations (screen changes)
- Micro-interactions (button press, toggle)
- Haptic feedback

### Task 7.2: Empty States
- No transactions → illustration + CTA
- No budgets → illustration + CTA
- No data for report → illustration

### Task 7.3: Error States
- Network error → retry button
- Database error → error message
- Validation errors → inline messages

### Task 7.4: Edge Cases
- Very long descriptions
- Very large amounts
- Rapid button tapping
- Rotation handling
- Back navigation

---

## FILE CREATION MAP

### New Files (akan dibuat):
```
di/
  DatabaseModule.kt          — Hilt module for DB
  RepositoryModule.kt        — Hilt module for repos

domain/
  GetDashboardData.kt        — Use case
  GetHealthScore.kt          — Use case

data/model/
  Account.kt                 — Account entity
  RecurringTransaction.kt    — Recurring transaction entity
  Achievement.kt             — Achievement entity

data/database/
  AccountDao.kt              — Account DAO
  RecurringTransactionDao.kt — Recurring DAO
  AchievementDao.kt          — Achievement DAO

data/preferences/
  AppPreferences.kt          — DataStore wrapper

ui/components/
  AnimatedNumber.kt          — Animated counter
  DonutChart.kt              — Donut/pie chart
  LineChart.kt               — Line chart
  CircularProgress.kt        — Circular budget progress
  SearchBar.kt               — Search input
  FilterDialog.kt            — Filter dialog
  SwipeActions.kt            — Swipe to edit/delete
  AchievementBadge.kt        — Badge component
  StreakCounter.kt           — Streak display

ui/screens/
  OnboardingScreen.kt        — Onboarding flow

util/
  CsvExporter.kt             — CSV export utility
```

### Modified Files (akan diubah):
```
build.gradle.kts (root)      — Plugin updates
app/build.gradle.kts         — Dependencies update
FinanceApp.kt                — @HiltAndroidApp
MainActivity.kt              — @AndroidEntryPoint
FinanceDatabase.kt           — New entities + migrations
All ViewModels (6)           — @HiltViewModel + MVI
All Screens (8)              — Updated UI
AppNavigation.kt             — New routes
```

---

## REVIEW CHECKLIST

Setelah setiap task:
1. Baca ulang semua file yang diubah/dibuat
2. Cek: tidak ada error kompilasi (logika)
3. Cek: edge cases tertangani
4. Cek: dark mode compatible
5. Cek: accessibility (contentDescription)
6. Cek: tidak ada hardcoded string
7. Push ke GitHub
8. Verifikasi CI build pass
9. Jika gagal → fix segera sebelum lanjut

---

*Document ini akan diupdate setelah setiap phase selesai.*
