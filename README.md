# Finance App

Aplikasi mobile Android untuk manajemen keuangan pribadi yang dibangun dengan Kotlin dan Jetpack Compose.

## Fitur
- Dashboard keuangan
- Pencatatan transaksi (pemasukan/pengeluaran)
- Kategorisasi pengeluaran
- Laporan bulanan
- Pengingat pembayaran

## Tech Stack
- **Bahasa**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room Database
- **Navigation**: Jetpack Navigation Compose
- **Build System**: Gradle

## Struktur Project
```
FinanceApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/financeapp/
│   │   │   ├── MainActivity.kt
│   │   │   ├── ui/
│   │   │   │   ├── screens/
│   │   │   │   └── theme/
│   │   │   └── data/
│   │   │       ├── model/
│   │   │       └── database/
│   │   └── res/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Cara Menjalankan
```bash
# Build project
./gradlew build

# Run di emulator
./gradlew installDebug

# Run tests
./gradlew test
```

## Rencana Development
- [ ] Setup project dasar
- [ ] Database schema
- [ ] Home screen
- [ ] Transaction input screen
- [ ] Analytics screen
- [ ] Settings screen
