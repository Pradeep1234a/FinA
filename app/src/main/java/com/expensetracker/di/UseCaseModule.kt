package com.expensetracker.di

import com.expensetracker.domain.usecase.budget.AddBudgetUseCase
import com.expensetracker.domain.usecase.budget.CheckBudgetAlertsUseCase
import com.expensetracker.domain.usecase.budget.DeleteBudgetUseCase
import com.expensetracker.domain.usecase.budget.GetBudgetUseCase
import com.expensetracker.domain.usecase.budget.GetBudgetsUseCase
import com.expensetracker.domain.usecase.budget.UpdateBudgetUseCase
import com.expensetracker.domain.usecase.category.AddCategoryUseCase
import com.expensetracker.domain.usecase.category.DeleteCategoryUseCase
import com.expensetracker.domain.usecase.category.GetCategoriesUseCase
import com.expensetracker.domain.usecase.category.GetCategoryUseCase
import com.expensetracker.domain.usecase.category.InitializeDefaultCategoriesUseCase
import com.expensetracker.domain.usecase.category.UpdateCategoryUseCase
import com.expensetracker.domain.usecase.recurring.AddRecurringTransactionUseCase
import com.expensetracker.domain.usecase.recurring.DeleteRecurringTransactionUseCase
import com.expensetracker.domain.usecase.recurring.GenerateRecurringTransactionUseCase
import com.expensetracker.domain.usecase.recurring.GetRecurringTransactionsUseCase
import com.expensetracker.domain.usecase.recurring.ProcessDueRecurringTransactionsUseCase
import com.expensetracker.domain.usecase.recurring.UpdateRecurringTransactionUseCase
import com.expensetracker.domain.usecase.settings.BackupUseCase
import com.expensetracker.domain.usecase.settings.ExportDataUseCase
import com.expensetracker.domain.usecase.settings.GetSettingsUseCase
import com.expensetracker.domain.usecase.settings.ImportDataUseCase
import com.expensetracker.domain.usecase.settings.RestoreUseCase
import com.expensetracker.domain.usecase.settings.UpdateCurrencyUseCase
import com.expensetracker.domain.usecase.settings.UpdateLanguageUseCase
import com.expensetracker.domain.usecase.settings.UpdateThemeUseCase
import com.expensetracker.domain.usecase.transaction.AddTransactionUseCase
import com.expensetracker.domain.usecase.transaction.DeleteTransactionUseCase
import com.expensetracker.domain.usecase.transaction.GetRecentTransactionsUseCase
import com.expensetracker.domain.usecase.transaction.GetTotalExpenseUseCase
import com.expensetracker.domain.usecase.transaction.GetTotalIncomeUseCase
import com.expensetracker.domain.usecase.transaction.GetTransactionUseCase
import com.expensetracker.domain.usecase.transaction.GetTransactionsByCategoryUseCase
import com.expensetracker.domain.usecase.transaction.GetTransactionsByDateRangeUseCase
import com.expensetracker.domain.usecase.transaction.GetTransactionsUseCase
import com.expensetracker.domain.usecase.transaction.SearchTransactionsUseCase
import com.expensetracker.domain.usecase.transaction.UpdateTransactionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetTransactionsUseCase(): GetTransactionsUseCase = GetTransactionsUseCase()

    @Provides
    @Singleton
    fun provideGetTransactionUseCase(): GetTransactionUseCase = GetTransactionUseCase()

    @Provides
    @Singleton
    fun provideAddTransactionUseCase(): AddTransactionUseCase = AddTransactionUseCase()

    @Provides
    @Singleton
    fun provideUpdateTransactionUseCase(): UpdateTransactionUseCase = UpdateTransactionUseCase()

    @Provides
    @Singleton
    fun provideDeleteTransactionUseCase(): DeleteTransactionUseCase = DeleteTransactionUseCase()

    @Provides
    @Singleton
    fun provideGetTransactionsByDateRangeUseCase(): GetTransactionsByDateRangeUseCase = GetTransactionsByDateRangeUseCase()

    @Provides
    @Singleton
    fun provideGetTransactionsByCategoryUseCase(): GetTransactionsByCategoryUseCase = GetTransactionsByCategoryUseCase()

    @Provides
    @Singleton
    fun provideGetTotalExpenseUseCase(): GetTotalExpenseUseCase = GetTotalExpenseUseCase()

    @Provides
    @Singleton
    fun provideGetTotalIncomeUseCase(): GetTotalIncomeUseCase = GetTotalIncomeUseCase()

    @Provides
    @Singleton
    fun provideSearchTransactionsUseCase(): SearchTransactionsUseCase = SearchTransactionsUseCase()

    @Provides
    @Singleton
    fun provideGetRecentTransactionsUseCase(): GetRecentTransactionsUseCase = GetRecentTransactionsUseCase()

    @Provides
    @Singleton
    fun provideGetCategoriesUseCase(): GetCategoriesUseCase = GetCategoriesUseCase()

    @Provides
    @Singleton
    fun provideGetCategoryUseCase(): GetCategoryUseCase = GetCategoryUseCase()

    @Provides
    @Singleton
    fun provideAddCategoryUseCase(): AddCategoryUseCase = AddCategoryUseCase()

    @Provides
    @Singleton
    fun provideUpdateCategoryUseCase(): UpdateCategoryUseCase = UpdateCategoryUseCase()

    @Provides
    @Singleton
    fun provideDeleteCategoryUseCase(): DeleteCategoryUseCase = DeleteCategoryUseCase()

    @Provides
    @Singleton
    fun provideInitializeDefaultCategoriesUseCase(): InitializeDefaultCategoriesUseCase = InitializeDefaultCategoriesUseCase()

    @Provides
    @Singleton
    fun provideGetBudgetsUseCase(): GetBudgetsUseCase = GetBudgetsUseCase()

    @Provides
    @Singleton
    fun provideGetBudgetUseCase(): GetBudgetUseCase = GetBudgetUseCase()

    @Provides
    @Singleton
    fun provideAddBudgetUseCase(): AddBudgetUseCase = AddBudgetUseCase()

    @Provides
    @Singleton
    fun provideUpdateBudgetUseCase(): UpdateBudgetUseCase = UpdateBudgetUseCase()

    @Provides
    @Singleton
    fun provideDeleteBudgetUseCase(): DeleteBudgetUseCase = DeleteBudgetUseCase()

    @Provides
    @Singleton
    fun provideCheckBudgetAlertsUseCase(): CheckBudgetAlertsUseCase = CheckBudgetAlertsUseCase()

    @Provides
    @Singleton
    fun provideGetRecurringTransactionsUseCase(): GetRecurringTransactionsUseCase = GetRecurringTransactionsUseCase()

    @Provides
    @Singleton
    fun provideAddRecurringTransactionUseCase(): AddRecurringTransactionUseCase = AddRecurringTransactionUseCase()

    @Provides
    @Singleton
    fun provideUpdateRecurringTransactionUseCase(): UpdateRecurringTransactionUseCase = UpdateRecurringTransactionUseCase()

    @Provides
    @Singleton
    fun provideDeleteRecurringTransactionUseCase(): DeleteRecurringTransactionUseCase = DeleteRecurringTransactionUseCase()

    @Provides
    @Singleton
    fun provideProcessDueRecurringTransactionsUseCase(): ProcessDueRecurringTransactionsUseCase = ProcessDueRecurringTransactionsUseCase()

    @Provides
    @Singleton
    fun provideGenerateRecurringTransactionUseCase(): GenerateRecurringTransactionUseCase = GenerateRecurringTransactionUseCase()

    @Provides
    @Singleton
    fun provideGetSettingsUseCase(): GetSettingsUseCase = GetSettingsUseCase()

    @Provides
    @Singleton
    fun provideUpdateThemeUseCase(): UpdateThemeUseCase = UpdateThemeUseCase()

    @Provides
    @Singleton
    fun provideUpdateCurrencyUseCase(): UpdateCurrencyUseCase = UpdateCurrencyUseCase()

    @Provides
    @Singleton
    fun provideUpdateLanguageUseCase(): UpdateLanguageUseCase = UpdateLanguageUseCase()

    @Provides
    @Singleton
    fun provideExportDataUseCase(): ExportDataUseCase = ExportDataUseCase()

    @Provides
    @Singleton
    fun provideImportDataUseCase(): ImportDataUseCase = ImportDataUseCase()

    @Provides
    @Singleton
    fun provideBackupUseCase(): BackupUseCase = BackupUseCase()

    @Provides
    @Singleton
    fun provideRestoreUseCase(): RestoreUseCase = RestoreUseCase()
}