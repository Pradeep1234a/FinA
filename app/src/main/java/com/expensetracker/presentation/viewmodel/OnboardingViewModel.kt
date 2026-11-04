package com.expensetracker.presentation.viewmodel

import androidx.hilt.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.domain.model.Category
import com.expensetracker.domain.model.CategoryType
import com.expensetracker.domain.model.Settings
import com.expensetracker.domain.repository.CategoryRepository
import com.expensetracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val categoryRepository: CategoryRepository
) : BaseViewModel() {

    private val _currentStep = MutableStateFlow<Int>(0)
    val currentStep: StateFlow<Int> = _currentStep

    private val _totalSteps = MutableStateFlow<Int>(4)
    val totalSteps: StateFlow<Int> = _totalSteps

    private val _selectedCurrency = MutableStateFlow<String>("USD")
    val selectedCurrency: StateFlow<String> = _selectedCurrency

    private val _selectedTheme = MutableStateFlow<com.expensetracker.domain.model.ThemeMode>(com.expensetracker.domain.model.ThemeMode.System)
    val selectedTheme: StateFlow<com.expensetracker.domain.model.ThemeMode> = _selectedTheme

    private val _notificationsEnabled = MutableStateFlow<Boolean>(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    private val _biometricEnabled = MutableStateFlow<Boolean>(false)
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled

    private val _defaultCategories = MutableStateFlow<List<Category>>(emptyList())
    val defaultCategories: StateFlow<List<Category>> = _defaultCategories

    private val _selectedDefaultCategories = MutableStateFlow<Set<Long>>(emptySet())
    val selectedDefaultCategories: StateFlow<Set<Long>> = _selectedDefaultCategories

    private val _saveState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val saveState: StateFlow<UiState<Unit>> = _saveState

    val steps = listOf("Welcome", "Preferences", "Categories", "Finish")

    init { loadDefaultCategories() }

    private fun loadDefaultCategories() {
        viewModelScope.launch {
            try {
                val categories = categoryRepository.getDefaultCategories()
                _defaultCategories.value = categories
                _selectedDefaultCategories.value = categories.map { it.id }.toSet()
            } catch (e: Exception) { }
        }
    }

    fun nextStep() { if (_currentStep.value < _totalSteps.value - 1) _currentStep.value++ }
    fun previousStep() { if (_currentStep.value > 0) _currentStep.value-- }
    fun setCurrency(currency: String) { _selectedCurrency.value = currency }
    fun setTheme(theme: com.expensetracker.domain.model.ThemeMode) { _selectedTheme.value = theme }
    fun setNotificationsEnabled(enabled: Boolean) { _notificationsEnabled.value = enabled }
    fun setBiometricEnabled(enabled: Boolean) { _biometricEnabled.value = enabled }
    fun toggleDefaultCategory(categoryId: Long) {
        val current = _selectedDefaultCategories.value.toMutableSet()
        if (current.contains(categoryId)) current.remove(categoryId) else current.add(categoryId)
        _selectedDefaultCategories.value = current
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _saveState.value = UiState.Loading()
            try {
                val settings = Settings(
                    themeMode = _selectedTheme.value, currency = _selectedCurrency.value, language = "en",
                    notificationsEnabled = _notificationsEnabled.value, biometricEnabled = _biometricEnabled.value,
                    autoBackupEnabled = false, backupFrequency = "Weekly", isSetupComplete = true
                )
                settingsRepository.saveSettings(settings)
                _selectedDefaultCategories.value.forEach { categoryId ->
                    val category = _defaultCategories.value.find { it.id == categoryId }
                    category?.let { if (!it.isDefault) categoryRepository.updateCategory(it.copy(isDefault = true)) }
                }
                _saveState.value = UiState.Success(Unit)
                onSuccess()
            } catch (e: Exception) { _saveState.value = UiState.Error(e.message ?: "Failed to complete setup") }
        }
    }

    fun skipOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _saveState.value = UiState.Loading()
            try {
                val settings = Settings(
                    themeMode = com.expensetracker.domain.model.ThemeMode.System, currency = "USD", language = "en",
                    notificationsEnabled = true, biometricEnabled = false, autoBackupEnabled = false,
                    backupFrequency = "Weekly", isSetupComplete = true
                )
                settingsRepository.saveSettings(settings)
                _saveState.value = UiState.Success(Unit)
                onSuccess()
            } catch (e: Exception) { _saveState.value = UiState.Error(e.message ?: "Failed to complete setup") }
        }
    }
}