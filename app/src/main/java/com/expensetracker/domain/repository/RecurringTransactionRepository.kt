package com.expensetracker.domain.repository

import com.expensetracker.domain.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface RecurringTransactionRepository {
    fun getRecurringTransactions(activeOnly: Boolean = true): Flow<List<RecurringTransaction>>

    suspend fun getRecurringTransaction(id: Long): RecurringTransaction?

    suspend fun addRecurringTransaction(rt: RecurringTransaction): Long

    suspend fun updateRecurringTransaction(rt: RecurringTransaction)

    suspend fun deleteRecurringTransaction(id: Long)

    suspend fun getDueRecurringTransactions(): List<RecurringTransaction>

    suspend fun markAsGenerated(id: Long, generatedDate: LocalDate)

    suspend fun toggleActive(id: Long, isActive: Boolean)
}