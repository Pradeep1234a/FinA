package com.expensetracker.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class RecurringTransaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val note: String? = null,
    val recurrenceType: RecurrenceType,
    val recurrenceInterval: Int = 1,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val nextDueDate: LocalDate,
    val isActive: Boolean = true,
    val lastGeneratedDate: LocalDate? = null
) {
    val isExpired: Boolean
        get() = endDate != null && LocalDate.now() > endDate!!

    val isDue: Boolean
        get() = isActive && !isExpired && LocalDate.now() >= nextDueDate

    val frequencyDescription: String
        get() {
            val interval = if (recurrenceInterval == 1) "" else "$recurrenceInterval "
            return when (recurrenceType) {
                RecurrenceType.DAILY -> "${interval}Day(s)"
                RecurrenceType.WEEKLY -> "${interval}Week(s)"
                RecurrenceType.MONTHLY -> "${interval}Month(s)"
                RecurrenceType.YEARLY -> "${interval}Year(s)"
            }
        }

    fun calculateNextDueDate(fromDate: LocalDate = LocalDate.now()): LocalDate {
        return when (recurrenceType) {
            RecurrenceType.DAILY -> fromDate.plusDays(recurrenceInterval.toLong())
            RecurrenceType.WEEKLY -> fromDate.plusWeeks(recurrenceInterval.toLong())
            RecurrenceType.MONTHLY -> fromDate.plusMonths(recurrenceInterval.toLong())
            RecurrenceType.YEARLY -> fromDate.plusYears(recurrenceInterval.toLong())
        }
    }

    companion object {
        fun copy(
            recurring: RecurringTransaction,
            amount: Double? = null,
            type: TransactionType? = null,
            category: Category? = null,
            note: String? = null,
            recurrenceType: RecurrenceType? = null,
            recurrenceInterval: Int? = null,
            startDate: LocalDate? = null,
            endDate: LocalDate? = null,
            isActive: Boolean? = null
        ): RecurringTransaction {
            val newRecurrenceType = recurrenceType ?: recurring.recurrenceType
            val newInterval = recurrenceInterval ?: recurring.recurrenceInterval
            val newStartDate = startDate ?: recurring.startDate
            val newEndDate = endDate ?? recurring.endDate
            val newNextDueDate = if (recurrenceType != null || recurrenceInterval != null || startDate != null) {
                RecurringTransaction(
                    id = recurring.id,
                    amount = recurring.amount,
                    type = recurring.type,
                    category = recurring.category,
                    note = recurring.note,
                    recurrenceType = newRecurrenceType,
                    recurrenceInterval = newInterval,
                    startDate = newStartDate,
                    endDate = newEndDate,
                    nextDueDate = recurring.nextDueDate,
                    isActive = recurring.isActive,
                    lastGeneratedDate = recurring.lastGeneratedDate
                ).calculateNextDueDate(newStartDate)
            } else {
                recurring.nextDueDate
            }

            return recurring.copy(
                amount = amount ?: recurring.amount,
                type = type ?: recurring.type,
                category = category ?: recurring.category,
                note = note ?? recurring.note,
                recurrenceType = newRecurrenceType,
                recurrenceInterval = newInterval,
                startDate = newStartDate,
                endDate = newEndDate,
                nextDueDate = newNextDueDate,
                isActive = isActive ?: recurring.isActive
            )
        }
    }
}