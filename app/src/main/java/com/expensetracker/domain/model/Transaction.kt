package com.expensetracker.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val date: LocalDateTime,
    val note: String? = null,
    val isRecurring: Boolean = false,
    val recurringTransactionId: Long? = null
) {
    val isExpense: Boolean
        get() = type == TransactionType.EXPENSE

    val isIncome: Boolean
        get() = type == TransactionType.INCOME

    val signedAmount: Double
        get() = if (isExpense) -amount else amount

    companion object {
        fun copy(transaction: Transaction, amount: Double? = null, type: TransactionType? = null, category: Category? = null, date: LocalDateTime? = null, note: String? = null): Transaction {
            return transaction.copy(
                amount = amount ?: transaction.amount,
                type = type ?: transaction.type,
                category = category ?: transaction.category,
                date = date ?: transaction.date,
                note = note ?: transaction.note
            )
        }
    }
}