package com.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.financeapp.data.preferences.AppPreferences
import com.financeapp.ui.navigation.AppNavigation
import com.financeapp.ui.theme.FinanceAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isDarkMode by appPreferences.isDarkMode.collectAsState(initial = false)
            val scope = rememberCoroutineScope()

            FinanceAppTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Check onboarding status and navigate
                    LaunchedEffect(Unit) {
                        val isOnboardingCompleted = appPreferences.isOnboardingCompleted.first()
                        if (!isOnboardingCompleted) {
                            navController.navigate("Onboarding") {
                                popUpTo("Main") { inclusive = true }
                            }
                        }
                    }

                    AppNavigation(
                        navController = navController,
                        appPreferences = appPreferences
                    )
                }
            }
        }
    }
}
