package com.financeapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.financeapp.ui.screens.AddTransactionScreen
import com.financeapp.ui.screens.BudgetScreen
import com.financeapp.ui.screens.EditTransactionScreen
import com.financeapp.ui.screens.MainScreen
import com.financeapp.ui.viewmodel.AddTransactionViewModel
import com.financeapp.ui.viewmodel.BudgetViewModel
import com.financeapp.ui.viewmodel.DashboardViewModel
import com.financeapp.ui.viewmodel.EditTransactionViewModel
import com.financeapp.ui.viewmodel.ReportViewModel
import com.financeapp.ui.viewmodel.TransactionViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    transactionViewModel: TransactionViewModel,
    addTransactionViewModel: AddTransactionViewModel,
    editTransactionViewModel: EditTransactionViewModel,
    reportViewModel: ReportViewModel,
    budgetViewModel: BudgetViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.Main::class.simpleName ?: "Main"
    ) {
        composable(NavigationRoutes.Main::class.simpleName ?: "Main") {
            MainScreen(
                dashboardViewModel = dashboardViewModel,
                transactionViewModel = transactionViewModel,
                reportViewModel = reportViewModel,
                budgetViewModel = budgetViewModel,
                navController = navController
            )
        }

        composable(NavigationRoutes.AddTransaction::class.simpleName ?: "AddTransaction") {
            AddTransactionScreen(
                viewModel = addTransactionViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            "edit_transaction/{transactionId}",
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            EditTransactionScreen(
                viewModel = editTransactionViewModel,
                transactionId = transactionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavigationRoutes.Budget::class.simpleName ?: "Budget") {
            BudgetScreen(viewModel = budgetViewModel)
        }
    }
}
