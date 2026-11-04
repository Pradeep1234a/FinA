package com.expensetracker.di

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesKeys
import androidx.datastore.preferences.preferencesDataStore
import com.expensetracker.data.datastore.PreferencesDataStore
import com.expensetracker.domain.model.ThemeMode
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    // DataStore keys
    private val THEME_KEY = PreferencesKeys.stringKey("theme")
    private val CURRENCY_KEY = PreferencesKeys.stringKey("currency")
    private val LANGUAGE_KEY = PreferencesKeys.stringKey("language")
    private val FIRST_DAY_OF_WEEK_KEY = PreferencesKeys.intKey("firstDayOfWeek")
    private val BUDGET_ALERT_ENABLED_KEY = PreferencesKeys.booleanKey("budgetAlertEnabled")
    private val RECURRING_ENABLED_KEY = PreferencesKeys.booleanKey("recurringEnabled")
    private val ONBOARDING_COMPLETE_KEY = PreferencesKeys.booleanKey("onboardingComplete")

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context) = context.preferencesDataStore("settings")

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        dataStore: androidx.datastore.preferences.PreferencesDataStore<Preferences>
    ): PreferencesDataStore {
        return PreferencesDataStore(
            dataStore = dataStore,
            themeKey = THEME_KEY,
            currencyKey = CURRENCY_KEY,
            languageKey = LANGUAGE_KEY,
            firstDayOfWeekKey = FIRST_DAY_OF_WEEK_KEY,
            budgetAlertEnabledKey = BUDGET_ALERT_ENABLED_KEY,
            recurringEnabledKey = RECURRING_ENABLED_KEY,
            onboardingCompleteKey = ONBOARDING_COMPLETE_KEY
        )
    }
}
