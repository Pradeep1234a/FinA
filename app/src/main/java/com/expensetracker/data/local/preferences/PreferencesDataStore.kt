package com.expensetracker.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesKeys
import androidx.datastore.preferences.core.PreferencesKt
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.getLong
import androidx.datastore.preferences.core.getString
import androidx.datastore.preferences.core.mutablePreferences
import androidx.datastore.preferences.rxjava3.RxDataStoreBuilder
import com.expensetracker.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.kotlin.toFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesDataStore @Inject constructor(
    private val context: Context
) {

    companion object {
        private const val DATA_STORE_NAME = "settings.preferences"
    }

    private val dataStore = RxDataStoreBuilder(context, DATA_STORE_NAME).build()

    private val themeKey = PreferencesKeys.stringKey("theme")
    private val currencyKey = PreferencesKeys.stringKey("currency")
    private val languageKey = PreferencesKeys.stringKey("language")
    private val firstDayOfWeekKey = PreferencesKeys.intKey("first_day_of_week")
    private val budgetAlertsKey = PreferencesKeys.booleanKey("budget_alerts")
    private val recurringEnabledKey = PreferencesKeys.booleanKey("recurring_enabled")
    private val onboardingCompleteKey = PreferencesKeys.booleanKey("onboarding_complete")

    val themeFlow: Flow<ThemeMode> = dataStore.data
        .map { prefs ->
            val themeString = prefs[themeKey] ?: "SYSTEM"
            ThemeMode.valueOf(themeString)
        }
        .toFlow()

    val currencyFlow: Flow<String> = dataStore.data
        .map { prefs -> prefs[currencyKey] ?: "USD" }
        .toFlow()

    val languageFlow: Flow<String> = dataStore.data
        .map { prefs -> prefs[languageKey] ?: "en" }
        .toFlow()

    val firstDayOfWeekFlow: Flow<Int> = dataStore.data
        .map { prefs -> prefs[firstDayOfWeekKey] ?: 1 }
        .toFlow()

    val budgetAlertsFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[budgetAlertsKey] ?: true }
        .toFlow()

    val recurringEnabledFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[recurringEnabledKey] ?: true }
        .toFlow()

    val onboardingCompleteFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[onboardingCompleteKey] ?: false }
        .toFlow()

    suspend fun setTheme(theme: ThemeMode) {
        dataStore.updateDataAsync { prefs ->
            prefs.edit {
                this[themeKey] = theme.name
            }
        }.await()
    }

    suspend fun setCurrency(currency: String) {
        dataStore.updateDataAsync { prefs ->
            prefs.edit {
                this[currencyKey] = currency
            }
        }.await()
    }

    suspend fun setLanguage(language: String) {
        dataStore.updateDataAsync { prefs ->
            prefs.edit {
                this[languageKey] = language
            }
        }.await()
    }

    suspend fun setFirstDayOfWeek(dayOfWeek: Int) {
        dataStore.updateDataAsync { prefs ->
            prefs.edit {
                this[firstDayOfWeekKey] = dayOfWeek
            }
        }.await()
    }

    suspend fun setBudgetAlerts(enabled: Boolean) {
        dataStore.updateDataAsync { prefs ->
            prefs.edit {
                this[budgetAlertsKey] = enabled
            }
        }.await()
    }

    suspend fun setRecurringEnabled(enabled: Boolean) {
        dataStore.updateDataAsync { prefs ->
            prefs.edit {
                this[recurringEnabledKey] = enabled
            }
        }.await()
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.updateDataAsync { prefs ->
            prefs.edit {
                this[onboardingCompleteKey] = complete
            }
        }.await()
    }
}