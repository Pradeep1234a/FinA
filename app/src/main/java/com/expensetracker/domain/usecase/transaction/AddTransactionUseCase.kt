package com.expensetracker.domain.usecase.transaction

import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.repository.TransactionRepository
import kotlin.Result

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Long> {
        return try {
            val id = repository.addTransaction(transaction)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
