package com.expensetracker.domain.usecase.transaction

import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import kotlin.Result

class GetTransactionsByDateRangeUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<List<Transaction>> {
        return try {
            val transactions = transactionRepository.getTransactions(
                startDate = startDate,
                endDate = endDate
            ).first()
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}