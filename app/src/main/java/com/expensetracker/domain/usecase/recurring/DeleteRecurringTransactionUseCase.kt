package com.expensetracker.domain.usecase.recurring

import com.expensetracker.domain.repository.RecurringTransactionRepository
import kotlin.Result

class DeleteRecurringTransactionUseCase @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository
) {
    operator fun invoke(id: Long): Result<Unit> {
        return try {
            recurringTransactionRepository.deleteRecurringTransaction(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
