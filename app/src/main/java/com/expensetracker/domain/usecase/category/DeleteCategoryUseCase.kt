package com.expensetracker.domain.usecase.category

import com.expensetracker.domain.repository.CategoryRepository
import kotlin.Result

class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(id: Long): Result<Unit> {
        return try {
            categoryRepository.deleteCategory(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}