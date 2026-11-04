package com.expensetracker.domain.usecase.budget

import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.YearMonth
import kotlin.Result

class CheckBudgetAlertsUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(monthYear: YearMonth): Result<List<BudgetRepository.BudgetProgress>> {
        return try {
            val budgets = budgetRepository.getBudgets(monthYear).first()
            val alerts = budgets.map { budget ->
                budgetRepository.getBudgetProgress(budget.category.id, monthYear)
            }.filter { it.status != Budget.BudgetStatus.ON_TRACK }
            Result.success(alerts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
