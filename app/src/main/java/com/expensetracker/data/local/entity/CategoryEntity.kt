package com.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.expensetracker.domain.model.CategoryType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,
    val color: Int,
    val type: CategoryType,
    val isSystem: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)