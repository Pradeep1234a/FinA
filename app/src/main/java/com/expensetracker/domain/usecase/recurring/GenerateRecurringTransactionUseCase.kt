package com.expensetracker.domain.usecase.recurring

import com.expensetracker.domain.model.RecurringTransaction
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.repository.RecurringTransactionRepository
import com.expensetracker.domain.repository.TransactionRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.Result

class GenerateRecurringTransactionUseCase @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(recurringTransactionId: Long): Result<Long> {
        return try {
            val recurring = recurringTransactionRepository.getRecurringTransaction(recurringTransactionId)
                ?: return Result.failure(IllegalArgumentException("Recurring transaction not found with id: $recurringTransactionId"))
            
            val transaction = Transaction(
                amount = recurring.amount,
                type = recurring.type,
                category = recurring.category,
                date = LocalDateTime.of(recurring.nextDueDate, kotlinx.datetime.LocalTime.MIDNIGHT),
                note = recurring.note,
                isRecurring = true,
                recurringTransactionId = recurring.id
            )
            
            val transactionId = transactionRepository.addTransaction(transaction)
            recurringTransactionRepository.markAsGenerated(recurring.id, recurring.nextDueDate)
            
            Result.success(transactionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
