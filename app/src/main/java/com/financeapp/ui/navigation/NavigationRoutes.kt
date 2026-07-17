package com.financeapp.ui.navigation

sealed class NavigationRoutes {
    data object Main : NavigationRoutes()
    data object Dashboard : NavigationRoutes()
    data object Transactions : NavigationRoutes()
    data object AddTransaction : NavigationRoutes()
    data object EditTransaction : NavigationRoutes()
    data object Reports : NavigationRoutes()
    data object Budget : NavigationRoutes()
    data object Settings : NavigationRoutes()
    data object Gamification : NavigationRoutes()
}
