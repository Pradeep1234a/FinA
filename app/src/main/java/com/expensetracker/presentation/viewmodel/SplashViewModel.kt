package com.expensetracker.presentation.viewmodel

import androidx.hilt.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : BaseViewModel() {

    private val _isFirstLaunch = MutableStateFlow<Boolean>(false)
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch

    private val _shouldNavigate = MutableStateFlow<Boolean>(false)
    val shouldNavigate: StateFlow<Boolean> = _shouldNavigate

    init {
        checkFirstLaunch()
    }

    private fun checkFirstLaunch() {
        viewModelScope.launch {
            delay(1500) // Show splash for 1.5 seconds
            try {
                val settings = settingsRepository.getSettings()
                _isFirstLaunch.value = !settings.isSetupComplete
                _shouldNavigate.value = true
            } catch (e: Exception) {
                _isFirstLaunch.value = true
                _shouldNavigate.value = true
            }
        }
    }

    fun onAnimationEnd() {
        _shouldNavigate.value = true
    }
}