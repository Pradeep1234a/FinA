package com.expensetracker.data.mapper

import com.expensetracker.data.local.entity.RecurringTransactionEntity
import com.expensetracker.domain.model.Category
import com.expensetracker.domain.model.RecurringTransaction
import com.expensetracker.domain.model.RecurrenceType
import com.expensetracker.domain.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

object RecurringTransactionMapper {

    fun toDomain(entity: RecurringTransactionEntity, category: Category): RecurringTransaction {
        return RecurringTransaction(
            id = entity.id,
            amount = entity.amount,
            type = entity.type,
            category = category,
            note = entity.note,
            recurrenceType = entity.recurrenceType,
            recurrenceInterval = entity.recurrenceInterval,
            startDate = LocalDate.ofInstant(entity.startDate, TimeZone.currentSystemDefault()),
            endDate = entity.endDate?.let { LocalDate.ofInstant(it, TimeZone.currentSystemDefault()) },
            nextDueDate = LocalDate.ofInstant(entity.nextDueDate, TimeZone.currentSystemDefault()),
            isActive = entity.isActive,
            lastGeneratedDate = entity.lastGeneratedDate?.let { LocalDate.ofInstant(it, TimeZone.currentSystemDefault()) }
        )
    }

    fun toEntity(domain: RecurringTransaction): RecurringTransactionEntity {
        return RecurringTransactionEntity(
            id = domain.id,
            amount = domain.amount,
            type = domain.type,
            categoryId = domain.category.id,
            note = domain.note,
            recurrenceType = domain.recurrenceType,
            recurrenceInterval = domain.recurrenceInterval,
            startDate = domain.startDate.atStartOfDay(TimeZone.currentSystemDefault()).toInstant().epochMilliseconds,
            endDate = domain.endDate?.atStartOfDay(TimeZone.currentSystemDefault())?.toInstant()?.epochMilliseconds,
            nextDueDate = domain.nextDueDate.atStartOfDay(TimeZone.currentSystemDefault()).toInstant().epochMilliseconds,
            isActive = domain.isActive,
            lastGeneratedDate = domain.lastGeneratedDate?.atStartOfDay(TimeZone.currentSystemDefault())?.toInstant()?.epochMilliseconds
        )
    }

    fun toEntityForInsert(domain: RecurringTransaction): RecurringTransactionEntity {
        return RecurringTransactionEntity(
            id = 0,
            amount = domain.amount,
            type = domain.type,
            categoryId = domain.category.id,
            note = domain.note,
            recurrenceType = domain.recurrenceType,
            recurrenceInterval = domain.recurrenceInterval,
            startDate = domain.startDate.atStartOfDay(TimeZone.currentSystemDefault()).toInstant().epochMilliseconds,
            endDate = domain.endDate?.atStartOfDay(TimeZone.currentSystemDefault())?.toInstant()?.epochMilliseconds,
            nextDueDate = domain.nextDueDate.atStartOfDay(TimeZone.currentSystemDefault()).toInstant().epochMilliseconds,
            isActive = domain.isActive,
            lastGeneratedDate = domain.lastGeneratedDate?.atStartOfDay(TimeZone.currentSystemDefault())?.toInstant()?.epochMilliseconds
        )
    }
}