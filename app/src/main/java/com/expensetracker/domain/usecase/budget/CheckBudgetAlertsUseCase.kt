package com.expensetracker.domain.usecase.budget

import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth

class CheckBudgetAlertsUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    operator fun invoke(monthYear: YearMonth): Flow<List<BudgetRepository.BudgetProgress>> {
        return budgetRepository.getBudgets(monthYear)
            .map { budgets ->
                budgets.map { budget ->
                    budgetRepository.getBudgetProgress(budget.category.id, monthYear)
                }.filter { it.status != Budget.BudgetStatus.ON_TRACK }
            }
    }
}