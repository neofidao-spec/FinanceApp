# FinanceApp — Financial Calculation Audit

## Temuan

| # | File:Line | Issue | Severity | Fix |
|---|-----------|-------|----------|-----|
| 1 | RecurringTransactionWorker:33-58 | Worker NOT idempotent — process kill = duplicate transactions | CRITICAL | Wrap insert+advance in @Transaction |
| 2 | GamificationUseCase:156-181 | addXp() TOCTOU race — concurrent calls lose XP | HIGH | Use Mutex for atomic read-modify-write |
| 3 | GetHealthScoreUseCase:42-48 | Double precision OK (coerceIn/toInt prevents display issues) | LOW | No fix needed |
| 4 | BudgetRepository:52-55 | Negative monthlyLimit not validated | MEDIUM | Add validation in addBudget |
| 5 | Transaction amount | Negative amount not validated | MEDIUM | Add validation in addTransaction |
| 6 | RecurringTransactionWorker:54-58 | Dead code — both branches return Result.success() | LOW | Return Result.retry() when generated==0 |
| 7 | AccountRepository:28-53 | initializeDefaultAccounts() dead code (DatabaseModule handles it) | LOW | Remove |

## Detail

### Bug #1 — CRITICAL: Recurring Transaction Duplicate Risk

Lokasi: `RecurringTransactionWorker.kt:33-58`

```kotlin
for (recurring in dueTransactions) {
    transactionRepo.addTransaction(transaction)  // step 1
    recurringRepo.advanceOccurrence(recurring)    // step 2
    // If process killed between step 1 and step 2,
    // next run will re-generate this transaction
}
```

Impact: Jika device mati/OS kill setelah step 1 tapi sebelum step 2, transaksi yang sama akan di-generate lagi di run berikutnya.

Fix: Bungkus dalam database transaction + tambah idempotency check.

### Bug #2 — HIGH: addXp() Race Condition

Lokasi: `GamificationUseCase.kt:156-181`

```kotlin
val progress = repository.getUserProgressOnce()  // read T1
val newTotal = progress.totalXp + amount          // modify T2
repository.saveUserProgress(progress.copy(...))   // write T3
```

Impact: Jika dua coroutine memanggil addXp() bersamaan, XP bisa hilang.

Fix: Gunakan Mutex untuk atomic read-modify-write.
