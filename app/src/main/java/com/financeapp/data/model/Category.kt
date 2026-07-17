package com.financeapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String,
    val type: TransactionType,
    val color: String
)

object DefaultCategories {
    fun getDefault(): List<Category> = listOf(
        // Income
        Category(1, "Gaji", "💰", TransactionType.INCOME, "#4CAF50"),
        Category(2, "Bonus", "🎁", TransactionType.INCOME, "#8BC34A"),
        Category(3, "Investasi", "📈", TransactionType.INCOME, "#2196F3"),
        Category(4, "Lainnya", "⭐", TransactionType.INCOME, "#FF9800"),
        
        // Expense
        Category(5, "Makanan", "🍔", TransactionType.EXPENSE, "#FF5722"),
        Category(6, "Transportasi", "🚗", TransactionType.EXPENSE, "#9C27B0"),
        Category(7, "Hiburan", "🎬", TransactionType.EXPENSE, "#E91E63"),
        Category(8, "Belanja", "🛍️", TransactionType.EXPENSE, "#3F51B5"),
        Category(9, "Utilities", "💡", TransactionType.EXPENSE, "#00BCD4"),
        Category(10, "Kesehatan", "🏥", TransactionType.EXPENSE, "#009688"),
        Category(11, "Pendidikan", "📚", TransactionType.EXPENSE, "#673AB7"),
        Category(12, "Lainnya", "⭐", TransactionType.EXPENSE, "#607D8B")
    )
}
