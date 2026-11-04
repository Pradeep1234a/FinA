package com.expensetracker.domain.usecase.category

import com.expensetracker.domain.model.Category
import com.expensetracker.domain.repository.CategoryRepository
import kotlin.Result

class AddCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(category: Category): Result<Long> {
        return try {
            val id = categoryRepository.addCategory(category)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}