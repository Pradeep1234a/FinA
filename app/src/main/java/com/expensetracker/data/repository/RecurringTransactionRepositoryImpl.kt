package com.expensetracker.data.repository

import com.expensetracker.data.local.dao.RecurringTransactionDao
import com.expensetracker.data.local.dao.TransactionDao
import com.expensetracker.data.mapper.RecurringTransactionMapper
import com.expensetracker.data.mapper.TransactionMapper
import com.expensetracker.domain.model.RecurringTransaction
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import javax.inject.Inject

class RecurringTransactionRepositoryImpl @Inject constructor(
    private val recurringDao: RecurringTransactionDao,
    private val transactionDao: TransactionDao,
    private val categoryRepository: CategoryRepository
) : RecurringTransactionRepository {

    override fun getRecurringTransactions(activeOnly: Boolean = true): Flow<List<RecurringTransaction>> {
        if (activeOnly) {
            return recurringDao.getActive().map { entities ->
                entities.mapNotNull { entity ->
                    categoryRepository.getCategory(entity.categoryId)
                        ?.let { RecurringTransactionMapper.toDomain(entity, it) }
                }
            }
        }
        return recurringDao.getAll().map { entities ->
            entities.mapNotNull { entity ->
                categoryRepository.getCategory(entity.categoryId)
                    ?.let { RecurringTransactionMapper.toDomain(entity, it) }
            }
        }
    }

    override suspend fun getRecurringTransaction(id: Long): RecurringTransaction? {
        val entity = recurringDao.getById(id)
        return entity?.let { entity ->
            categoryRepository.getCategory(entity.categoryId)
                ?.let { RecurringTransactionMapper.toDomain(entity, it) }
        }
    }

    override suspend fun addRecurringTransaction(rt: RecurringTransaction): Long {
        val entity = RecurringTransactionMapper.toEntityForInsert(rt)
        return recurringDao.insert(entity)
    }

    override suspend fun updateRecurringTransaction(rt: RecurringTransaction) {
        val entity = RecurringTransactionMapper.toEntity(rt)
        recurringDao.update(entity)
    }

    override suspend fun deleteRecurringTransaction(id: Long) {
        recurringDao.deleteById(id)
    }

    override suspend fun getDueRecurringTransactions(): List<RecurringTransaction> {
        val now = System.currentTimeMillis()
        val entities = recurringDao.getDue(now)
        return entities.mapNotNull { entity ->
            categoryRepository.getCategory(entity.categoryId)
                ?.let { RecurringTransactionMapper.toDomain(entity, it) }
        }
    }

    override suspend fun markAsGenerated(id: Long, generatedDate: LocalDate) {
        val entity = recurringDao.getById(id) ?: return
        val nextDueDate = entity.nextDueDate
        val newNextDueDate = calculateNextDueDate(entity, generatedDate)
        recurringDao.updateGeneratedDate(id, generatedDate.atStartOfDay(TimeZone.currentSystemDefault()).toInstant().epochMilliseconds, newNextDueDate)
    }

    override suspend fun toggleActive(id: Long, isActive: Boolean) {
        recurringDao.setActive(id, isActive)
    }

    private fun calculateNextDueDate(entity: RecurringTransactionEntity, fromDate: LocalDate): Long {
        val recurrenceType = entity.recurrenceType
        val interval = entity.recurrenceInterval
        return when (recurrenceType) {
            DAILY -> fromDate.plusDays(interval.toLong()).atStartOfDay(TimeZone.currentSystemDefault()).toInstant().epochMilliseconds
            WEEKLY -> fromDate.plusWeeks(interval.toLong()).atStartOfDay(TimeZone.currentSystemDefault()).toInstant().epochMilliseconds
            MONTHLY -> fromDate.plusMonths(interval.toLong()).atStartOfDay(TimeZone.currentSystemDefault()).toInstant().epochMilliseconds
            YEARLY -> fromDate.plusYears(interval.toLong()).atStartOfDay(TimeZone.currentSystemDefault()).toInstant().epochMilliseconds
        }
    }

    suspend fun generateTransactionFromRecurring(rt: RecurringTransaction): Transaction {
        return Transaction(
            id = 0,
            amount = rt.amount,
            type = rt.type,
            category = rt.category,
            date = rt.nextDueDate.atStartOfDay(TimeZone.currentSystemDefault()),
            note = rt.note,
            isRecurring = true,
            recurringTransactionId = rt.id
        )
    }
}