package com.expensetracker.data.repository

import com.expensetracker.data.local.dao.TransactionDao
import com.expensetracker.data.mapper.TransactionMapper
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import androidx.paging.flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryRepository: CategoryRepository
) : TransactionRepository {

    override fun getTransactions(
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        type: TransactionType? = null,
        categoryId: Long? = null,
        query: String? = null
    ): Flow<PagingSource<Int, Transaction>> {
        val startTimestamp = startDate?.toInstant(TimeZone.currentSystemDefault())?.epochMilliseconds ?: 0L
        val endTimestamp = endDate?.toInstant(TimeZone.currentSystemDefault())?.epochMilliseconds ?: Long.MAX_VALUE

        // This is a simplified implementation - in reality you'd use a more complex query
        return transactionDao.getByDateRange(startTimestamp, endTimestamp)
            .map { entities ->
                entities.map { entity ->
                    // Get category for each transaction
                    categoryRepository.getCategory(entity.categoryId)
                        ?.let { TransactionMapper.toDomain(entity, it) }
                }.filterNotNull()
            }
            .distinctUntilChanged()
            .flatMapLatest { transactions ->
                Pager(PagingConfig(pageSize = 20)) {
                    object : PagingSource<Int, Transaction>() {
                        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Transaction> {
                            val page = params.key ?: 1
                            val start = (page - 1) * 20
                            val end = start + 20
                            val pageData = transactions.drop(start).take(20)
                            return LoadResult.Page(
                                data = pageData,
                                prevKey = if (page == 1) null else page - 1,
                                nextKey = if (end < transactions.size) page + 1 else null
                            )
                        }
                    }
                }.flow
            }
            .cachedIn(viewModelScope)
    }

    override suspend fun getTransaction(id: Long): Transaction? {
        val entity = transactionDao.getById(id)
        return entity?.let { entity ->
            categoryRepository.getCategory(entity.categoryId)
                ?.let { TransactionMapper.toDomain(entity, it) }
        }
    }

    override suspend fun addTransaction(transaction: Transaction): Long {
        val entity = TransactionMapper.toEntityForInsert(transaction)
        return transactionDao.insert(entity)
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        val entity = TransactionMapper.toEntity(transaction)
        transactionDao.update(entity)
    }

    override suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteById(id)
    }

    override suspend fun getTotalAmount(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        type: TransactionType
    ): Double {
        val startTimestamp = startDate.toInstant(TimeZone.currentSystemDefault()).epochMilliseconds
        val endTimestamp = endDate.toInstant(TimeZone.currentSystemDefault()).epochMilliseconds
        return transactionDao.getTotalAmountByType(type, startTimestamp, endTimestamp) ?: 0.0
    }

    override suspend fun getAmountByCategory(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        type: TransactionType
    ): Map<Long, Double> {
        val startTimestamp = startDate.toInstant(TimeZone.currentSystemDefault()).epochMilliseconds
        val endTimestamp = endDate.toInstant(TimeZone.currentSystemDefault()).epochMilliseconds
        return transactionDao.getAmountByCategory(type, startTimestamp, endTimestamp).toMap()
    }

    override suspend fun getRecentTransactions(limit: Int = 10): List<Transaction> {
        val entities = transactionDao.getRecentTransactions(limit)
        return entities.mapNotNull { entity ->
            categoryRepository.getCategory(entity.categoryId)
                ?.let { TransactionMapper.toDomain(entity, it) }
        }
    }
}