# Finance App - Comprehensive Bug Report

## Summary
Found **18 confirmed bugs** + **3 security issues** ranging from Critical to Low severity.

---

## 🔴 CRITICAL BUGS

### 1. Dark Mode Not Wired to Theme
**File:** `MainActivity.kt:30-42`, `Theme.kt:54-62`
**Severity:** HIGH | **Type:** State Management

Dark mode toggle in Settings does nothing. App always uses `isSystemInDarkTheme()` instead of user preference.

**Impact:** Users cannot change theme manually.

---

### 2. runBlocking on Main Thread - ANR Risk
**File:** `SettingsViewModel.kt:56-59`
**Severity:** CRITICAL | **Type:** Performance

```kotlin
val transactions = kotlinx.coroutines.runBlocking { ... }  // ❌ BLOCKS MAIN THREAD
```

**Impact:** Causes ANR crash when exporting CSV. Freezes UI.

---

### 3. Silent Exception Handling
**Files:** `AddTransactionViewModel.kt:72`, `DashboardViewModel.kt:101,123`, `SettingsViewModel.kt:45`
**Severity:** HIGH | **Type:** Error Handling

All exceptions silently swallowed. User has no feedback that data failed to load.

**Impact:** Dashboard shows empty/stale data with no error message.

---

### 4. Destructive Database Migration
**File:** `FinanceDatabase.kt:26`
**Severity:** HIGH | **Type:** Data Loss

```kotlin
.fallbackToDestructiveMigration()  // ❌ DELETES ALL DATA on version change
```

**Impact:** Users lose all transactions on app updates.

---

## 🟠 MAJOR BUGS

### 5. Race Condition in DashboardViewModel
**File:** `DashboardViewModel.kt:51-68`
**Severity:** HIGH | **Type:** State Management

Multiple coroutines race to update `_uiState` without synchronization.

**Impact:** Dashboard sometimes shows blank or inconsistent data on first load.

---

### 6. Missing Null Check - NPE Risk
**File:** `EditTransactionViewModel.kt:127-130`
**Severity:** HIGH | **Type:** Crash Risk

```kotlin
categoryId = _uiState.value.selectedCategory!!.id  // ❌ Can NPE
```

**Impact:** Crashes when form validation state is out of sync.

---

### 7. Wrong Month Filter Logic
**File:** `DashboardViewModel.kt:78-82`
**Severity:** MEDIUM | **Type:** Logic Bug

If current month empty, falls back to ALL transactions ever.

**Impact:** Switching to empty months shows wrong totals.

---

### 8. Memory Leak - Nested Flow Subscriptions
**File:** `BudgetViewModel.kt:30-50`
**Severity:** MEDIUM | **Type:** Memory

Multiple nested flow collectors don't properly unsubscribe.

**Impact:** Memory usage grows with each navigation to Budget tab.

---

### 9. Form Validation Inconsistency
**File:** `EditTransactionViewModel.kt:95-104` vs `AddTransactionViewModel.kt:133`
**Severity:** MEDIUM | **Type:** Validation

Edit requires description non-empty, but Add allows it.

**Impact:** Can add transaction with empty description, but cannot edit it.

---

### 10. Lost Success/Error Messages
**Files:** Multiple ViewModels
**Severity:** MEDIUM | **Type:** State Management

Message clearing via delayed coroutine not tied to screen lifecycle.

**Impact:** Messages sometimes don't appear or appear for wrong action.

---

## 🟡 MODERATE BUGS

### 11. Navigation Race Condition
**File:** `AppNavigation.kt:40-50`
**Severity:** MEDIUM | **Type:** Navigation

ViewModel and transaction ID loading happens separately, can race.

---

### 12. No Empty State Messages
**File:** `TransactionListScreen.kt`
**Severity:** LOW | **Type:** UX

Should display "No transactions found" when list empty.

---

