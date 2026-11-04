package com.expensetracker.domain.usecase.category

import com.expensetracker.domain.repository.CategoryRepository
import kotlin.Result

class InitializeDefaultCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            categoryRepository.initializeDefaultCategories()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
