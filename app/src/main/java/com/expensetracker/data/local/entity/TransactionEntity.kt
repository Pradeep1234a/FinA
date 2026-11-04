package com.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.expensetracker.domain.model.TransactionType

@Entity(
    tableName = "transactions",
    indices = [
        Index("date"),
        Index("categoryId"),
        Index("type"),
        Index("isRecurring")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val date: Long, // timestamp
    val note: String? = null,
    val isRecurring: Boolean = false,
    val recurringTransactionId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)