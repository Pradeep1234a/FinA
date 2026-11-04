package com.expensetracker.domain.usecase.budget

import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.repository.BudgetRepository
import kotlinx.datetime.YearMonth
import kotlin.Result

class GetBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(categoryId: Long, monthYear: YearMonth): Result<Budget> {
        return try {
            val budget = budgetRepository.getBudget(categoryId, monthYear)
            if (budget != null) {
                Result.success(budget)
            } else {
                Result.failure(IllegalArgumentException("Budget not found for category $categoryId and month $monthYear"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
