# FinanceApp UI/UX Audit Report

**Audited**: All 10 Screen files + 20 Component files (30 files total)
**Date**: 2026-07-20

---

## CRITICAL Issues

### C1. Deprecated `Divider` usage (should be `HorizontalDivider`)
| File | Line | Severity |
|------|------|----------|
| TransactionListScreen.kt | 31 (import), 250 (usage) | CRITICAL |
| BudgetScreen.kt | 39 (import), 132/157/180/185 (usages) | CRITICAL |
| SettingsScreen.kt | 21 (import), 132/157/180/185 (usages) | CRITICAL |

**Fix**: Replace `Divider` with `HorizontalDivider` from `material3`. `Divider` is deprecated since Material3 1.2.0.

---

### C2. Missing keys in LazyColumn `items()` — causes broken animations & recomposition waste
| File | Line | Severity |
|------|------|----------|
| DashboardScreen.kt | 322 | CRITICAL |
| ReportScreen.kt | 240 | CRITICAL |
| GamificationScreen.kt | 431 (forEach+item pattern) | CRITICAL |
| CategorySelector.kt | 85 (LazyVerticalGrid items) | CRITICAL |

**DashboardScreen.kt:322** — `items(uiState.recentTransactions.take(5))` has no `key` parameter.
**Fix**: `items(uiState.recentTransactions.take(5), key = { it.transaction.id })`

**ReportScreen.kt:240** — `items(uiState.monthlyReport!!.categoryBreakdown)` has no `key`.
**Fix**: `items(uiState.monthlyReport!!.categoryBreakdown, key = { it.category.id })`

**GamificationScreen.kt:431** — `forEach` + `item { }` pattern inside LazyColumn instead of `items()`.
**Fix**: Replace `categoryAchievements.forEach { achievement -> item { ... } }` with `items(categoryAchievements, key = { it.id }) { ... }`

**CategorySelector.kt:85** — LazyVerticalGrid items without key.
**Fix**: `items(categories, key = { it.id })`

---

### C3. Shimmer effect breaks in dark mode
| File | Line | Severity |
|------|------|----------|
| ShimmerComponents.kt | 51 | CRITICAL |

`Color.White.copy(alpha = 0.4f)` is hardcoded for the shimmer gradient. In dark mode, a white shimmer on dark surface looks wrong.

**Fix**: Use `MaterialTheme.colorScheme.surfaceVariant` or `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)` instead. Since `shimmerEffect()` is a `Modifier` extension (not a `@Composable`), it needs access to colors via `composed { }` which IS composable context:
```kotlin
val shimmerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
```

---

### C4. Deprecated `LinearProgressIndicator(progress = value)` overload
| File | Line | Severity |
|------|------|----------|
| ReportScreen.kt | 271 | CRITICAL |

**Fix**: Change `progress = (summary.percentage / 100f)` to `progress = { (summary.percentage / 100f) }` (lambda form).

---

## HIGH Issues

### H1. Hardcoded colors bypassing theme (dark mode broken)
| File | Lines | Description |
|------|-------|-------------|
| DashboardScreen.kt | 542-548 | Flame colors: `Color(0xFFFF6F00)`, `Color(0xFFFFA726)`, `Color(0xFFEF5350)`, `Color(0xFFBDBDBD)` |
| StreakCard.kt | 41-45 | Same hardcoded flame colors |

**Fix**: Create `FinanceColors` semantic entries (e.g., `flameHot`, `flameWarm`, `flameCold`) or use existing `financeColors.warning`/`financeColors.expense` variants.

---

### H2. Duplicate import in BudgetScreen.kt
| File | Line | Severity |
|------|------|----------|
| BudgetScreen.kt | 54, 69 | HIGH |

`LocalFocusManager` is imported twice.

**Fix**: Remove one of the duplicate imports.

---

### H3. Non-composable function called in recomposition scope without `remember`
| File | Lines | Severity |
|------|-------|----------|
| TransactionListScreen.kt | 206 | HIGH |

