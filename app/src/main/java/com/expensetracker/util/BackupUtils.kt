package com.expensetracker.util

import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.model.Category
import com.expensetracker.domain.model.RecurringTransaction
import com.expensetracker.domain.model.Settings
import com.expensetracker.domain.model.Transaction
import kotlinx.serialization.json.Json
import java.io.File

object BackupUtils {

    private val json = Json { ignoreUnknownKeys = true }

    @kotlinx.serialization.Serializable
    data class BackupData(
        val version: Int = 1,
        val timestamp: Long = System.currentTimeMillis(),
        val transactions: List<Transaction>,
        val categories: List<Category>,
        val budgets: List<Budget>,
        val recurringTransactions: List<RecurringTransaction>,
        val settings: Settings
    )

    fun createBackup(
        transactions: List<Transaction>,
        categories: List<Category>,
        budgets: List<Budget>,
        recurringTransactions: List<RecurringTransaction>,
        settings: Settings,
        file: File
    ): Result<File> {
        return try {
            val backup = BackupData(
                transactions = transactions,
                categories = categories,
                budgets = budgets,
                recurringTransactions = recurringTransactions,
                settings = settings
            )

            file.outputStream().use { out ->
                out.write(json.encodeToString(backup).toByteArray())
            }
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun restoreBackup(file: File): Result<BackupData> {
        return try {
            val jsonString = file.readText()
            val backup = json.decodeFromString<BackupData>(jsonString)

            if (backup.version > 1) {
                return Result.failure(IllegalArgumentException("Unsupported backup version: ${backup.version}"))
            }

            Result.success(backup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}