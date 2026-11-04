package com.expensetracker.domain.usecase.settings

import com.expensetracker.domain.repository.SettingsRepository
import java.io.File
import kotlin.Result

class RestoreUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(backupFile: File): Result<Unit> {
        return settingsRepository.restore(backupFile)
    }
}
