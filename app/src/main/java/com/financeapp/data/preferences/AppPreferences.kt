package com.financeapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        // CURRENCY — reserved for future use (currency selector)
        // TODO: Wire currency selector to SettingsScreen UI
        //  Currently accepts IDR/USD/SGD input but not connected to transactions
        private val CURRENCY = stringPreferencesKey("currency")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED] ?: false
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DARK_MODE] ?: false
    }

    val currency: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CURRENCY] ?: "IDR"
    }

    suspend fun setOnboardingCompleted() {
        try {
            context.dataStore.edit { prefs ->
                prefs[ONBOARDING_COMPLETED] = true
            }
        } catch (e: Exception) {
            Log.e("AppPrefs", "Failed to set onboarding completed", e)
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        try {
            context.dataStore.edit { prefs ->
                prefs[DARK_MODE] = enabled
            }
        } catch (e: Exception) {
            Log.e("AppPrefs", "Failed to set dark mode", e)
        }
    }

    suspend fun setCurrency(value: String) {
        try {
            context.dataStore.edit { prefs ->
                prefs[CURRENCY] = value
            }
        } catch (e: Exception) {
            Log.e("AppPrefs", "Failed to set currency", e)
        }
    }
}
