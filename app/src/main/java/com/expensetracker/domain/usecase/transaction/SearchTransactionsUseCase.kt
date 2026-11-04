package com.expensetracker.domain.usecase.transaction

import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import kotlin.Result

class SearchTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        query: String,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null
    ): Result<List<Transaction>> {
        return try {
            val transactions = transactionRepository.getTransactions(
                startDate = startDate,
                endDate = endDate,
                query = query
            ).first()
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}