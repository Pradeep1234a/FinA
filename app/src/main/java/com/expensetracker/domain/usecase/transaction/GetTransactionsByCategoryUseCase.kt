package com.expensetracker.domain.usecase.transaction

import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import kotlin.Result

class GetTransactionsByCategoryUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        categoryId: Long,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        type: TransactionType? = null
    ): Result<List<Transaction>> {
        return try {
            val transactions = transactionRepository.getTransactions(
                startDate = startDate,
                endDate = endDate,
                type = type,
                categoryId = categoryId
            ).first()
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}