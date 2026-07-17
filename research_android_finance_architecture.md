# Rekomendasi Arsitektur Android Finance App 2024-2025
## Studi Kasus: Ivy Wallet (3.2k ⭐), MyExpenses (1.2k ⭐), Mifos Mobile (358 ⭐)

---

## 1. MVVM vs MVI untuk Jetpack Compose

### ✅ Rekomendasi: **MVI (Model-View-Intent) untuk Compose**

**Alasan:**
- Compose secara alami adalah *declarative* → MVI (unidirectional data flow) lebih cocok daripada MVVM klasik
- State tunggal (`StateFlow`) → mudah di-debug, di-test, dan di-replay
- Ivy Wallet menggunakan pola **ViewModel + StateFlow + UseCase** yang mendekati MVI
- Mifos Mobile menggunakan **Clean Architecture + MVI** dengan `UiState` sealed class

**Pola yang terbukti dari open-source apps:**

```kotlin
// Pattern dari Ivy Wallet & Mifos Mobile
class TransactionViewModel(...) : ViewModel() {
    // Single UI State
    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()
    
    // Intent handling via functions (bukan sealed class events)
    fun onAmountChanged(amount: Double) {
        _uiState.update { it.copy(amount = amount) }
    }
    
    fun onSaveClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            saveTransactionUseCase(_uiState.value.toTransaction())
                .onSuccess { _uiState.update { it.copy(isSaved = true) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }
}

// UI State - data class dengan copy()
data class TransactionUiState(
    val amount: Double = 0.0,
    val category: Category? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)
```

**Kapan pakai MVVM klasik?** Hanya untuk screen sangat sederhana (< 3 state fields, 1-2 event).

---

## 2. Repository Pattern + Kotlin Flow Best Practices

### Pola terbukti dari Ivy Wallet & Mifos:

```kotlin
// Repository mengembalikan Flow (bukan suspend function untuk data observasi)
interface TransactionRepository {
    fun getTransactions(accountId: String): Flow<List<Transaction>>
    suspend fun insert(transaction: Transaction)
    suspend fun delete(id: String)
}

// Implementasi dengan Room (auto-emit saat data berubah)
class TransactionRepositoryImpl(
    private val dao: TransactionDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TransactionRepository {
    
    override fun getTransactions(accountId: String): Flow<List<Transaction>> =
        dao.getTransactionsByAccount(accountId)  // Room Flow auto-update
            .flowOn(dispatcher)
    
    override suspend fun insert(transaction: Transaction) =
        withContext(dispatcher) { dao.insert(transaction.toEntity()) }
}
```

### Best Practices:
1. **`Flow` untuk data observasi**, `suspend fun` untuk one-shot operations
2. **`stateIn()` di ViewModel** untuk mengkonversi cold Flow → hot StateFlow:
   ```kotlin
   val transactions: StateFlow<List<Transaction>> = 
       repository.getTransactions(accountId)
           .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
   ```
3. **`WhileSubscribed(5000)`** — delay 5 detik sebelum stop collecting (handle rotasi layar)
4. **Error handling di Repository**, bukan di ViewModel:
   ```kotlin
   fun getTransactions(): Flow<Resource<List<Transaction>>> = flow {
       emit(Resource.Loading)
       try {
           dao.getAll().collect { emit(Resource.Success(it)) }
       } catch (e: Exception) {
           emit(Resource.Error(e))
       }
   }
   ```
5. **Single source of truth**: Room DB = source of truth, API = updater

---

## 3. Room Database Advanced Patterns

### A. FTS (Full-Text Search) untuk pencarian transaksi

```kotlin
// Entity FTS
@Fts4(contentEntity = TransactionEntity::class)
@Entity(tableName = "transactions_fts")
data class TransactionFts(
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "note") val note: String
)

// DAO
@Dao
interface TransactionDao {
    @Query("""
        SELECT t.* FROM transactions t 
        JOIN transactions_fts fts ON t.rowid = fts.rowid 
        WHERE transactions_fts MATCH :query
        ORDER BY t.date DESC
    """)
    fun search(query: String): Flow<List<TransactionEntity>>
}
```

### B. Relations (Relasi antar tabel)

```kotlin
// Relasi 1-to-Many
data class AccountWithTransactions(
    @Embedded val account: AccountEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "account_id"
    )
    val transactions: List<TransactionEntity>
)

// Relasi Many-to-Many via junction table
data class TransactionWithTags(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TransactionTagCrossRef::class,
            parentColumn = "transaction_id",
            entityColumn = "tag_id"
        )
    )
    val tags: List<TagEntity>
)
```

