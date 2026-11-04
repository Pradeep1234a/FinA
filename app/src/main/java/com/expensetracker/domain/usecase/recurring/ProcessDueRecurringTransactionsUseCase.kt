package com.expensetracker.domain.usecase.recurring

import com.expensetracker.domain.model.RecurringTransaction
import com.expensetracker.domain.repository.RecurringTransactionRepository
import com.expensetracker.domain.repository.TransactionRepository
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.Result

class ProcessDueRecurringTransactionsUseCase @Inject constructor(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(): Result<List<Long>> {
        return try {
            val dueTransactions = recurringTransactionRepository.getDueRecurringTransactions()
            val generatedIds = mutableListOf<Long>()
            
            for (recurring in dueTransactions) {
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
                generatedIds.add(transactionId)
                
                recurringTransactionRepository.markAsGenerated(recurring.id, recurring.nextDueDate)
            }
            
            Result.success(generatedIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
