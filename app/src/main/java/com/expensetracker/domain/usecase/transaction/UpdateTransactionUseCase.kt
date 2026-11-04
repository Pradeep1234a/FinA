package com.expensetracker.domain.usecase.transaction

import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.repository.TransactionRepository
import kotlin.Result

class UpdateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        return try {
            repository.updateTransaction(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
