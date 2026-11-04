package com.expensetracker.domain.repository

import com.expensetracker.domain.model.Settings
import com.expensetracker.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import java.io.File

interface SettingsRepository {
    fun getSettings(): Flow<Settings>

    suspend fun updateTheme(theme: ThemeMode)

    suspend fun updateCurrency(currency: String)

    suspend fun updateLanguage(language: String)

    suspend fun updateFirstDayOfWeek(dayOfWeek: Int)

    suspend fun updateBudgetAlerts(enabled: Boolean)

    suspend fun updateRecurringEnabled(enabled: Boolean)

    suspend fun setOnboardingComplete()

    suspend fun exportData(): Result<File>

    suspend fun importData(file: File): Result<Unit>

    suspend fun backup(): Result<File>

    suspend fun restore(backupFile: File): Result<Unit>
}