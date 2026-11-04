package com.expensetracker.domain.usecase.category

import com.expensetracker.domain.model.Category
import com.expensetracker.domain.repository.CategoryRepository
import kotlin.Result

class GetCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(id: Long): Result<Category> {
        return try {
            val category = categoryRepository.getCategory(id)
            if (category != null) {
                Result.success(category)
            } else {
                Result.failure(IllegalArgumentException("Category not found with id: $id"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}