`groupTransactionsByDate(uiState.filteredTransactions.take(uiState.visibleCount))` is called on every recomposition. The grouping operation (groupBy + toSortedMap + mapKeys) is not free.

**Fix**: Wrap in `remember(uiState.filteredTransactions, uiState.visibleCount)` or extract to a `derivedStateOf`:
```kotlin
val groupedTransactions = remember(uiState.filteredTransactions, uiState.visibleCount) {
    groupTransactionsByDate(uiState.filteredTransactions.take(uiState.visibleCount))
}
```

---

### H4. Fully qualified class names instead of imports
| File | Lines | Description |
|------|-------|-------------|
| DashboardScreen.kt | 382-383, 393-394 | `androidx.compose.ui.geometry.Size` |
| ReportScreen.kt | 156 | `androidx.compose.material3.HorizontalDivider` |
| SettingsScreen.kt | 167 | `androidx.compose.material3.TextButton` |
| AccountSelector.kt | 169, 174 | `androidx.compose.foundation.BorderStroke` |
| HapticFeedback.kt | 58 | `androidx.compose.material3.IconButton` |

**Fix**: Add proper imports and use short names.

---

### H5. SettingsScreen error state has no retry button
| File | Lines | Severity |
|------|-------|----------|
| SettingsScreen.kt | 58-72 | HIGH |

Error state only shows text with no retry mechanism. All other screens have a retry button.

**Fix**: Add a retry button like other screens, or at minimum a back button.

---

### H6. MainScreen tab switching causes full recomposition
| File | Lines | Severity |
|------|-------|----------|
| MainScreen.kt | 113-123 | HIGH |

Using `when (selectedTab)` without any state preservation means each screen is recreated from scratch when switching tabs.

**Fix**: Use `Crossfade` or `AnimatedContent` with tab-specific keys, or use `SaveableStateHolder` to preserve tab state.

---

### H7. SettingsScreen uses manual Row + IconButton instead of TopAppBar
| File | Lines | Severity |
|------|-------|----------|
| SettingsScreen.kt | 84-89 | HIGH |

Inconsistent with AddTransactionScreen and EditTransactionScreen which use `TopAppBar`. Missing status bar inset handling.

**Fix**: Use `TopAppBar` with `navigationIcon` for consistency.

---

### H8. `forEach` + `item { }` inside LazyColumn in GamificationScreen
| File | Lines | Severity |
|------|-------|----------|
| GamificationScreen.kt | 320-325 | HIGH |

XP history uses `forEach` inside a `Card > Column` (not inside LazyColumn directly), but each item is rendered as a composable Column — this works but is less performant than using `LazyColumn` for large lists.

**Note**: Since this is inside a single `item {}` in the parent LazyColumn, it renders all XP history items eagerly. For small lists (< 20) this is fine, but could be a perf issue for large histories.

**Fix**: Consider using `items()` on the parent LazyColumn for XP history items, or extract to a nested LazyColumn.

---

## MEDIUM Issues

### M1. Hardcoded dimensions not using Spacing
| File | Lines | Description |
|------|-------|-------------|
| DashboardScreen.kt | 219, 249, 368, 372, 591-592, 599, 601 | `2.dp`, `1.dp`, `72.dp`, `56.dp`, `28.dp` |
| TransactionListScreen.kt | 312 | `48.dp` |
| BudgetScreen.kt | 172, 342, 355, 459, 478, 557-558 | `80.dp`, `12.dp`, `6.dp`, `44.dp` |
| GamificationScreen.kt | 146, 155, 323, 404, 474, 478, 480, 512-513, 527 | `(-4).dp`, `44.dp`, `10.dp`, `48.dp`, `36.dp`, `18.dp`, `6.dp` |
| ReportScreen.kt | 203, 274 | `48.dp`, `12.dp` |
| OnboardingScreen.kt | 197, 225-226, 258 | `56.dp`, `120.dp`, `24.sp` |
| ShimmerComponents.kt | 117, 140 | `40.dp`, `80.dp` |
| DatePickerField.kt | 68 | `14.dp` |
| CategorySelector.kt | 62, 115 | `6.dp`, `48.dp` |
| MonthlyTrendChart.kt | 129 | `200.dp` |
| DonutChart.kt | 85, 88 | `200.dp` |
| SettingsScreen.kt | 244 | `2.dp` |

