package com.expensetracker.domain.usecase.settings

import com.expensetracker.domain.model.ThemeMode
import com.expensetracker.domain.repository.SettingsRepository
import kotlin.Result

class UpdateThemeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(theme: ThemeMode): Result<Unit> {
        return try {
            settingsRepository.updateTheme(theme)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
