package com.expensetracker.domain.usecase.transaction

import com.expensetracker.domain.model.TransactionType
import com.expensetracker.domain.repository.TransactionRepository
import kotlinx.datetime.LocalDateTime
import kotlin.Result

class GetTotalIncomeUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<Double> {
        return try {
            val total = transactionRepository.getTotalAmount(
                startDate = startDate,
                endDate = endDate,
                type = TransactionType.INCOME
            )
            Result.success(total)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}