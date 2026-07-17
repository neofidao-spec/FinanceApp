package com.financeapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.financeapp.ui.viewmodel.EditTransactionViewModel

@Composable
fun AppNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.Main::class.simpleName ?: "Main"
    ) {
        composable(NavigationRoutes.Main::class.simpleName ?: "Main") {
            MainScreen(navController = navController)
        }

        composable(NavigationRoutes.AddTransaction::class.simpleName ?: "AddTransaction") {
            val viewModel: AddTransactionViewModel = hiltViewModel()
            AddTransactionScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            "edit_transaction/{transactionId}",
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            val viewModel: EditTransactionViewModel = hiltViewModel()
            EditTransactionScreen(
                viewModel = viewModel,
                transactionId = transactionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavigationRoutes.Budget::class.simpleName ?: "Budget") {
            val viewModel: BudgetViewModel = hiltViewModel()
            BudgetScreen(viewModel = viewModel)
        }
    }
}
