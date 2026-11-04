package com.expensetracker.domain.usecase.transaction

import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.repository.TransactionRepository
import kotlin.Result

class GetRecentTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(limit: Int = 10): Result<List<Transaction>> {
        return try {
            val transactions = transactionRepository.getRecentTransactions(limit)
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}