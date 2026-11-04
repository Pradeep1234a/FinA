package com.expensetracker.domain.usecase.transaction

import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        type: TransactionType? = null,
        categoryId: Long? = null,
        query: String? = null
    ): Flow<androidx.paging.PagingSource<Int, Transaction>> {
        return repository.getTransactions(startDate, endDate, type, categoryId, query)
    }
}