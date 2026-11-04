package com.expensetracker.data.mapper

import com.expensetracker.data.local.entity.BudgetEntity
import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.model.Category
import kotlinx.datetime.YearMonth

object BudgetMapper {

    fun toDomain(entity: BudgetEntity, category: Category, spent: Double = 0.0): Budget {
        return Budget(
            id = entity.id,
            category = category,
            amount = entity.amount,
            monthYear = YearMonth.parse(entity.monthYear),
            alertThreshold = entity.alertThreshold,
            spent = spent
        )
    }

    fun toEntity(domain: Budget): BudgetEntity {
        return BudgetEntity(
            id = domain.id,
            categoryId = domain.category.id,
            amount = domain.amount,
            monthYear = domain.monthYear.toString(),
            alertThreshold = domain.alertThreshold
        )
    }

    fun toEntityForInsert(domain: Budget): BudgetEntity {
        return BudgetEntity(
            id = 0,
            categoryId = domain.category.id,
            amount = domain.amount,
            monthYear = domain.monthYear.toString(),
            alertThreshold = domain.alertThreshold
        )
    }
}