package com.financeapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.financeapp.data.preferences.AppPreferences
import com.financeapp.ui.screens.AddTransactionScreen
import com.financeapp.ui.screens.BudgetScreen
import com.financeapp.ui.screens.EditTransactionScreen
import com.financeapp.ui.screens.MainScreen
import com.financeapp.ui.screens.OnboardingScreen
import com.financeapp.ui.screens.OnboardingViewModel
import com.financeapp.ui.viewmodel.AddTransactionViewModel
import com.financeapp.ui.viewmodel.BudgetViewModel
import com.financeapp.ui.viewmodel.EditTransactionViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    appPreferences: AppPreferences
) {
    val isOnboardingCompleted by appPreferences.isOnboardingCompleted.collectAsState(initial = false)

    NavHost(
        navController = navController,
        startDestination = if (isOnboardingCompleted) {
            NavigationRoutes.Main::class.simpleName ?: "Main"
        } else {
            "Onboarding"
        }
    ) {
        composable("Onboarding") {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                onFinish = {
                    navController.navigate(NavigationRoutes.Main::class.simpleName ?: "Main") {
                        popUpTo("Onboarding") { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

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
