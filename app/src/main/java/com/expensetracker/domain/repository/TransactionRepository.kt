package com.expensetracker.domain.repository

import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import androidx.paging.PagingSource

interface TransactionRepository {
    fun getTransactions(
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        type: TransactionType? = null,
        categoryId: Long? = null,
        query: String? = null
    ): Flow<PagingSource<Int, Transaction>>

    suspend fun getTransaction(id: Long): Transaction?

    suspend fun addTransaction(transaction: Transaction): Long

    suspend fun updateTransaction(transaction: Transaction)

    suspend fun deleteTransaction(id: Long)

    suspend fun getTotalAmount(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        type: TransactionType
    ): Double

    suspend fun getAmountByCategory(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        type: TransactionType
    ): Map<Long, Double>

    suspend fun getRecentTransactions(limit: Int = 10): List<Transaction>
}