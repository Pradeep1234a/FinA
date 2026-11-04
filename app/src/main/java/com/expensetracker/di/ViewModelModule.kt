package com.expensetracker.di

import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.expensetracker.presentation.viewmodel.AddEditTransactionViewModel
import com.expensetracker.presentation.viewmodel.BudgetManagementViewModel
import com.expensetracker.presentation.viewmodel.CategoryManagementViewModel
import com.expensetracker.presentation.viewmodel.HomeViewModel
import com.expensetracker.presentation.viewmodel.RecurringTransactionsViewModel
import com.expensetracker.presentation.viewmodel.ReportsViewModel
import com.expensetracker.presentation.viewmodel.SettingsViewModel
import com.expensetracker.presentation.viewmodel.TransactionListViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(ActivityComponent::class)
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(viewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TransactionListViewModel::class)
    abstract fun bindTransactionListViewModel(viewModel: TransactionListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddEditTransactionViewModel::class)
    abstract fun bindAddEditTransactionViewModel(viewModel: AddEditTransactionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CategoryManagementViewModel::class)
    abstract fun bindCategoryManagementViewModel(viewModel: CategoryManagementViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BudgetManagementViewModel::class)
    abstract fun bindBudgetManagementViewModel(viewModel: BudgetManagementViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReportsViewModel::class)
    abstract fun bindReportsViewModel(viewModel: ReportsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RecurringTransactionsViewModel::class)
    abstract fun bindRecurringTransactionsViewModel(viewModel: RecurringTransactionsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel
}