package com.expensetracker.domain.usecase.budget

import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth

class GetBudgetsUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    operator fun invoke(monthYear: YearMonth): Flow<List<Budget>> {
        return budgetRepository.getBudgets(monthYear)
    }
}