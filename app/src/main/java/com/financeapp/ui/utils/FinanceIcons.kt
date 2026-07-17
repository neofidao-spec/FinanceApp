package com.financeapp.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Premium icon mapping for finance categories.
 * Uses Material Icons instead of emojis for a professional look.
 */
object FinanceIcons {
    
    // Category icon mapping
    private val categoryIconMap = mapOf(
        // Income categories
        "Gaji" to Icons.Filled.Work,
        "Bonus" to Icons.Filled.CardGiftcard,
        "Investasi" to Icons.Filled.TrendingUp,
        "Lainnya" to Icons.Filled.AttachMoney,
        
        // Expense categories
        "Makanan" to Icons.Filled.LocalDining,
        "Transportasi" to Icons.Filled.DirectionsBus,
        "Hiburan" to Icons.Filled.Movie,
        "Belanja" to Icons.Filled.ShoppingBag,
        "Utilities" to Icons.Filled.Lightbulb,
        "Kesehatan" to Icons.Filled.LocalHospital,
        "Pendidikan" to Icons.Filled.School,
        
        // Account types
        "Cash" to Icons.Filled.Payments,
        "Bank" to Icons.Filled.AccountBalance,
        "E-Wallet" to Icons.Filled.AccountBalanceWallet,
        "Credit Card" to Icons.Filled.CardGiftcard,
        
        // General
        "Default" to Icons.Filled.Category
    )
    
    // Category color mapping
    private val categoryColorMap = mapOf(
        // Income - Greens
        "Gaji" to Color(0xFF2E7D32),
        "Bonus" to Color(0xFF43A047),
        "Investasi" to Color(0xFF66BB6A),
        "Lainnya" to Color(0xFF81C784),
        
        // Expense - Distinct colors
        "Makanan" to Color(0xFFE65100),
        "Transportasi" to Color(0xFF1565C0),
        "Hiburan" to Color(0xFF7B1FA2),
        "Belanja" to Color(0xFFC62828),
        "Utilities" to Color(0xFF00838F),
        "Kesehatan" to Color(0xFF2E7D32),
        "Pendidikan" to Color(0xFF4527A0),
        
        // Account colors
        "Cash" to Color(0xFF2E7D32),
        "Bank" to Color(0xFF1565C0),
        "E-Wallet" to Color(0xFFE65100),
        "Credit Card" to Color(0xFF7B1FA2)
    )
    
    /**
     * Get Material Icon for a category name
     */
    fun getIcon(categoryName: String): ImageVector {
        return categoryIconMap[categoryName] ?: Icons.Filled.Category
    }
    
    /**
     * Get color for a category name
     */
    fun getColor(categoryName: String): Color {
        return categoryColorMap[categoryName] ?: Color(0xFF757575)
    }
    
    /**
     * Get color from hex string
     */
    fun getColorFromHex(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color(0xFF757575)
        }
    }
}
