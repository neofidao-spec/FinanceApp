package com.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.financeapp.ui.navigation.AppNavigation
import com.financeapp.ui.theme.FinanceAppTheme
import com.financeapp.ui.viewmodel.AddTransactionViewModel
import com.financeapp.ui.viewmodel.DashboardViewModel
import com.financeapp.ui.viewmodel.EditTransactionViewModel
import com.financeapp.ui.viewmodel.TransactionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as FinanceApp
        
        val dashboardViewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(app.transactionRepository, app.categoryRepository) as T
            }
        }

        val transactionViewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return TransactionViewModel(app.transactionRepository, app.categoryRepository) as T
            }
        }

        val addTransactionViewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AddTransactionViewModel(app.transactionRepository, app.categoryRepository) as T
            }
        }

        val editTransactionViewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return EditTransactionViewModel(app.transactionRepository, app.categoryRepository) as T
            }
        }

        val dashboardViewModel = ViewModelProvider(this, dashboardViewModelFactory)[DashboardViewModel::class.java]
        val transactionViewModel = ViewModelProvider(this, transactionViewModelFactory)[TransactionViewModel::class.java]
        val addTransactionViewModel = ViewModelProvider(this, addTransactionViewModelFactory)[AddTransactionViewModel::class.java]
        val editTransactionViewModel = ViewModelProvider(this, editTransactionViewModelFactory)[EditTransactionViewModel::class.java]

        setContent {
            FinanceAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        dashboardViewModel = dashboardViewModel,
                        transactionViewModel = transactionViewModel,
                        addTransactionViewModel = addTransactionViewModel,
                        editTransactionViewModel = editTransactionViewModel
                    )
                }
            }
        }
    }
}
