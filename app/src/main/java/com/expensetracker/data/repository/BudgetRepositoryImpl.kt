package com.expensetracker.data.repository

import com.expensetracker.data.local.dao.BudgetDao
import com.expensetracker.data.local.dao.TransactionDao
import com.expensetracker.data.mapper.BudgetMapper
import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.model.Category
import com.expensetracker.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao,
    private val categoryRepository: CategoryRepository
) : BudgetRepository {

    override fun getBudgets(monthYear: YearMonth): Flow<List<Budget>> {
        return budgetDao.getByMonth(monthYear.toString()).map { entities ->
            entities.mapNotNull { entity ->
                categoryRepository.getCategory(entity.categoryId)?.let { category ->
                    val spent = getSpentForCategory(category.id, monthYear)
                    BudgetMapper.toDomain(entity, category, spent)
                }
            }
        }
    }

    override suspend fun getBudget(categoryId: Long, monthYear: YearMonth): Budget? {
        val entity = budgetDao.getByCategoryAndMonth(categoryId, monthYear.toString())
        return entity?.let { entity ->
            categoryRepository.getCategory(categoryId)?.let { category ->
                val spent = getSpentForCategory(categoryId, monthYear)
                BudgetMapper.toDomain(entity, category, spent)
            }
        }
    }

    override suspend fun addBudget(budget: Budget): Long {
        val entity = BudgetMapper.toEntityForInsert(budget)
        return budgetDao.insert(entity)
    }

    override suspend fun updateBudget(budget: Budget) {
        val entity = BudgetMapper.toEntity(budget)
        budgetDao.update(entity)
    }

    override suspend fun deleteBudget(id: Long) {
        budgetDao.deleteById(id)
    }

    override suspend fun getBudgetProgress(categoryId: Long, monthYear: YearMonth): BudgetRepository.BudgetProgress {
        val budget = getBudget(categoryId, monthYear)
        val category = categoryRepository.getCategory(categoryId)
        val spent = getSpentForCategory(categoryId, monthYear)
        val budgetEntity = budget ?: Budget(
            id = 0,
            category = category!!,
            amount = 0.0,
            monthYear = monthYear,
            alertThreshold = 0.8,
            spent = spent
        )
        val progress = if (budgetEntity.amount > 0) spent / budgetEntity.amount else 0.0
        val status = when {
            spent > budgetEntity.amount -> Budget.BudgetStatus.OVER_BUDGET
            progress >= budgetEntity.alertThreshold -> Budget.BudgetStatus.NEAR_LIMIT
            else -> Budget.BudgetStatus.ON_TRACK
        }
        return BudgetRepository.BudgetProgress(budgetEntity, spent, progress, status)
    }

    private suspend fun getSpentForCategory(categoryId: Long, monthYear: YearMonth): Double {
        val startOfMonth = monthYear.atDay(1).atStartOfDay(TimeZone.currentSystemDefault()).toInstant().epochMilliseconds
        val endOfMonth = monthYear.atDay(monthYear.daysInMonth).atTime(23, 59, 59).atZone(TimeZone.currentSystemDefault()).toInstant().epochMilliseconds
        return transactionDao.getTotalAmountByType(TransactionType.EXPENSE, startOfMonth, endOfMonth) ?: 0.0
    }
}