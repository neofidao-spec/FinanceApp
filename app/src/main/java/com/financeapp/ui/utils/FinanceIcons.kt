package com.financeapp.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
    
    // Navigation icons
    object Navigation {
        val Dashboard = Icons.Filled.Home
        val Transactions = Icons.Filled.Payments
        val Reports = Icons.Filled.TrendingUp
        val Budget = Icons.Filled.Savings
        val Settings = Icons.Filled.Settings
    }
    
    // Action icons
    object Action {
        val Add = Icons.Filled.Add
        val Edit = Icons.Filled.Edit
        val Delete = Icons.Filled.Delete
        val Search = Icons.Filled.Search
        val Filter = Icons.Filled.FilterList
        val ArrowBack = Icons.Filled.ArrowBack
        val ArrowForward = Icons.Filled.ArrowForward
        val Check = Icons.Filled.Check
        val Close = Icons.Filled.Close
    }
    
    // Status icons
    object Status {
        val Success = Icons.Filled.CheckCircle
        val Warning = Icons.Filled.Warning
        val Error = Icons.Filled.Error
        val Info = Icons.Filled.Info
    }
}
