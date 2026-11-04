package com.expensetracker.domain.usecase.category

import com.expensetracker.domain.model.Category
import com.expensetracker.domain.model.CategoryType
import com.expensetracker.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(type: CategoryType? = null): Flow<List<Category>> {
        return categoryRepository.getCategories(type)
    }
}