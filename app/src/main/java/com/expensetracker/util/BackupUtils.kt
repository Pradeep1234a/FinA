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

    // Current backup version
    private const val CURRENT_VERSION = 1

    @kotlinx.serialization.Serializable
    data class BackupData(
        val version: Int = CURRENT_VERSION,
        val timestamp: Long = System.currentTimeMillis(),
        val transactions: List<Transaction>,
        val categories: List<Category>,
        val budgets: List<Budget>,
        val recurringTransactions: List<RecurringTransaction>,
        val settings: Settings
    )

    // Version 1 backup format (initial)
    @kotlinx.serialization.Serializable
    private data class BackupDataV1(
        val version: Int = 1,
        val timestamp: Long,
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

            // Try to decode as current version first
            val backup = try {
                json.decodeFromString<BackupData>(jsonString)
            } catch (e: Exception) {
                // If fails, try to decode as older version and migrate
                val v1Backup = json.decodeFromString<BackupDataV1>(jsonString)
                migrateFromV1(v1Backup)
            }

            if (backup.version > CURRENT_VERSION) {
                return Result.failure(IllegalArgumentException("Unsupported backup version: ${backup.version}"))
            }

            Result.success(backup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun migrateFromV1(v1Backup: BackupDataV1): BackupData {
        // Version 1 is the current version, so just return as-is
        // In the future, add migration logic here for older versions
        return BackupData(
            version = CURRENT_VERSION,
            timestamp = v1Backup.timestamp,
            transactions = v1Backup.transactions,
            categories = v1Backup.categories,
            budgets = v1Backup.budgets,
            recurringTransactions = v1Backup.recurringTransactions,
            settings = v1Backup.settings
        )
    }

    // Helper function to validate backup file without full restore
    fun validateBackupFile(file: File): Result<BackupData> {
        return try {
            if (!file.exists() || !file.isFile) {
                return Result.failure(IllegalArgumentException("Backup file does not exist"))
            }

            val jsonString = file.readText()
            val backup = json.decodeFromString<BackupData>(jsonString)

            // Basic validation
            if (backup.version < 1) {
                return Result.failure(IllegalArgumentException("Invalid backup version"))
            }

            Result.success(backup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}