### 13. Missing Account Selection UI
**File:** `AddTransactionScreen.kt`
**Severity:** LOW | **Type:** Incomplete Feature

Account selector UI missing. Transactions always go to default account.

---

### 14. Filter Clear Doesn't Work
**File:** `TransactionViewModel.kt:91-107`
**Severity:** LOW | **Type:** Logic Bug

Clearing filters doesn't reset results properly.

---

### 15. Keyboard Not Hidden After Submit
**Files:** Multiple screens
**Severity:** LOW | **Type:** UX

Keyboard stays open after form submission.

---

### 16. Hard-coded Budget Threshold
**File:** `BudgetViewModel.kt:76`
**Severity:** LOW | **Type:** Configuration

Alert threshold hardcoded to 80%. Should be user-configurable.

---

### 17. No Search Debounce
**File:** `TransactionViewModel.kt:68-71`
**Severity:** LOW | **Type:** Performance

Filtering runs on every keystroke. Needs debounce.

---

### 18. Missing Accessibility Labels
**Severity:** LOW | **Type:** Accessibility

No content descriptions for decorative icons.

---

## 🔒 SECURITY ISSUES

### S1. Hard-coded Keystore Password
**File:** `build.gradle.kts:29-34`
**Severity:** MEDIUM

```kotlin
?: "financeapp123"  // ❌ FALLBACK PASSWORD EXPOSED
```

**Impact:** Anyone with source code can sign APKs with app's private key.

---

### S2. Backup Enabled for Sensitive Data
**File:** `AndroidManifest.xml:4`
**Severity:** MEDIUM

```xml
android:allowBackup="true"  // ❌ Allows 'adb backup' extraction
```

**Impact:** Financial data can be extracted without root.

---

### S3. Unencrypted Database
**Severity:** MEDIUM

All financial data stored in plaintext. Readable if device rooted.

**Impact:** Violates privacy expectations for sensitive financial data.

---

## 📊 BUG SUMMARY

| # | Title | Severity | Category |
|---|-------|----------|----------|
| 1 | Dark mode not wired | HIGH | State Mgmt |
| 2 | runBlocking ANR | CRITICAL | Performance |
| 3 | Silent exceptions | HIGH | Error Handling |
| 4 | Destructive migration | HIGH | Data Loss |
| 5 | Race condition | HIGH | State Mgmt |
| 6 | NPE null check | HIGH | Crash Risk |
| 7 | Wrong month filter | MEDIUM | Logic |
| 8 | Memory leak | MEDIUM | Memory |
| 9 | Validation inconsistent | MEDIUM | Validation |
| 10 | Lost messages | MEDIUM | UX |
| 11 | Navigation race | MEDIUM | Navigation |
| 12 | No empty state | LOW | UX |
| 13 | Missing account UI | LOW | Feature |
| 14 | Filter clear | LOW | Logic |
| 15 | Keyboard remains | LOW | UX |
| 16 | Hard-coded threshold | LOW | Config |
| 17 | No search debounce | LOW | Performance |
| 18 | No a11y labels | LOW | Accessibility |
| S1 | Hard-coded password | MEDIUM | Security |
| S2 | Backup enabled | MEDIUM | Security |
| S3 | Unencrypted data | MEDIUM | Security |

---

## 🔧 FIX PRIORITY

### 🚨 IMMEDIATE (Production Blockers)
1. Bug #2 - runBlocking ANR crashes
2. Bug #4 - Destructive migration loses data
3. Bug #6 - NPE crash on edit
4. Bug #3 - Silent failures
5. S1 - Hard-coded credentials
6. S2 - Backup security

### ⚠️ HIGH (First release)
1. Bug #1 - Dark mode broken
2. Bug #5 - Race condition
3. Bug #7 - Wrong calculations
4. S3 - Encryption

### 📋 MEDIUM (Next sprint)
1. Bugs #8-11 - Memory, validation, navigation

### 💡 LOW (Polish)
- Bugs #12-18 - UX and accessibility
