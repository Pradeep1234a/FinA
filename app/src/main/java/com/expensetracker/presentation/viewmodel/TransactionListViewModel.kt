package com.expensetracker.presentation.viewmodel

import androidx.hilt.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.flow
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.domain.repository.TransactionRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : BaseViewModel() {

    private val _transactionType = MutableStateFlow<TransactionType>(TransactionType.Expense)
    val transactionType: StateFlow<TransactionType> = _transactionType

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId

    private val _startDate = MutableStateFlow<Long>(java.time.YearMonth.now().atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
    val startDate: StateFlow<Long> = _startDate

    private val _endDate = MutableStateFlow<Long>(java.time.YearMonth.now().atEndOfMonth().atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
    val endDate: StateFlow<Long> = _endDate

    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sortOrder = MutableStateFlow<SortOrder>(SortOrder.DateDesc)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    enum class SortOrder {
        DateAsc, DateDesc, AmountAsc, AmountDesc, CategoryAsc, CategoryDesc
    }

    val pager = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = {
            TransactionPagingSource(
                repository = transactionRepository,
                type = _transactionType.value,
                categoryId = _selectedCategoryId.value,
                startDate = _startDate.value,
                endDate = _endDate.value,
                searchQuery = _searchQuery.value,
                sortOrder = _sortOrder.value
            )
        }
    ).flow.cachedIn(viewModelScope)

    fun setTransactionType(type: TransactionType) { _transactionType.value = type }
    fun setCategoryFilter(categoryId: Long?) { _selectedCategoryId.value = categoryId }
    fun setDateRange(start: Long, end: Long) { _startDate.value = start; _endDate.value = end }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSortOrder(order: SortOrder) { _sortOrder.value = order }
    fun clearFilters() {
        _selectedCategoryId.value = null
        _searchQuery.value = ""
        val now = java.time.YearMonth.now()
        _startDate.value = now.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        _endDate.value = now.atEndOfMonth().atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}

class TransactionPagingSource(
    private val repository: TransactionRepository,
    private val type: TransactionType,
    private val categoryId: Long?,
    private val startDate: Long,
    private val endDate: Long,
    private val searchQuery: String,
    private val sortOrder: TransactionListViewModel.SortOrder
) : androidx.paging.PagingSource<Int, Transaction>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Transaction> {
        val page = params.key ?: 1
        val limit = params.loadSize
        val offset = (page - 1) * limit

        try {
            val transactions = repository.getTransactionsPaged(
                type = type, categoryId = categoryId, startDate = startDate, endDate = endDate,
                searchQuery = searchQuery, sortOrder = sortOrder, limit = limit, offset = offset
            )
            val hasNext = transactions.size == limit
            return LoadResult.Page(data = transactions, prevKey = if (page == 1) null else page - 1, nextKey = if (hasNext) page + 1 else null)
        } catch (e: Exception) { return LoadResult.Error(e) }
    }

    override fun getRefreshKey(state: PagingState<Int, Transaction>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1) ?: 1
            anchorPage
        }
    }
}