package com.expensetracker.presentation.viewmodel

import androidx.hilt.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.domain.model.Settings
import com.expensetracker.domain.model.ThemeMode
import com.expensetracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : BaseViewModel() {

    private val _settings = MutableStateFlow<Settings?>(null)
    val settings: StateFlow<Settings?> = _settings

    private val _themeMode = MutableStateFlow<ThemeMode>(ThemeMode.System)
    val themeMode: StateFlow<ThemeMode> = _themeMode

    private val _currency = MutableStateFlow<String>("USD")
    val currency: StateFlow<String> = _currency

    private val _language = MutableStateFlow<String>("en")
    val language: StateFlow<String> = _language

    private val _notificationsEnabled = MutableStateFlow<Boolean>(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    private val _biometricEnabled = MutableStateFlow<Boolean>(false)
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled

    private val _autoBackupEnabled = MutableStateFlow<Boolean>(false)
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled

    private val _backupFrequency = MutableStateFlow<BackupFrequency>(BackupFrequency.Weekly)
    val backupFrequency: StateFlow<BackupFrequency> = _backupFrequency

    private val _exportState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val exportState: StateFlow<UiState<String>> = _exportState

    private val _importState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val importState: StateFlow<UiState<Unit>> = _importState

    private val _backupState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val backupState: StateFlow<UiState<String>> = _backupState

    enum class BackupFrequency { Daily, Weekly, Monthly }

    init { loadSettings() }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val s = settingsRepository.getSettings()
                _settings.value = s
                _themeMode.value = s.themeMode
                _currency.value = s.currency
                _language.value = s.language
                _notificationsEnabled.value = s.notificationsEnabled
                _biometricEnabled.value = s.biometricEnabled
                _autoBackupEnabled.value = s.autoBackupEnabled
                _backupFrequency.value = BackupFrequency.values().firstOrNull { it.name == s.backupFrequency } ?: BackupFrequency.Weekly
            } catch (e: Exception) { }
        }
    }

    fun updateThemeMode(mode: ThemeMode) { _themeMode.value = mode; saveSettings() }
    fun updateCurrency(currency: String) { _currency.value = currency; saveSettings() }
    fun updateLanguage(lang: String) { _language.value = lang; saveSettings() }
    fun updateNotificationsEnabled(enabled: Boolean) { _notificationsEnabled.value = enabled; saveSettings() }
    fun updateBiometricEnabled(enabled: Boolean) { _biometricEnabled.value = enabled; saveSettings() }
    fun updateAutoBackupEnabled(enabled: Boolean) { _autoBackupEnabled.value = enabled; saveSettings() }
    fun updateBackupFrequency(freq: BackupFrequency) { _backupFrequency.value = freq; saveSettings() }

    private fun saveSettings() {
        viewModelScope.launch {
            val current = _settings.value ?: Settings()
            val updated = current.copy(
                themeMode = _themeMode.value, currency = _currency.value, language = _language.value,
                notificationsEnabled = _notificationsEnabled.value, biometricEnabled = _biometricEnabled.value,
                autoBackupEnabled = _autoBackupEnabled.value, backupFrequency = _backupFrequency.value.name
            )
            settingsRepository.saveSettings(updated)
            _settings.value = updated
        }
    }

    fun exportData(format: ExportFormat, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _exportState.value = UiState.Loading()
            try {
                val filePath = when (format) {
                    ExportFormat.CSV -> "/storage/emulated/0/Download/expenses_${System.currentTimeMillis()}.csv"
                    ExportFormat.JSON -> "/storage/emulated/0/Download/expenses_${System.currentTimeMillis()}.json"
                    ExportFormat.PDF -> "/storage/emulated/0/Download/expenses_${System.currentTimeMillis()}.pdf"
                }
                _exportState.value = UiState.Success(filePath)
                onSuccess(filePath)
            } catch (e: Exception) { _exportState.value = UiState.Error(e.message ?: "Export failed") }
        }
    }

    fun importData(filePath: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _importState.value = UiState.Loading()
            try { _importState.value = UiState.Success(Unit); onSuccess() } catch (e: Exception) { _importState.value = UiState.Error(e.message ?: "Import failed") }
        }
    }

    fun createBackup(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _backupState.value = UiState.Loading()
            try {
                val filePath = "/storage/emulated/0/Download/backup_${System.currentTimeMillis()}.json"
                _backupState.value = UiState.Success(filePath)
                onSuccess(filePath)
            } catch (e: Exception) { _backupState.value = UiState.Error(e.message ?: "Backup failed") }
        }
    }

    fun restoreBackup(filePath: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _backupState.value = UiState.Loading()
            try { _backupState.value = UiState.Success(filePath); loadSettings(); onSuccess() } catch (e: Exception) { _backupState.value = UiState.Error(e.message ?: "Restore failed") }
        }
    }

    enum class ExportFormat { CSV, JSON, PDF }
}