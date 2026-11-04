package com.expensetracker.presentation.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Provider

class ViewModelFactory @Inject constructor(
    private val providers: Map<Class<out ViewModel>, Provider<ViewModel>>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val provider = providers[modelClass]
            ?: throw IllegalArgumentException("ViewModel $modelClass not found in Hilt map")
        return provider.get() as T
    }
}

@HiltViewModel
class HomeViewModel @ViewModelInject constructor() : BaseViewModel() {
    // Implementation will be in separate file
}

@HiltViewModel
class TransactionListViewModel @ViewModelInject constructor() : BaseViewModel() {
    // Implementation will be in separate file
}

@HiltViewModel
class AddEditTransactionViewModel @ViewModelInject constructor() : BaseViewModel() {
    // Implementation will be in separate file
}

@HiltViewModel
class CategoryManagementViewModel @ViewModelInject constructor() : BaseViewModel() {
    // Implementation will be in separate file
}

@HiltViewModel
class BudgetManagementViewModel @ViewModelInject constructor() : BaseViewModel() {
    // Implementation will be in separate file
}

@HiltViewModel
class ReportsViewModel @ViewModelInject constructor() : BaseViewModel() {
    // Implementation will be in separate file
}

@HiltViewModel
class RecurringTransactionsViewModel @ViewModelInject constructor() : BaseViewModel() {
    // Implementation will be in separate file
}

@HiltViewModel
class SettingsViewModel @ViewModelInject constructor() : BaseViewModel() {
    // Implementation will be in separate file
}

@HiltViewModel
class SplashViewModel @ViewModelInject constructor() : BaseViewModel() {
    // Implementation will be in separate file
}

@HiltViewModel
class OnboardingViewModel @ViewModelInject constructor() : BaseViewModel() {
    // Implementation will be in separate file
}