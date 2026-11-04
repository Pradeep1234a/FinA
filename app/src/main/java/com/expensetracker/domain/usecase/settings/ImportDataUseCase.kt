package com.expensetracker.domain.usecase.settings

import com.expensetracker.domain.repository.SettingsRepository
import java.io.File
import kotlin.Result

class ImportDataUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(file: File): Result<Unit> {
        return settingsRepository.importData(file)
    }
}
