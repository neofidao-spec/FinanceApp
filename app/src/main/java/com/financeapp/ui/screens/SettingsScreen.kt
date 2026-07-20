package com.financeapp.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.ui.viewmodel.SettingsViewModel
import com.financeapp.ui.theme.Spacing
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.res.stringResource
import com.financeapp.R

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (uiState.errorMessage != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    uiState.errorMessage ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(Spacing.md)
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
            }
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(modifier = Modifier.height(Spacing.lg))

        // Section 1: Tampilan
        Text(stringResource(R.string.settings_section_appearance), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(Spacing.sm))

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_dark_mode), fontWeight = FontWeight.Medium)
                        Text(stringResource(R.string.settings_dark_mode_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = uiState.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Section 2: Akun
        Text(stringResource(R.string.settings_section_account), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(Spacing.sm))

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_account_count), fontWeight = FontWeight.Medium)
                        Text(stringResource(R.string.settings_account_count_desc, uiState.accountCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(stringResource(R.string.settings_manage_accounts), fontWeight = FontWeight.Medium)
                Text(stringResource(R.string.settings_manage_accounts_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Section 3: Data
        Text(stringResource(R.string.settings_section_data), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(Spacing.sm))

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_total_transactions), fontWeight = FontWeight.Medium)
                        Text(stringResource(R.string.settings_total_transactions_desc, uiState.transactionCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Spacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_export_csv), fontWeight = FontWeight.Medium)
                        Text(stringResource(R.string.settings_export_csv_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    androidx.compose.material3.TextButton(
                        onClick = {
                            viewModel.exportTransactions { intent ->
                                if (intent != null) {
                                    context.startActivity(Intent.createChooser(intent, stringResource(R.string.settings_share_csv)))
                                }
                            }
                        }
                    ) {
                        Text(stringResource(R.string.settings_export_button))
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(stringResource(R.string.settings_database), fontWeight = FontWeight.Medium)
                Text(stringResource(R.string.settings_database_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(Spacing.sm))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(stringResource(R.string.settings_currency_format), fontWeight = FontWeight.Medium)
                Text(stringResource(R.string.settings_currency_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Section 4: Tentang
        Text(stringResource(R.string.settings_section_about), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(Spacing.sm))

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                Text(stringResource(R.string.settings_app_name), fontWeight = FontWeight.Medium)
                Text(stringResource(R.string.settings_version), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    stringResource(R.string.settings_app_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    "Kotlin + Jetpack Compose + Room",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                Text(stringResource(R.string.settings_tech_stack), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(Spacing.sm))
                TechItem("Kotlin", "1.9.25")
                TechItem("Jetpack Compose", "BOM 2024.06.00")
                TechItem("Material 3", "1.1.2")
                TechItem("Room Database", "2.6.1")
                TechItem("Navigation Compose", "2.7.5")
                TechItem("Lifecycle ViewModel", "2.6.2")
                TechItem("Hilt", "2.51.1")
                TechItem("DataStore", "1.0.0")
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))
    }
    }
}

@Composable
private fun TechItem(name: String, version: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(version, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
