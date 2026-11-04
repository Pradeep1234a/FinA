package com.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.expensetracker.domain.model.RecurrenceType
import com.expensetracker.domain.model.TransactionType

@Entity(
    tableName = "recurring_transactions",
    indices = [Index("nextDueDate"), Index("isActive")]
)
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val note: String? = null,
    val recurrenceType: RecurrenceType,
    val recurrenceInterval: Int = 1,
    val startDate: Long,
    val endDate: Long? = null,
    val nextDueDate: Long,
    val isActive: Boolean = true,
    val lastGeneratedDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)