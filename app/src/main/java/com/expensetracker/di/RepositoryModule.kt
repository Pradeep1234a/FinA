package com.expensetracker.di

import com.expensetracker.data.repository.BudgetRepositoryImpl
import com.expensetracker.data.repository.CategoryRepositoryImpl
import com.expensetracker.data.repository.RecurringTransactionRepositoryImpl
import com.expensetracker.data.repository.SettingsRepositoryImpl
import com.expensetracker.data.repository.TransactionRepositoryImpl
import com.expensetracker.domain.repository.BudgetRepository
import com.expensetracker.domain.repository.CategoryRepository
import com.expensetracker.domain.repository.RecurringTransactionRepository
import com.expensetracker.domain.repository.SettingsRepository
import com.expensetracker.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

@Module
@InstallIn(ApplicationComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    abstract fun bindRecurringTransactionRepository(impl: RecurringTransactionRepositoryImpl): RecurringTransactionRepository

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}