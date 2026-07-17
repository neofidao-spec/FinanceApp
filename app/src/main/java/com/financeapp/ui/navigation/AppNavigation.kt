package com.financeapp.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.financeapp.ui.viewmodel.AddTransactionViewModel
import com.financeapp.ui.viewmodel.BudgetViewModel
import com.financeapp.ui.viewmodel.EditTransactionViewModel

private const val TRANSITION_DURATION = 300

@Composable
fun AppNavigation(
    navController: NavHostController,
    appPreferences: AppPreferences
) {
    NavHost(
        navController = navController,
        startDestination = "Main"
    ) {
        composable(
            route = "Onboarding",
            enterTransition = {
                fadeIn(animationSpec = tween(TRANSITION_DURATION)) +
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Up,
                            animationSpec = tween(TRANSITION_DURATION)
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(TRANSITION_DURATION))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(TRANSITION_DURATION))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(TRANSITION_DURATION)) +
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(TRANSITION_DURATION)
                        )
            }
        ) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate("Main") {
                        popUpTo("Onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "Main",
            enterTransition = {
                fadeIn(animationSpec = tween(TRANSITION_DURATION))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(TRANSITION_DURATION))
            }
        ) {
            MainScreen(navController = navController)
        }

        composable(
            route = "AddTransaction",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(TRANSITION_DURATION)
                ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(TRANSITION_DURATION)
                ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
            }
        ) {
            val viewModel: AddTransactionViewModel = hiltViewModel()
            AddTransactionScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit_transaction/{transactionId}",
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType }),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(TRANSITION_DURATION)
                ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(TRANSITION_DURATION)
                ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
            }
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            val viewModel: EditTransactionViewModel = hiltViewModel()
            EditTransactionScreen(
                viewModel = viewModel,
                transactionId = transactionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "Budget",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(TRANSITION_DURATION)
                ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(TRANSITION_DURATION)
                ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
            }
        ) {
            val viewModel: BudgetViewModel = hiltViewModel()
            BudgetScreen(viewModel = viewModel)
        }
    }
}