The Spacing.kt comment says "Never use raw .dp values in screens or components." This rule is widely violated.

**Fix**: Add missing Spacing values (e.g., `Spacing.size44`, `Spacing.size56`, `Spacing.size200`) or use existing ones where close enough.

---

### M2. Hardcoded contentDescription strings (not using stringResource)
| File | Lines | Description |
|------|-------|-------------|
| GamificationScreen.kt | 106, 118, 172, 210, 364, 403, 478, 519, 596 | "Kesalahan memuat data", "Muat ulang", "Streak freeze tersedia", "Pengaturan", "Prestasi", "Belum ada pencapaian", "Statistik", "Tantangan selesai", "Riwayat XP" |
| ReportScreen.kt | 79, 88, 120, 202 | "Bulan Sebelumnya", "Bulan Berikutnya", "Kesalahan", "Tidak ada data" |
| SettingsScreen.kt | 86 | "Kembali" |
| MainScreen.kt | 103 | "Tambah Transaksi" |
| SearchBar.kt | 53, 62, 69 | "Search", "Clear", "Filter" (English in Indonesian app!) |
| DailyQuestCard.kt | 129 | "Selesai", "Belum selesai" |
| StreakCard.kt | 79 | "Streak" |
| HealthScoreCard.kt | 143 | "Meningkat", "Menurun" |
| DatePickerField.kt | 81 | "Pilih tanggal" |
| SwipeableTransactionItem.kt | 120, 125 | "Hapus transaksi", "Edit transaksi" |
| AccountSelector.kt | 123 | "Terpilih" |
| OnboardingScreen.kt | various | All page text |

**Fix**: Move all strings to `strings.xml` and use `stringResource()`.

---

### M3. Hardcoded UI text (not using stringResource)
| File | Lines | Description |
|------|-------|-------------|
| GamificationScreen.kt | 119, 127, 140, 182-186, 195, 200, 289, 300, 311, 374, 409-414, 462-464, 507, 544, 549, 557, 569-576 | Many UI strings |
| ReportScreen.kt | 136, 154, 160, 170, 183, 207, 213, 223, 234 | Report text |
| SettingsScreen.kt | 88, 93, 103-104, 117, 127-128, 134-135, 142, 152-153, 164-165, 182-188, 195, 200-201, 221 | All settings text |
| FilterDialog.kt | 52, 56, 64, 69, 74, 81, 103, 112, 121, 140, 149 | All filter text |
| OnboardingScreen.kt | 78, 84, 89-90, 140, 204 | Onboarding text |
| SearchBar.kt | 28 | Placeholder |
| CategorySelector.kt | 49, 71, 79, 99, 126 | Category text |
| HapticFeedback.kt | 41 | "Menyimpan..." |
| AccountSelector.kt | 64, 75 | "Akun", "Pilih akun" |
| EditTransactionScreen.kt | 132 | "Tipe: ..." |
| MonthlyTrendChart.kt | 102, 119 | Legend text |
| XpSummaryDialog.kt | 86-87, 93, 98, 105, 123, 140, 146, 159 | Dialog text |
| BudgetScreen.kt | 237-239, 355, 529, 534, 622, 634 | Budget text |

**Fix**: Extract all to `strings.xml`.

---

### M4. Error state icon is misleading (TrendingUp for error)
| File | Lines | Description |
|------|-------|-------------|
| DashboardScreen.kt | 155 | Uses `TrendingDown` icon for error — acceptable |
| TransactionListScreen.kt | 183 | Uses `TrendingUp` icon for error — WRONG |
| ReportScreen.kt | 119 | Uses `TrendingUp` icon for error — WRONG |
| GamificationScreen.kt | 105 | Uses `TrendingUp` icon for error — WRONG |

