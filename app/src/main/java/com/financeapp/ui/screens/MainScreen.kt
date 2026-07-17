package com.financeapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.financeapp.ui.navigation.NavigationRoutes
import com.financeapp.ui.viewmodel.BudgetViewModel
import com.financeapp.ui.viewmodel.DashboardViewModel
import com.financeapp.ui.viewmodel.ReportViewModel
import com.financeapp.ui.viewmodel.TransactionViewModel

@Composable
fun MainScreen(
    dashboardViewModel: DashboardViewModel,
    transactionViewModel: TransactionViewModel,
    reportViewModel: ReportViewModel,
    budgetViewModel: BudgetViewModel,
    navController: NavHostController
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Text("📊") },
                    label = { Text("Dashboard") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Text("📝") },
                    label = { Text("Transaksi") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Text("📈") },
                    label = { Text("Laporan") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { Text("💰") },
                    label = { Text("Budget") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
                NavigationBarItem(
                    icon = { Text("⚙️") },
                    label = { Text("Pengaturan") },
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(NavigationRoutes.AddTransaction::class.simpleName ?: "AddTransaction")
                    }
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
                0 -> DashboardScreen(viewModel = dashboardViewModel)
                1 -> TransactionListScreen(
                    viewModel = transactionViewModel,
                    onTransactionClick = { id ->
                        navController.navigate("edit_transaction/$id")
                    }
                )
                2 -> ReportScreen(viewModel = reportViewModel)
                3 -> BudgetScreen(viewModel = budgetViewModel)
                4 -> SettingsScreen()
            }
        }
    }
}
