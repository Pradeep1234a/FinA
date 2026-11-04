package com.expensetracker.data.repository

import android.content.Context
import com.expensetracker.data.local.dao.BudgetDao
import com.expensetracker.data.local.dao.CategoryDao
import com.expensetracker.data.local.dao.RecurringTransactionDao
import com.expensetracker.data.local.dao.SettingsDao
import com.expensetracker.data.local.dao.TransactionDao
import com.expensetracker.data.local.preferences.PreferencesDataStore
import com.expensetracker.data.mapper.BudgetMapper
import com.expensetracker.data.mapper.CategoryMapper
import com.expensetracker.data.mapper.RecurringTransactionMapper
import com.expensetracker.data.mapper.TransactionMapper
import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.model.Category
import com.expensetracker.domain.model.CategoryType
import com.expensetracker.domain.model.RecurringTransaction
import com.expensetracker.domain.model.Settings
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.domain.repository.SettingsRepository
import com.expensetracker.util.BackupUtils
import com.expensetracker.util.ExportUtils
import com.expensetracker.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val context: Context,
    private val settingsDao: SettingsDao,
    private val preferencesDataStore: PreferencesDataStore,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val recurringTransactionDao: RecurringTransactionDao
) : SettingsRepository {

    override fun getSettings(): Flow<Settings> {
        return combine(
            preferencesDataStore.themeFlow,
            preferencesDataStore.currencyFlow,
            preferencesDataStore.languageFlow,
            preferencesDataStore.firstDayOfWeekFlow,
            preferencesDataStore.budgetAlertsFlow,
            preferencesDataStore.recurringEnabledFlow,
            preferencesDataStore.onboardingCompleteFlow
        ) { theme, currency, language, firstDayOfWeek, budgetAlerts, recurringEnabled, onboardingComplete ->
            Settings(
                theme = theme,
                currency = currency,
                language = language,
                firstDayOfWeek = DayOfWeek.Companion.values()[firstDayOfWeek - 1],
                budgetAlertEnabled = budgetAlerts,
                recurringTransactionEnabled = recurringEnabled,
                onboardingComplete = onboardingComplete
            )
        }
    }

    override suspend fun updateTheme(theme: com.expensetracker.domain.model.ThemeMode) {
        preferencesDataStore.setTheme(theme)
    }

    override suspend fun updateCurrency(currency: String) {
        preferencesDataStore.setCurrency(currency)
    }

    override suspend fun updateLanguage(language: String) {
        preferencesDataStore.setLanguage(language)
    }

    override suspend fun updateFirstDayOfWeek(dayOfWeek: Int) {
        preferencesDataStore.setFirstDayOfWeek(dayOfWeek)
    }

    override suspend fun updateBudgetAlerts(enabled: Boolean) {
        preferencesDataStore.setBudgetAlerts(enabled)
    }

    override suspend fun updateRecurringEnabled(enabled: Boolean) {
        preferencesDataStore.setRecurringEnabled(enabled)
    }

    override suspend fun setOnboardingComplete() {
        preferencesDataStore.setOnboardingComplete(true)
    }

    override suspend fun exportData(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val transactions = transactionDao.getAllByDateRange(0, Long.MAX_VALUE).await()
            val categories = categoryDao.getAll().first()
            val recurringTransactions = recurringTransactionDao.getAll().first()

            val domainTransactions = transactions.mapNotNull { entity ->
                categories.find { it.id == entity.categoryId }?.let { category ->
                    TransactionMapper.toDomain(entity, CategoryMapper.toDomain(category))
                }
            }

            val domainCategories = categories.map { CategoryMapper.toDomain(it) }

            val domainBudgets = mutableListOf<Budget>()
            val allBudgetEntities = budgetDao.getAll().await()
            for (budgetEntity in allBudgetEntities) {
                val category = categories.find { it.id == budgetEntity.categoryId }?.let { CategoryMapper.toDomain(it) }
                if (category != null) {
                    val spent = calculateSpentForBudget(budgetEntity.categoryId, budgetEntity.monthYear).await()
                    domainBudgets.add(BudgetMapper.toDomain(budgetEntity, category, spent))
                }
            }

            val domainRecurringTransactions = recurringTransactions.mapNotNull { entity ->
                categories.find { it.id == entity.categoryId }?.let { category ->
                    RecurringTransactionMapper.toDomain(entity, CategoryMapper.toDomain(category))
                }
            }

            val settings = getSettings().first()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
            val exportDir = File(context.cacheDir, "export_$timestamp")
            exportDir.mkdirs()

            val transactionsFile = File(exportDir, "transactions.csv")
            ExportUtils.exportTransactionsToCSV(domainTransactions, transactionsFile).getOrThrow()

            val categoriesFile = File(exportDir, "categories.csv")
            ExportUtils.exportCategoriesToCSV(domainCategories, categoriesFile).getOrThrow()

            val budgetsFile = File(exportDir, "budgets.csv")
            ExportUtils.exportBudgetsToCSV(domainBudgets, budgetsFile).getOrThrow()

            val recurringFile = File(exportDir, "recurring_transactions.csv")
            ExportUtils.exportRecurringToCSV(domainRecurringTransactions, recurringFile).getOrThrow()

            val settingsFile = File(exportDir, "settings.json")
            val json = Json { ignoreUnknownKeys = true }
            settingsFile.writeText(json.encodeToString(settings))

            Result.success(exportDir)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun importData(file: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!file.exists() || !file.isDirectory) {
                return Result.failure(IllegalArgumentException("Invalid export directory"))
            }

            val transactionsFile = File(file, "transactions.csv")
            if (transactionsFile.exists()) {
                // Parse and insert transactions - requires CSV parsing logic
            }

            val categoriesFile = File(file, "categories.csv")
            if (categoriesFile.exists()) {
                // Parse and insert categories
            }

            val budgetsFile = File(file, "budgets.csv")
            if (budgetsFile.exists()) {
                // Parse and insert budgets
            }

            val recurringFile = File(file, "recurring_transactions.csv")
            if (recurringFile.exists()) {
                // Parse and insert recurring transactions
            }

            val settingsFile = File(file, "settings.json")
            if (settingsFile.exists()) {
                val json = Json { ignoreUnknownKeys = true }
                val settings = json.decodeFromString<Settings>(settingsFile.readText())
                updateTheme(settings.theme)
                updateCurrency(settings.currency)
                updateLanguage(settings.language)
                updateFirstDayOfWeek(settings.firstDayOfWeek.ordinal + 1)
                updateBudgetAlerts(settings.budgetAlertEnabled)
                updateRecurringEnabled(settings.recurringTransactionEnabled)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun backup(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val transactions = transactionDao.getAllByDateRange(0, Long.MAX_VALUE).await()
            val categories = categoryDao.getAll().first()
            val recurringTransactions = recurringTransactionDao.getAll().first()

            val domainTransactions = transactions.mapNotNull { entity ->
                categories.find { it.id == entity.categoryId }?.let { category ->
                    TransactionMapper.toDomain(entity, CategoryMapper.toDomain(category))
                }
            }

            val domainCategories = categories.map { CategoryMapper.toDomain(it) }

            val domainBudgets = mutableListOf<Budget>()
            val allBudgetEntities = budgetDao.getAll().await()
            for (budgetEntity in allBudgetEntities) {
                val category = categories.find { it.id == budgetEntity.categoryId }?.let { CategoryMapper.toDomain(it) }
                if (category != null) {
                    val spent = calculateSpentForBudget(budgetEntity.categoryId, budgetEntity.monthYear).await()
                    domainBudgets.add(BudgetMapper.toDomain(budgetEntity, category, spent))
                }
            }

            val domainRecurringTransactions = recurringTransactions.mapNotNull { entity ->
                categories.find { it.id == entity.categoryId }?.let { category ->
                    RecurringTransactionMapper.toDomain(entity, CategoryMapper.toDomain(category))
                }
            }

            val settings = getSettings().first()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
            val backupFile = File(context.getExternalFilesDir(null) ?: context.cacheDir, "backup_$timestamp.json")

            BackupUtils.createBackup(
                transactions = domainTransactions,
                categories = domainCategories,
                budgets = domainBudgets,
                recurringTransactions = domainRecurringTransactions,
                settings = settings,
                file = backupFile
            ).getOrThrow()

            Result.success(backupFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restore(backupFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!backupFile.exists()) {
                return Result.failure(IllegalArgumentException("Backup file does not exist"))
            }

            val backup = BackupUtils.restoreBackup(backupFile).getOrThrow()

            updateTheme(backup.settings.theme)
            updateCurrency(backup.settings.currency)
            updateLanguage(backup.settings.language)
            updateFirstDayOfWeek(backup.settings.firstDayOfWeek.ordinal + 1)
            updateBudgetAlerts(backup.settings.budgetAlertEnabled)
            updateRecurringEnabled(backup.settings.recurringTransactionEnabled)

            for (category in backup.categories) {
                if (!category.isSystem) {
                    val entity = CategoryMapper.toEntityForInsert(category)
                    categoryDao.insert(entity).await()
                }
            }

            for (recurring in backup.recurringTransactions) {
                val entity = RecurringTransactionMapper.toEntityForInsert(recurring)
                recurringTransactionDao.insert(entity).await()
            }

            for (budget in backup.budgets) {
                val entity = BudgetMapper.toEntityForInsert(budget)
                budgetDao.insert(entity).await()
            }

            for (transaction in backup.transactions) {
                val entity = TransactionMapper.toEntityForInsert(transaction)
                transactionDao.insert(entity).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun calculateSpentForBudget(categoryId: Long, monthYear: String): Double {
        val yearMonth = kotlinx.datetime.YearMonth.parse(monthYear)
        val startOfMonth = yearMonth.atDay(1).atStartOfDay(TimeZone.currentSystemDefault()).toInstant().epochMilliseconds
        val endOfMonth = yearMonth.atDay(yearMonth.daysInMonth).atTime(23, 59, 59).atZone(TimeZone.currentSystemDefault()).toInstant().epochMilliseconds
        return transactionDao.getTotalAmountByType(TransactionType.EXPENSE, startOfMonth, endOfMonth) ?: 0.0
    }
}