**Fix**: Use `Icons.Filled.Error` or `Icons.Filled.ErrorOutline` for error states (as BudgetScreen.kt correctly does on line 145).

---

### M5. Duplicate flame color logic
| File | Lines | Description |
|------|-------|-------------|
| DashboardScreen.kt | 542-548 | Flame color `when` block |
| StreakCard.kt | 41-45 | Identical flame color `when` block |

**Fix**: Extract to a shared utility function (e.g., `StreakUtils.getFlameColor(streak)`).

---

### M6. HealthScoreRing and HealthScoreCard have divergent implementations
| File | Lines | Description |
|------|-------|-------------|
| DashboardScreen.kt | 331-448 | `HealthScoreRing` — local private composable |
| HealthScoreCard.kt | 36-168 | `HealthScoreCard` — separate component |

Two separate implementations of the health score ring with different styling and different logic for `scoreColor`. DashboardScreen uses `financeColors.income` for score >= 80, while HealthScoreCard uses `primary` for score >= 80.

**Fix**: Consolidate into a single shared component or make DashboardScreen use HealthScoreCard.

---

### M7. Missing `@OptIn(ExperimentalMaterial3Api::class)` where needed
| File | Lines | Description |
|------|-------|-------------|
| FilterDialog.kt | 37 | Has it for `FlowRow` but FilterChip doesn't need it (stable in Material3) |

**Note**: This is minor — `ExperimentalLayoutApi` is used for `FlowRow`.

---

### M8. OnboardingViewModel defined inside Screen file
| File | Lines | Description |
|------|-------|-------------|
| OnboardingScreen.kt | 54-66 | ViewModel + data class + static list all in Screen file |

**Fix**: Move `OnboardingViewModel` to the `viewmodel/` package and `OnboardingPage` data class to `model/`.

---

### M9. `ReportScreen.kt` uses `!!` (non-null assertion)
| File | Line | Description |
|------|------|-------------|
| ReportScreen.kt | 240 | `uiState.monthlyReport!!.categoryBreakdown` |

**Fix**: Use safe-call `?.` pattern or guard with `val report = uiState.monthlyReport; if (report != null)`.

---

### M10. Hardcoded button height in OnboardingScreen
| File | Line | Description |
|------|------|-------------|
| OnboardingScreen.kt | 197 | `.height(56.dp)` |

**Fix**: Use a Spacing constant or MaterialTheme's recommended dimensions.

---

## LOW Issues

### L1. Inconsistent error state patterns across screens
| Screen | Loading | Error | Empty |
|--------|---------|-------|-------|
| DashboardScreen | Shimmer skeleton | Icon + text + retry | Savings icon + text |
| TransactionListScreen | CircularProgressIndicator | TrendingUp icon + text + retry | Search icon + text |
| BudgetScreen | CircularProgressIndicator | Error icon + text + retry | Savings icon + text |
| GamificationScreen | CircularProgressIndicator | TrendingUp icon + text + retry | EmojiEvents icon + text |
| ReportScreen | CircularProgressIndicator | TrendingUp icon + text + retry | Receipt icon + text |
| SettingsScreen | CircularProgressIndicator | Text only (no retry, no icon) | N/A |

**Fix**: Create a shared `ErrorState` and `LoadingState` composable for consistency.

---

### L2. Hardcoded `48.dp` for empty state icons
| File | Line | Description |
|------|------|-------------|
| TransactionListScreen.kt | 312 | `.size(48.dp)` |
| ReportScreen.kt | 203 | `.size(48.dp)` |
| GamificationScreen.kt | 404 | `.size(48.dp)` |
| DashboardScreen.kt | 658 | `.size(48.dp)` |
| BudgetScreen.kt | 666 | `.size(48.dp)` |

**Fix**: Use `Spacing.iconLg` (48.dp) — this is actually correct value but should use the constant.

---

### L3. FilterDialog uses `∞` character for Max placeholder
| File | Line | Description |
|------|------|-------------|
| FilterDialog.kt | 121 | `placeholder = { Text("∞") }` |