### C. Migrations (dari Ivy Wallet yang punya 130+ versi DB)

```kotlin
// AutoMigration (Room 2.4+)
@Database(
    version = 130,
    autoMigrations = [
        AutoMigration(from = 129, to = 130)
    ]
)

// Manual Migration untuk perubahan kompleks
val MIGRATION_128_129 = object : Migration(128, 129) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE transactions ADD COLUMN note TEXT DEFAULT NULL")
    }
}

// Testing migrations
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )
    
    @Test
    fun migrate129To130() {
        helper.createDatabase("test-db", 129).use { db ->
            db.execSQL("INSERT INTO transactions (amount) VALUES (100)")
        }
        helper.runMigrationsAndValidate("test-db", 130, true, MIGRATION_129_130)
    }
}
```

### D. TypeConverters untuk tipe data kompleks

```kotlin
class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long = date.toEpochDay()
    
    @TypeConverter
    fun toLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)
    
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal): String = value.toPlainString()
    
    @TypeConverter
    fun toBigDecimal(value: String): BigDecimal = value.toBigDecimal()
}
```

---

## 4. Compose Animation Patterns untuk Financial UIs

### A. Animated Numbers (Counter Animation)

```kotlin
@Composable
fun AnimatedBalance(amount: Double) {
    val animatedAmount by animateFloatAsState(
        targetValue = amount.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "balance"
    )
    Text(
        text = formatCurrency(animatedAmount.toDouble()),
        style = MaterialTheme.typography.headlineLarge
    )
}

// Alternatif: AnimatedContent untuk perubahan besar
@Composable
fun AnimatedTotalAmount(total: String) {
    AnimatedContent(
        targetState = total,
        transitionSpec = {
            slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
        },
        label = "total"
    ) { targetTotal ->
        Text(text = targetTotal, style = MaterialTheme.typography.displaySmall)
    }
}
```

### B. Animated Charts (Line/Bar)

```kotlin
@Composable
fun AnimatedLineChart(data: List<Double>) {
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f))
    )
    
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val path = Path()
        data.forEachIndexed { index, value ->
            val x = (index / data.lastIndex.toFloat()) * size.width * progress
            val y = size.height - (value / data.max()) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = Color.Green, style = Stroke(width = 3.dp.toPx()))
    }
}
```

### C. Transition Animations antar Screen

```kotlin
// NavHost dengan custom transitions
NavHost(navController, startDestination = "home") {
    composable("home",
        enterTransition = { fadeIn() + slideInHorizontally() },
        exitTransition = { fadeOut() + slideOutHorizontally() }
    ) { HomeScreen() }
    
    composable("transaction/{id}",
        enterTransition = { slideInVertically(initialOffsetY = { it }) },
        exitTransition = { slideOutVertically(targetOffsetY = { it }) }
    ) { backStackEntry ->
        TransactionScreen(backStackEntry.arguments?.getString("id"))
    }
}
```

---

## 5. Material 3 Design Tokens untuk Finance Apps

### Palet Warna yang Cocok:

```kotlin
// Light Theme
val FinanceLightColors = lightColorScheme(
    primary = Color(0xFF1B5E20),        // Hijau tua (income/positive)
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA5D6A7),
    secondary = Color(0xFFC62828),      // Merah (expense/negative)
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEF9A9A),
    surface = Color(0xFFFFFBFE),
    surfaceVariant = Color(0xFFF5F5F5),  // Card backgrounds
    outline = Color(0xFFE0E0E0),         // Dividers
)

// Dark Theme
val FinanceDarkColors = darkColorScheme(
    primary = Color(0xFF81C784),         // Hijau muda
    onPrimary = Color(0xFF003300),
    primaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFFEF9A9A),       // Merah muda
    onSecondary = Color(0xFF4A0000),
    secondaryContainer = Color(0xFFC62828),
    surface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFF2C2C2C),
)
```

### Typography untuk Angka Keuangan:

```kotlin
val FinanceTypography = Typography(
    // Angka besar (saldo, total)
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,  // Monospace agar angka rapi
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    // Label kecil
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
)
```

### Component Tokens:

```kotlin
// Transaction Card
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ),
    shape = RoundedCornerShape(16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
)

// Amount Chip (Income/Expense indicator)
SuggestionChip(
    onClick = {},
    label = { Text("-Rp 150.000") },
    colors = SuggestionChipDefaults.suggestionChipColors(
        containerColor = MaterialTheme.colorScheme.errorContainer,
        labelColor = MaterialTheme.colorScheme.onErrorContainer
    )
)
```

---

## 6. Dark Mode Best Practices

### Implementasi:

