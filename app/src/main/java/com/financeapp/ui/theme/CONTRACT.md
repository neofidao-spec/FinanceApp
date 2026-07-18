# FinanceApp — Design System Contract

**Status:** BINDING — semua screen dan component WAJIB mengikuti aturan ini.

---

## 1. Spacing

Semua padding, margin, gap HARUS dari `Spacing.*`:

```kotlin
// BENAR
Modifier.padding(Spacing.md)
Spacer(modifier = Modifier.height(Spacing.sm))

// SALAH — dilarang hardcoded dp
Modifier.padding(16.dp)
Spacer(modifier = Modifier.height(8.dp)
```

| Token | Value | Penggunaan |
|-------|-------|------------|
| `Spacing.xs` | 4.dp | Gap antar icon, padding chip kecil |
| `Spacing.sm` | 8.dp | Gap antar item terkait, inner padding |
| `Spacing.md` | 16.dp | Padding konten standar, card padding |
| `Spacing.lg` | 24.dp | Separator section, screen padding atas |
| `Spacing.xl` | 32.dp | Gap section besar, hero element |
| `Spacing.xxl` | 48.dp | Margin tepi screen, divider mayor |

---

## 2. Colors

Semua warna HARUS dari `MaterialTheme.colorScheme.*` atau `MaterialTheme.financeColors.*`:

```kotlin
// BENAR
color = MaterialTheme.financeColors.income
color = MaterialTheme.colorScheme.onSurface

// SALAH — dilarang hardcoded Color
color = Color(0xFF2E7D32)
color = Color.Green
color = Color.Gray
```

### Semantic Finance Colors

| Token | Light | Dark | Penggunaan |
|-------|-------|------|------------|
| `financeColors.income` | Hijau gelap | Hijau terang | Angka pemasukan, saldo positif |
| `financeColors.onIncome` | Putih | Hijau gelap | Teks di atas income |
| `financeColors.incomeContainer` | Hijau muda | Hijau gelap | Background card income |
| `financeColors.onIncomeContainer` | Hijau sangat gelap | Hijau muda | Teks di container income |
| `financeColors.expense` | Merah gelap | Merah terang | Angka pengeluaran, saldo negatif |
| `financeColors.onExpense` | Putih | Merah gelap | Teks di atas expense |
| `financeColors.expenseContainer` | Merah muda | Merah gelap | Background card expense |
| `financeColors.onExpenseContainer` | Merah sangat gelap | Merah muda | Teks di container expense |
| `financeColors.warning` | Kuning/amber | Kuning terang | Budget mendekati limit |
| `financeColors.onWarning` | Putih | Amber gelap | Teks di atas warning |
| `financeColors.warningContainer` | Kuning sangat muda | Amber gelap | Background warning |
| `financeColors.onWarningContainer` | Amber gelap | Kuning muda | Teks di warning container |

### Material3 Colors (sudah ada)

| Token | Penggunaan |
|-------|------------|
| `colorScheme.primary` | Tombol utama, link, FAB |
| `colorScheme.onPrimary` | Teks di atas primary |
| `colorScheme.error` | Error state, delete action |
| `colorScheme.surface` | Background card, sheet |
| `colorScheme.onSurface` | Teks utama |
| `colorScheme.onSurfaceVariant` | Teks sekunder, label |
| `colorScheme.outline` | Border, divider |

---

## 3. Typography

Semua teks HARUS dari `MaterialTheme.typography.*`:

```kotlin
// BENAR
Text("Rp 1.000.000", style = MaterialTheme.typography.displayMedium)

// SALAH — dilarang hardcoded fontSize
Text("Rp 1.000.000", fontSize = 28.sp, fontWeight = FontWeight.Bold)
```

| Role | Size | Weight | Tracking | Penggunaan |
|------|------|--------|----------|------------|
| `displayLarge` | 34sp | Bold | 0 | Hero balance di dashboard |
| `displayMedium` | 28sp | Bold | 0 | Balance card utama |
| `displaySmall` | 24sp | Bold | 0 | Screen title besar |
| `headlineLarge` | 22sp | Bold | -0.2 | Section title, angka besar |
| `headlineMedium` | 20sp | SemiBold | 0 | Subsection title |
| `headlineSmall` | 18sp | SemiBold | 0 | Card title besar |
| `titleLarge` | 20sp | Bold | -0.2 | Amount di list item |
| `titleMedium` | 16sp | Medium | 0.1 | Card title, input label |
| `titleSmall` | 14sp | Medium | 0.1 | List item title |
| `bodyLarge` | 16sp | Normal | 0.5 | Teks utama, deskripsi |
| `bodyMedium` | 14sp | Normal | 0.25 | Teks sekunder |
| `bodySmall` | 12sp | Normal | 0.4 | Caption, timestamp |
| `labelLarge` | 14sp | Medium | 0.1 | Button text, chip label |
| `labelMedium` | 12sp | Medium | 0.5 | Section header kecil |
| `labelSmall` | 10sp | Medium | 0.5 | Badge text, overline |

---

## 4. Shapes

Semua bentuk HARUS dari `MaterialTheme.shapes.*`:

```kotlin
// BENAR
Card(shape = MaterialTheme.shapes.medium)

// SALAH — dilarang hardcoded shape
Card(shape = RoundedCornerShape(16.dp))
```

| Token | Radius | Penggunaan |
|-------|--------|------------|
| `shapes.extraSmall` | 8.dp | Chip, badge, tag kecil |
| `shapes.small` | 12.dp | Input field, list item, card kecil |
| `shapes.medium` | 16.dp | Card utama, dialog |
| `shapes.large` | 24.dp | Bottom sheet, dialog besar |

---

## Enforcement

- Code review WAJIB reject PR yang melanggar aturan di atas
- `grep -rn 'Color(0x\|\.dp)' app/src/main/java/com/financeapp/ui/screens/` harus 0 hasil
- `grep -rn 'fontSize.*sp' app/src/main/java/com/financeapp/ui/screens/` harus 0 hasil
- `grep -rn 'RoundedCornerShape' app/src/main/java/com/financeapp/ui/screens/` harus 0 hasil
