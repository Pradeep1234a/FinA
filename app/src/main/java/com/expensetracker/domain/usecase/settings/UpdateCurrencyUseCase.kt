package com.expensetracker.domain.usecase.settings

import com.expensetracker.domain.repository.SettingsRepository
import kotlin.Result

class UpdateCurrencyUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(currency: String): Result<Unit> {
        return try {
            settingsRepository.updateCurrency(currency)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
