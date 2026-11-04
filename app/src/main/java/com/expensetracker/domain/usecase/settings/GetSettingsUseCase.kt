package com.expensetracker.domain.usecase.settings

import com.expensetracker.domain.model.Settings
import com.expensetracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlin.Result

class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): Result<Settings> {
        return try {
            val settings = settingsRepository.getSettings().first()
            Result.success(settings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
