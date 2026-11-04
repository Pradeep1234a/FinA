package com.expensetracker.domain.usecase.settings

import com.expensetracker.domain.repository.SettingsRepository
import kotlin.Result

class UpdateLanguageUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(language: String): Result<Unit> {
        return try {
            settingsRepository.updateLanguage(language)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
