package com.expensetracker.domain.usecase.recurring

import com.expensetracker.domain.model.RecurringTransaction
import com.expensetracker.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.Flow

class GetRecurringTransactionsUseCase @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository
) {
    operator fun invoke(activeOnly: Boolean = true): Flow<List<RecurringTransaction>> {
        return recurringTransactionRepository.getRecurringTransactions(activeOnly)
    }
}
