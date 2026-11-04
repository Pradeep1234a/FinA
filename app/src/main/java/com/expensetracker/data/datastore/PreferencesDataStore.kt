package com.expensetracker.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesKeys
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.expensetracker.domain.model.Settings
import com.expensetracker.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek
import kotlinx.coroutines.flow.map

class PreferencesDataStore @Inject constructor(
    private val context: Context,
    private val themeKey: Preferences.Key<String> = PreferencesKeys.stringKey("theme"),
    private val currencyKey: Preferences.Key<String> = PreferencesKeys.stringKey("currency"),
    private val languageKey: Preferences.Key<String> = PreferencesKeys.stringKey("language"),
    private val firstDayOfWeekKey: Preferences.Key<Int> = PreferencesKeys.intKey("firstDayOfWeek"),
    private val budgetAlertEnabledKey: Preferences.Key<Boolean> = PreferencesKeys.booleanKey("budgetAlertEnabled"),
    private val recurringEnabledKey: Preferences.Key<Boolean> = PreferencesKeys.booleanKey("recurringEnabled"),
    private val onboardingCompleteKey: Preferences.Key<Boolean> = PreferencesKeys.booleanKey("onboardingComplete")
) {

    private val dataStore = context.preferencesDataStore("settings")

    val settings: Flow<Settings> = dataStore.data
        .catch { exception ->
            emit(Preferences.EMPTY)
        }
        .map { preferences ->
            Settings(
                theme = ThemeMode.valueOf(preferences[themeKey] ?: ThemeMode.SYSTEM.name),
                currency = preferences[currencyKey] ?: "USD",
                language = preferences[languageKey] ?: "en",
                firstDayOfWeek = DayOfWeek.ordinal(preferences[firstDayOfWeekKey] ?: 1),
                budgetAlertEnabled = preferences[budgetAlertEnabledKey] ?: true,
                recurringTransactionEnabled = preferences[recurringEnabledKey] ?: true,
                onboardingComplete = preferences[onboardingCompleteKey] ?: false
            )
        }

    suspend fun updateTheme(theme: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[themeKey] = theme.name
        }
    }

    suspend fun updateCurrency(currency: String) {
        dataStore.edit { preferences ->
            preferences[currencyKey] = currency
        }
    }

    suspend fun updateLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[languageKey] = language
        }
    }

    suspend fun updateFirstDayOfWeek(dayOfWeek: Int) {
        dataStore.edit { preferences ->
            preferences[firstDayOfWeekKey] = dayOfWeek
        }
    }

    suspend fun updateBudgetAlerts(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[budgetAlertEnabledKey] = enabled
        }
    }

    suspend fun updateRecurringEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[recurringEnabledKey] = enabled
        }
    }

    suspend fun setOnboardingComplete() {
        dataStore.edit { preferences ->
            preferences[onboardingCompleteKey] = true
        }
    }
}