package com.expensetracker.domain.usecase.transaction

import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.repository.TransactionRepository
import kotlin.Result

class GetTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(id: Long): Result<Transaction> {
        return try {
            val transaction = repository.getTransaction(id)
            if (transaction != null) {
                Result.success(transaction)
            } else {
                Result.failure(IllegalArgumentException("Transaction not found with id: $id"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}