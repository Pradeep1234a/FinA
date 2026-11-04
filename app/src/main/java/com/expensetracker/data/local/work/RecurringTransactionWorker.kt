package com.expensetracker.data.local.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.expensetracker.data.local.AppDatabase
import com.expensetracker.data.local.dao.RecurringTransactionDao
import com.expensetracker.data.local.dao.TransactionDao
import com.expensetracker.data.local.entity.RecurringTransactionEntity
import com.expensetracker.data.local.entity.TransactionEntity
import com.expensetracker.domain.model.RecurrenceType
import com.expensetracker.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

/**
 * Worker to process due recurring transactions.
 * Runs periodically to check for and generate transactions from recurring templates.
 */
class RecurringTransactionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "RecurringTransactionWorker"
        const val WORK_NAME = "recurring_transaction_work"
        const val WORK_TAG = "recurring_transactions"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting recurring transaction processing")

        return try {
            withContext(Dispatchers.IO) {
                processDueRecurringTransactions()
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error processing recurring transactions", e)
            Result.retry()
        }
    }

    /**
     * Processes all due recurring transactions and generates corresponding transactions.
     */
    private suspend fun processDueRecurringTransactions() {
        val database = AppDatabase.getInstance(applicationContext)
        val recurringDao: RecurringTransactionDao = database.recurringTransactionDao()
        val transactionDao: TransactionDao = database.transactionDao()

        // Get all active recurring transactions that are due
        val dueRecurringTransactions = recurringDao.getDueRecurringTransactions()

        if (dueRecurringTransactions.isEmpty()) {
            Log.d(TAG, "No due recurring transactions found")
            return
        }

        Log.d(TAG, "Found ${dueRecurringTransactions.size} due recurring transactions")

        val currentDate = LocalDate.now(TimeZone.currentSystemDefault())
        val currentDateTime = LocalDateTime.now(TimeZone.currentSystemDefault())

        for (recurringEntity in dueRecurringTransactions) {
            try {
                generateTransactionFromRecurring(
                    recurringEntity,
                    transactionDao,
                    recurringDao,
                    currentDate,
                    currentDateTime
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error generating transaction for recurring ID: ${recurringEntity.id}", e)
                // Continue with other recurring transactions
            }
        }
    }

    /**
     * Generates a transaction from a recurring transaction template.
     */
    private suspend fun generateTransactionFromRecurring(
        recurringEntity: RecurringTransactionEntity,
        transactionDao: TransactionDao,
        recurringDao: RecurringTransactionDao,
        currentDate: LocalDate,
        currentDateTime: LocalDateTime
    ) {
        // Create new transaction entity from recurring template
        val transactionEntity = TransactionEntity(
            id = 0,
            amount = recurringEntity.amount,
            type = recurringEntity.type,
            categoryId = recurringEntity.categoryId,
            date = currentDateTime,
            note = recurringEntity.note,
            isRecurring = true,
            recurringTransactionId = recurringEntity.id,
            createdAt = currentDateTime,
            updatedAt = currentDateTime
        )

        // Insert the new transaction
        val newTransactionId = transactionDao.insert(transactionEntity)

        if (newTransactionId > 0) {
            Log.d(TAG, "Generated transaction ID: $newTransactionId from recurring ID: ${recurringEntity.id}")

            // Calculate next due date
            val nextDueDate = calculateNextDueDate(recurringEntity, currentDate)

            // Check if recurring transaction should continue
            val shouldContinue = recurringEntity.endDate == null || nextDueDate <= recurringEntity.endDate!!

            if (shouldContinue) {
                // Update recurring transaction with new next due date and last generated date
                val updatedEntity = recurringEntity.copy(
                    nextDueDate = nextDueDate,
                    lastGeneratedDate = currentDate
                )
                recurringDao.update(updatedEntity)
                Log.d(TAG, "Updated recurring ID: ${recurringEntity.id} next due date to: $nextDueDate")
            } else {
                // Deactivate the recurring transaction
                val deactivatedEntity = recurringEntity.copy(
                    isActive = false,
                    nextDueDate = nextDueDate,
                    lastGeneratedDate = currentDate
                )
                recurringDao.update(deactivatedEntity)
                Log.d(TAG, "Deactivated recurring ID: ${recurringEntity.id} (end date reached)")
            }
        } else {
            Log.w(TAG, "Failed to generate transaction from recurring ID: ${recurringEntity.id}")
        }
    }

    /**
     * Calculates the next due date based on recurrence type and interval.
     */
    private fun calculateNextDueDate(recurringEntity: RecurringTransactionEntity, fromDate: LocalDate): LocalDate {
        return when (RecurrenceType.valueOf(recurringEntity.recurrenceType)) {
            RecurrenceType.DAILY -> fromDate.plusDays(recurringEntity.recurrenceInterval.toLong())
            RecurrenceType.WEEKLY -> fromDate.plusWeeks(recurringEntity.recurrenceInterval.toLong())
            RecurrenceType.MONTHLY -> fromDate.plusMonths(recurringEntity.recurrenceInterval.toLong())
            RecurrenceType.YEARLY -> fromDate.plusYears(recurringEntity.recurrenceInterval.toLong())
        }
    }
}