```kotlin
// Theme.kt
@Composable
fun FinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,  // Material You (Android 12+)
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) 
            else dynamicLightColorScheme(context)
        }
        darkTheme -> FinanceDarkColors
        else -> FinanceLightColors
    }
    
    // Edge-to-edge support
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    MaterialTheme(colorScheme = colorScheme, content = content)
}
```

### Prinsip untuk Finance Apps:
1. **Jangan pakai warna hardcoded** — selalu via `MaterialTheme.colorScheme`
2. **Angka negatif**: `errorContainer` (merah muda di dark, merah terang di light)
3. **Angka positif**: `primaryContainer` (hijau)
4. **Chart colors**: definisikan terpisah untuk dark/light, karena kontras berbeda
5. **Amoled black**: opsional, tapi `Color(0xFF000000)` untuk background utama hemat baterai
6. **Test dengan `uiMode`** di emulator: Settings > Developer Options > Dark mode

---

## 7. Performance Optimization untuk List-Heavy Screens

### A. LazyColumn dengan Key

```kotlin
LazyColumn {
    items(
        items = transactions,
        key = { it.id },  // CRITICAL: stable keys untuk animasi & recomposition
        contentType = { "transaction" }
    ) { transaction ->
        TransactionItem(transaction)
    }
}
```

### B. Stable Data Classes

```kotlin
// Tandai @Stable agar Compose skip recomposition jika data sama
@Stable
data class TransactionUi(
    val id: String,
    val title: String,
    val amount: String,  // Sudah di-format, bukan raw double
    val date: String,
    val categoryIcon: Int
)
```

### C. Derived State untuk Computed Values

```kotlin
@Composable
fun TransactionList(transactions: List<Transaction>) {
    val groupedTransactions by remember(transactions) {
        derivedStateOf {
            transactions.groupBy { it.date.toLocalDate() }
        }
    }
    // groupedTransactions hanya di-recompute saat transactions berubah
}
```

### D. Paging untuk Data Besar

```kotlin
// Repository
fun getTransactionsPaged(): Flow<PagingData<TransactionEntity>> =
    Pager(config = PagingConfig(
        pageSize = 20,
        enablePlaceholders = false,
        prefetchDistance = 5
    )) {
        dao.getAllPaged()
    }.flow

// ViewModel
val transactions: Flow<PagingData<Transaction>> = 
    repository.getTransactionsPaged()
        .map { pagingData -> pagingData.map { it.toDomain() } }
        .cachedIn(viewModelScope)

// Composable
@Composable
fun TransactionList(transactions: LazyPagingItems<Transaction>) {
    LazyColumn {
        items(transactions, key = { it.id }) { transaction ->
            transaction?.let { TransactionItem(it) }
        }
        // Loading indicator
        when (transactions.loadState.append) {
            is LoadState.Loading -> item { CircularProgressIndicator() }
            else -> {}
        }
    }
}
```

### E. Optimasi Lainnya:

1. **`remember`** untuk object allocation yang mahal
2. **`deferredState`** (Compose 1.7+) untuk state yang jarang berubah
3. **Baseline Profiles** untuk percepat startup
4. **Avoid `LaunchedEffect(Unit)`** di item list — pindahkan ke ViewModel
5. **Shimmer loading** daripada `CircularProgressIndicator` untuk UX lebih baik
6. **Debounce input** pencarian (300ms) sebelum query Room

---

## Summary Arsitektur yang Direkomendasikan

```
┌─────────────────────────────────────────────────┐
│                 UI Layer (Compose)               │
│  Screen → ViewModel (StateFlow<UiState>)        │
│  MVI Pattern: UiState + Intent Functions        │
├─────────────────────────────────────────────────┤
│               Domain Layer                       │
│  UseCases (suspend fun / Flow return)           │
│  Business Logic, Validation, Aggregation         │
├─────────────────────────────────────────────────┤
│               Data Layer                          │
│  Repository (Flow + suspend)                     │
│  Room DAO (Flow queries) + Remote DataSource    │
│  Single Source of Truth = Room DB               │
└─────────────────────────────────────────────────┘
```

### Tech Stack yang Terbukti:
- **Language**: Kotlin 2.0+
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVI (Clean Architecture)
- **DI**: Hilt (recommended) atau Koin
- **DB**: Room 2.6+ (FTS4, AutoMigration)
- **Async**: Kotlin Coroutines + Flow
- **Navigation**: Compose Navigation (type-safe args di Kotlin 2.0)
- **Paging**: Paging 3 + Compose integration
- **Testing**: Turbine (Flow testing), MockK, Compose UI Test
