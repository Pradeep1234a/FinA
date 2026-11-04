package com.expensetracker.domain.usecase.recurring

import com.expensetracker.domain.model.RecurringTransaction
import com.expensetracker.domain.repository.RecurringTransactionRepository
import kotlin.Result

class AddRecurringTransactionUseCase @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository
) {
    operator fun invoke(recurringTransaction: RecurringTransaction): Result<Long> {
        return try {
            val id = recurringTransactionRepository.addRecurringTransaction(recurringTransaction)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
