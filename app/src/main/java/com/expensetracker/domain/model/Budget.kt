package com.expensetracker.domain.model

import kotlinx.datetime.YearMonth
import kotlinx.serialization.Serializable

@Serializable
data class Budget(
    val id: Long = 0,
    val category: Category,
    val amount: Double,
    val monthYear: YearMonth,
    val alertThreshold: Double = 0.8,
    val spent: Double = 0.0
) {
    val remaining: Double
        get() = amount - spent

    val progress: Double
        get() = if (amount > 0) spent / amount else 0.0

    val progressPercent: Int
        get() = (progress * 100).toInt()

    val isOverBudget: Boolean
        get() = spent > amount

    val isNearLimit: Boolean
        get() = progress >= alertThreshold && !isOverBudget

    val status: BudgetStatus
        get() {
            return when {
                isOverBudget -> BudgetStatus.OVER_BUDGET
                isNearLimit -> BudgetStatus.NEAR_LIMIT
                else -> BudgetStatus.ON_TRACK
            }
        }

    val alertAmount: Double
        get() = amount * alertThreshold

    companion object {
        fun copy(budget: Budget, amount: Double? = null, alertThreshold: Double? = null, spent: Double? = null): Budget {
            return budget.copy(
                amount = amount ?: budget.amount,
                alertThreshold = alertThreshold ?: budget.alertThreshold,
                spent = spent ?: budget.spent
            )
        }
    }
}

enum class BudgetStatus {
    ON_TRACK,
    NEAR_LIMIT,
    OVER_BUDGET
}