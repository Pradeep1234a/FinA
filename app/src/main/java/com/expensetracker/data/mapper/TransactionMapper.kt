package com.expensetracker.data.mapper

import com.expensetracker.data.local.entity.TransactionEntity
import com.expensetracker.domain.model.Category
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.TransactionType
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

object TransactionMapper {

    fun toDomain(entity: TransactionEntity, category: Category): Transaction {
        return Transaction(
            id = entity.id,
            amount = entity.amount,
            type = entity.type,
            category = category,
            date = LocalDateTime.ofInstant(entity.date, TimeZone.currentSystemDefault()),
            note = entity.note,
            isRecurring = entity.isRecurring,
            recurringTransactionId = entity.recurringTransactionId
        )
    }

    fun toEntity(domain: Transaction): TransactionEntity {
        return TransactionEntity(
            id = domain.id,
            amount = domain.amount,
            type = domain.type,
            categoryId = domain.category.id,
            date = domain.date.toInstant(TimeZone.currentSystemDefault()).epochMilliseconds,
            note = domain.note,
            isRecurring = domain.isRecurring,
            recurringTransactionId = domain.recurringTransactionId
        )
    }

    fun toEntityForInsert(domain: Transaction): TransactionEntity {
        return TransactionEntity(
            id = 0,
            amount = domain.amount,
            type = domain.type,
            categoryId = domain.category.id,
            date = domain.date.toInstant(TimeZone.currentSystemDefault()).epochMilliseconds,
            note = domain.note,
            isRecurring = domain.isRecurring,
            recurringTransactionId = domain.recurringTransactionId
        )
    }
}