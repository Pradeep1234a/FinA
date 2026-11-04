package com.expensetracker.domain.usecase.category

import com.expensetracker.domain.model.Category
import com.expensetracker.domain.repository.CategoryRepository
import kotlin.Result

class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: Category): Result<Unit> {
        return try {
            categoryRepository.updateCategory(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
