package com.expensetracker.domain.usecase.recurring

import com.expensetracker.domain.model.RecurringTransaction
import com.expensetracker.domain.repository.RecurringTransactionRepository
import kotlin.Result

class UpdateRecurringTransactionUseCase @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository
) {
    operator fun invoke(recurringTransaction: RecurringTransaction): Result<Unit> {
        return try {
            recurringTransactionRepository.updateRecurringTransaction(recurringTransaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
