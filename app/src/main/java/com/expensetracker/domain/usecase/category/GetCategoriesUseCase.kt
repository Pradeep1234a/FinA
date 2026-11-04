package com.expensetracker.domain.usecase.category

import com.expensetracker.domain.model.Category
import com.expensetracker.domain.model.CategoryType
import com.expensetracker.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first
import kotlin.Result

class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(type: CategoryType? = null): Result<List<Category>> {
        return try {
            val categories = categoryRepository.getCategories(type).first()
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
