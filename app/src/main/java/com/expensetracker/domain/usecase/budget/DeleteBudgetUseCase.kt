package com.expensetracker.domain.usecase.budget

import com.expensetracker.domain.repository.BudgetRepository
import kotlin.Result

class DeleteBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        return try {
            budgetRepository.deleteBudget(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
