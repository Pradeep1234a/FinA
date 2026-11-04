package com.expensetracker.domain.repository

import com.expensetracker.domain.model.Budget
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth

interface BudgetRepository {
    fun getBudgets(monthYear: YearMonth): Flow<List<Budget>>

    suspend fun getBudget(categoryId: Long, monthYear: YearMonth): Budget?

    suspend fun addBudget(budget: Budget): Long

    suspend fun updateBudget(budget: Budget)

    suspend fun deleteBudget(id: Long)

    suspend fun getBudgetProgress(categoryId: Long, monthYear: YearMonth): BudgetProgress

    data class BudgetProgress(
        val budget: Budget,
        val spent: Double,
        val progress: Double,
        val status: Budget.BudgetStatus
    )
}