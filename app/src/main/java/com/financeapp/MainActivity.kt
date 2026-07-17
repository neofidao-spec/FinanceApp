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
import com.financeapp.ui.viewmodel.BudgetViewModel
import com.financeapp.ui.viewmodel.DashboardViewModel
import com.financeapp.ui.viewmodel.EditTransactionViewModel
import com.financeapp.ui.viewmodel.ReportViewModel
import com.financeapp.ui.viewmodel.TransactionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as FinanceApp

        val dashboardViewModelFactory = createFactory {
            DashboardViewModel(app.transactionRepository, app.categoryRepository)
        }
        val transactionViewModelFactory = createFactory {
            TransactionViewModel(app.transactionRepository, app.categoryRepository)
        }
        val addTransactionViewModelFactory = createFactory {
            AddTransactionViewModel(app.transactionRepository, app.categoryRepository)
        }
        val editTransactionViewModelFactory = createFactory {
            EditTransactionViewModel(app.transactionRepository, app.categoryRepository)
        }
        val reportViewModelFactory = createFactory {
            ReportViewModel(app.transactionRepository, app.categoryRepository)
        }
        val budgetViewModelFactory = createFactory {
            BudgetViewModel(app.budgetRepository, app.categoryRepository)
        }

        val dashboardViewModel = ViewModelProvider(this, dashboardViewModelFactory)[DashboardViewModel::class.java]
        val transactionViewModel = ViewModelProvider(this, transactionViewModelFactory)[TransactionViewModel::class.java]
        val addTransactionViewModel = ViewModelProvider(this, addTransactionViewModelFactory)[AddTransactionViewModel::class.java]
        val editTransactionViewModel = ViewModelProvider(this, editTransactionViewModelFactory)[EditTransactionViewModel::class.java]
        val reportViewModel = ViewModelProvider(this, reportViewModelFactory)[ReportViewModel::class.java]
        val budgetViewModel = ViewModelProvider(this, budgetViewModelFactory)[BudgetViewModel::class.java]

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
                        editTransactionViewModel = editTransactionViewModel,
                        reportViewModel = reportViewModel,
                        budgetViewModel = budgetViewModel
                    )
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : androidx.lifecycle.ViewModel> createFactory(creator: () -> T): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <VM : androidx.lifecycle.ViewModel> create(modelClass: Class<VM>): VM {
                return creator() as VM
            }
        }
    }
}