The infinity character may render poorly on some Android devices/fonts. Also not translatable.

**Fix**: Use "Tidak terbatas" string resource or "999999".

---

### L4. SearchBar debounced search fires on initial composition
| File | Lines | Description |
|------|-------|-------------|
| SearchBar.kt | 40-43 | `LaunchedEffect(localQuery)` fires immediately |

When the component first composes, the LaunchedEffect runs and calls `onSearchChange("")` after 300ms even if no search was initiated.

**Fix**: Add a flag to skip the initial emission, or use `snapshotFlow` with debounce.

---

### L5. Missing `Modifier.semantics` for accessibility
| File | Description |
|------|-------------|
| Multiple files | Interactive elements (clickable cards, swipe items) lack explicit semantics for TalkBack |

**Fix**: Add `Modifier.semantics { ... }` or `Modifier.clearAndSetSemantics { ... }` to interactive containers, especially `SwipeableTransactionItem`, `CategoryItem`, and `BudgetItem`.

---

### L6. `DashboardScreen.kt` line 424 — STABLE trend shows misleading icon
| File | Line | Description |
|------|------|-------------|
| DashboardScreen.kt | 348-349, 424 | When `trend == STABLE`, icon is `TrendingUp` with contentDescription "Meningkat" |

**Fix**: For STABLE trend, use `Icons.Filled.HorizontalRule` or similar, and contentDescription "Stabil".

---

### L7. `BudgetScreen.kt` hardcoded text in delete dialog
| File | Line | Description |
|------|------|-------------|
| BudgetScreen.kt | 634 | `"Yakin ingin menghapus budget untuk ${budget.category.name}?"` |

**Fix**: Use `stringResource(R.string.budget_delete_confirm, budget.category.name)`.

---

### L8. `EditTransactionScreen.kt` hardcoded transaction type display
| File | Line | Description |
|------|------|-------------|
| EditTransactionScreen.kt | 132 | `"Tipe: ${if (...) \"Pengeluaran\" else \"Pemasukan\"}"` |

**Fix**: Use `stringResource(R.string.edit_transaction_type, if (...) stringResource(R.string.common_expense) else stringResource(R.string.common_income))`.

---

### L9. Missing Scaffold paddingValues handling
| File | Line | Description |
|------|------|-------------|
| GamificationScreen.kt | 132 | `Column(modifier = Modifier.fillMaxSize().padding(paddingValues))` — but no topBar, so this just adds bottom padding from nowhere |

**Note**: This is actually fine since the NavigationBar provides the bottom padding, but it's inconsistent with screens that don't explicitly apply paddingValues.

---

### L10. `DashboardScreen.kt` uses `forEach + item` for XP history
| File | Lines | Description |
|------|-------|-------------|
| GamificationScreen.kt | 320-325 | `state.recentXpHistory.forEach { xp -> XpHistoryRow(...) }` inside a single `item {}` |

This is technically correct but means all XP history items are composed at once, not lazily.

---

## Summary Statistics

| Severity | Count |
|----------|-------|
| CRITICAL | 4 (across 12 locations) |
| HIGH | 8 |
| MEDIUM | 10 |
| LOW | 10 |
| **Total unique issues** | **32** |

## Top Recommendations (Priority Order)

1. **Fix deprecated `Divider` → `HorizontalDivider`** across 3 files (quick fix, high impact)
2. **Add keys to all `items()` calls** in LazyColumn (prevents animation bugs)
3. **Fix shimmer dark mode** (Color.White → theme color)
4. **Replace deprecated `LinearProgressIndicator(progress = value)`** with lambda form
5. **Move all hardcoded strings to `strings.xml`** (largest effort, biggest accessibility/i18n win)
6. **Extract hardcoded colors to theme** (dark mode correctness)
7. **Consolidate error/loading state composables** (DRY, consistency)
8. **Fix TrendingUp icon used for error states** (UX confusion)
9. **Add `remember` to `groupTransactionsByDate`** (perf fix)
10. **Use `Spacing` constants instead of raw `dp`** (design system compliance)
