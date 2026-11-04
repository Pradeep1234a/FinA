package com.expensetracker.domain.repository

import com.expensetracker.domain.model.Category
import com.expensetracker.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(type: CategoryType? = null): Flow<List<Category>>

    suspend fun getCategory(id: Long): Category?

    suspend fun addCategory(category: Category): Long

    suspend fun updateCategory(category: Category)

    suspend fun deleteCategory(id: Long)

    suspend fun initializeDefaultCategories()

    suspend fun reorderCategories(categoryIds: List<Long>)
}