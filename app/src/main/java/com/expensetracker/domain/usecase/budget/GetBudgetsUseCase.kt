package com.expensetracker.domain.usecase.budget

import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.YearMonth
import kotlin.Result

class GetBudgetsUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(monthYear: YearMonth): Result<List<Budget>> {
        return try {
            val budgets = budgetRepository.getBudgets(monthYear).first()
            Result.success(budgets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
