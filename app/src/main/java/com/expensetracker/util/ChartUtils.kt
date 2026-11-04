package com.expensetracker.util

import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.model.Category
import com.expensetracker.domain.model.Transaction
import com.expensetracker.presentation.ui.theme.CategoryColors
import com.expensetracker.presentation.ui.theme.IncomeColor
import com.expensetracker.presentation.ui.theme.ExpenseColor
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

object ChartUtils {

    data class PieChartEntry(
        val label: String,
        val value: Double,
        val color: Int
    )

    data class BarChartEntry(
        val label: String,
        val value: Double,
        val color: Int
    )

    data class LineChartEntry(
        val date: LocalDate,
        val income: Double,
        val expense: Double
    )

    data class BudgetChartEntry(
        val category: Category,
        val budget: Double,
        val spent: Double,
        val color: Int
    )

    fun prepareExpensePieChartData(
        transactions: List<Transaction>,
        categories: List<Category>
    ): List<PieChartEntry> {
        val categoryMap = categories.associateBy { it.id }
        val expenseByCategory = transactions
            .filter { it.type.name == "EXPENSE" }
            .groupBy { it.category.id }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        return expenseByCategory.entries.map { (categoryId, amount) ->
            val category = categoryMap[categoryId]
            val colorIndex = category?.sortOrder?.minus(1)?.coerceAtLeast(0) ?: 0
            val color = if (colorIndex < CategoryColors.size) CategoryColors[colorIndex].toArgb() else CategoryColors[0].toArgb()
            PieChartEntry(category?.name ?: "Unknown", amount, color)
        }.sortedByDescending { it.value }
    }

    fun prepareIncomePieChartData(
        transactions: List<Transaction>,
        categories: List<Category>
    ): List<PieChartEntry> {
        val categoryMap = categories.associateBy { it.id }
        val incomeByCategory = transactions
            .filter { it.type.name == "INCOME" }
            .groupBy { it.category.id }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        return incomeByCategory.entries.map { (categoryId, amount) ->
            val category = categoryMap[categoryId]
            val colorIndex = category?.sortOrder?.minus(1)?.coerceAtLeast(0) ?: 0
            val color = if (colorIndex < CategoryColors.size) CategoryColors[colorIndex].toArgb() else CategoryColors[0].toArgb()
            PieChartEntry(category?.name ?: "Unknown", amount, color)
        }.sortedByDescending { it.value }
    }

    fun prepareMonthlyBarChartData(
        transactions: List<Transaction>,
        months: Int = 6
    ): List<BarChartEntry> {
        val now = YearMonth.now()
        val monthlyData = mutableMapOf<YearMonth, Double>()

        for (i in 0 until months) {
            val month = now.minusMonths(i.toLong())
            monthlyData[month] = 0.0
        }

        for (t in transactions) {
            val month = YearMonth.from(t.date)
            if (monthlyData.containsKey(month)) {
                val current = monthlyData[month] ?: 0.0
                monthlyData[month] = if (t.type.name == "INCOME") current + t.amount else current - t.amount
            }
        }

        return monthlyData.entries
            .sortedBy { it.key }
            .map { (month, value) ->
                BarChartEntry(month.toString(), value, if (value >= 0) IncomeColor.toArgb() else ExpenseColor.toArgb())
            }
    }

    fun prepareTrendLineChartData(
        transactions: List<Transaction>,
        days: Int = 30
    ): List<LineChartEntry> {
        val now = LocalDate.now()
        val dailyData = mutableMapOf<LocalDate, Pair<Double, Double>>()

        for (i in 0 until days) {
            val date = now.minusDays(i.toLong())
            dailyData[date] = 0.0 to 0.0
        }

        for (t in transactions) {
            val date = t.date.toLocalDate()
            if (dailyData.containsKey(date)) {
                val (income, expense) = dailyData[date] ?: 0.0 to 0.0
                if (t.type.name == "INCOME") {
                    dailyData[date] = (income + t.amount) to expense
                } else {
                    dailyData[date] = income to (expense + t.amount)
                }
            }
        }

        return dailyData.entries
            .sortedBy { it.key }
            .map { (date, (income, expense)) ->
                LineChartEntry(date, income, expense)
            }
    }

    fun prepareBudgetChartData(budgets: List<Budget>): List<BudgetChartEntry> {
        return budgets.map { budget ->
            val colorIndex = budget.category.sortOrder.minus(1).coerceAtLeast(0)
            val color = if (colorIndex < CategoryColors.size) CategoryColors[colorIndex].toArgb() else CategoryColors[0].toArgb()
            BudgetChartEntry(budget.category, budget.amount, budget.spent, color)
        }.sortedByDescending { it.budget }
    }
}