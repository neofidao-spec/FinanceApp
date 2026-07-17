package com.financeapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String, // Keep for backward compatibility
    val iconName: String = "", // Material Icon name
    val type: TransactionType,
    val color: String
)

object DefaultCategories {
    fun getDefault(): List<Category> = listOf(
        // Income
        Category(1, "Gaji", "💰", "Work", TransactionType.INCOME, "#2E7D32"),
        Category(2, "Bonus", "🎁", "CardGiftcard", TransactionType.INCOME, "#43A047"),
        Category(3, "Investasi", "📈", "TrendingUp", TransactionType.INCOME, "#66BB6A"),
        Category(4, "Lainnya", "⭐", "AttachMoney", TransactionType.INCOME, "#81C784"),
        
        // Expense
        Category(5, "Makanan", "🍔", "LocalDining", TransactionType.EXPENSE, "#E65100"),
        Category(6, "Transportasi", "🚗", "DirectionsBus", TransactionType.EXPENSE, "#1565C0"),
        Category(7, "Hiburan", "🎬", "Movie", TransactionType.EXPENSE, "#7B1FA2"),
        Category(8, "Belanja", "🛍️", "ShoppingBag", TransactionType.EXPENSE, "#C62828"),
        Category(9, "Utilities", "💡", "Lightbulb", TransactionType.EXPENSE, "#00838F"),
        Category(10, "Kesehatan", "🏥", "LocalHospital", TransactionType.EXPENSE, "#2E7D32"),
        Category(11, "Pendidikan", "📚", "School", TransactionType.EXPENSE, "#4527A0"),
        Category(12, "Lainnya", "⭐", "Category", TransactionType.EXPENSE, "#757575")
    )
}
