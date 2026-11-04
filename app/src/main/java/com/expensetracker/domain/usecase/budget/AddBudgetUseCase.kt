package com.expensetracker.domain.usecase.budget

import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.repository.BudgetRepository
import kotlin.Result

class AddBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(budget: Budget): Result<Long> {
        return try {
            val id = budgetRepository.addBudget(budget)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
