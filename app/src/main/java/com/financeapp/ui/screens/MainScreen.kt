package com.financeapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import com.financeapp.ui.navigation.NavigationRoutes

private data class NavigationItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainScreen(
    navController: NavHostController
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val navigationItems = listOf(
        NavigationItem("Dashboard", Icons.Filled.Home, Icons.Outlined.Home),
        NavigationItem("Transaksi", Icons.Filled.Payments, Icons.Outlined.Payments),
        NavigationItem("Laporan", Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp),
        NavigationItem("Budget", Icons.Filled.Savings, Icons.Outlined.Savings),
        NavigationItem("Pengaturan", Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                navigationItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(NavigationRoutes.AddTransaction::class.simpleName ?: "AddTransaction")
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Tambah Transaksi")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen()
                1 -> TransactionListScreen(
                    onTransactionClick = { id ->
                        navController.navigate("edit_transaction/$id")
                    }
                )
                2 -> ReportScreen()
                3 -> BudgetScreen()
                4 -> SettingsScreen()
            }
        }
    }
}
