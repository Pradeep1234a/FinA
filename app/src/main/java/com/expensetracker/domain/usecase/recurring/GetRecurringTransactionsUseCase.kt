package com.expensetracker.domain.usecase.recurring

import com.expensetracker.domain.model.RecurringTransaction
import com.expensetracker.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.first
import kotlin.Result

class GetRecurringTransactionsUseCase @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository
) {
    suspend operator fun invoke(activeOnly: Boolean = true): Result<List<RecurringTransaction>> {
        return try {
            val transactions = recurringTransactionRepository.getRecurringTransactions(activeOnly).first()
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
