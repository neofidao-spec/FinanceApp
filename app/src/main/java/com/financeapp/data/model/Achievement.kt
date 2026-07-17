package com.financeapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val icon: String,
    val category: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: LocalDateTime? = null
) {
    val progress: Float
        get() = if (targetValue > 0) (currentValue.toFloat() / targetValue).coerceIn(0f, 1f) else 0f
}

enum class AchievementCategory {
    CONSISTENCY,
    BUDGET,
    SAVINGS,
    TRANSACTIONS
}

object DefaultAchievements {
    fun getDefault(): List<Achievement> = listOf(
        Achievement(
            name = "Pencatat Pemula",
            description = "Catat 5 transaksi",
            icon = "edit_note",
            category = AchievementCategory.TRANSACTIONS.name,
            targetValue = 5
        ),
        Achievement(
            name = "Pencatat Setia",
            description = "Catat 30 transaksi",
            icon = "menu_book",
            category = AchievementCategory.TRANSACTIONS.name,
            targetValue = 30
        ),
        Achievement(
            name = "Master Transaksi",
            description = "Catat 100 transaksi",
            icon = "military_tech",
            category = AchievementCategory.TRANSACTIONS.name,
            targetValue = 100
        ),
        Achievement(
            name = "Hemat Mingguan",
            description = "Tidak overspend selama 1 minggu",
            icon = "savings",
            category = AchievementCategory.BUDGET.name,
            targetValue = 7
        ),
        Achievement(
            name = "Budget Master",
            description = "Di bawah budget selama 3 bulan",
            icon = "workspace_premium",
            category = AchievementCategory.BUDGET.name,
            targetValue = 3
        ),
        Achievement(
            name = "First Save",
            description = "Dapatkan pemasukan pertama",
            icon = "eco",
            category = AchievementCategory.SAVINGS.name,
            targetValue = 1
        ),
        Achievement(
            name = "Jutawan",
            description = "Saldo mencapai Rp 1.000.000",
            icon = "diamond",
            category = AchievementCategory.SAVINGS.name,
            targetValue = 1000000
        ),
        Achievement(
            name = "Konsisten 7 Hari",
            description = "Catat transaksi 7 hari berturut-turut",
            icon = "local_fire_department",
            category = AchievementCategory.CONSISTENCY.name,
            targetValue = 7
        ),
        Achievement(
            name = "Konsisten 30 Hari",
            description = "Catat transaksi 30 hari berturut-turut",
            icon = "bolt",
            category = AchievementCategory.CONSISTENCY.name,
            targetValue = 30
        )
    )
}
