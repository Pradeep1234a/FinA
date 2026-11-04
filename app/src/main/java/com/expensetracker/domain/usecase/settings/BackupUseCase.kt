package com.expensetracker.domain.usecase.settings

import com.expensetracker.domain.repository.SettingsRepository
import java.io.File
import kotlin.Result

class BackupUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Result<File> {
        return settingsRepository.backup()
    }
}
