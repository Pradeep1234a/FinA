package com.expensetracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    sealed interface UiState<out T> {
        data class Loading : UiState<Nothing>
        data class Success<out T>(val data: T) : UiState<T>
        data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>
        data class Empty(val message: String) : UiState<Nothing>
    }

    protected fun <T> MutableStateFlow<UiState<T>>.postSuccess(data: T) {
        value = UiState.Success(data)
    }

    protected fun <T> MutableStateFlow<UiState<T>>.postError(message: String, throwable: Throwable? = null) {
        value = UiState.Error(message, throwable)
    }

    protected fun <T> MutableStateFlow<UiState<T>>.postLoading() {
        value = UiState.Loading
    }

    protected fun <T> MutableStateFlow<UiState<T>>.postEmpty(message: String) {
        value = UiState.Empty(message)
    }

    protected fun <T> launch(coroutine: suspend () -> T) {
        viewModelScope.launch {
            coroutine()
        }
    }
}

class ViewModelFactory @Inject constructor(
    private val creators: Map<Class<out ViewModel>, @Suppress("UNUSED_PARAMETER") ViewModelProvider.Factory>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator = creators[modelClass] ?: creators.entries.firstOrNull {
            modelClass.isAssignableFrom(it.key)
        }?.value ?: throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        return try {
            creator.create(modelClass)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to create ViewModel: $modelClass", e)
        }
    }
}