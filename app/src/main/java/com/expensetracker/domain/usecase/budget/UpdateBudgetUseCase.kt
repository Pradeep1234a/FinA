package com.expensetracker.domain.usecase.budget

import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.repository.BudgetRepository
import kotlin.Result

class UpdateBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(budget: Budget): Result<Unit> {
        return try {
            budgetRepository.updateBudget(budget)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
