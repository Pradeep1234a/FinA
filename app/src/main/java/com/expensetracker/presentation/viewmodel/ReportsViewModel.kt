package com.expensetracker.presentation.viewmodel

import androidx.hilt.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.domain.model.Category
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.domain.repository.CategoryRepository
import com.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : BaseViewModel() {

    enum class Period { Week, Month, Quarter, Year, Custom }

    private val _selectedPeriod = MutableStateFlow<Period>(Period.Month)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod

    private val _customStartDate = MutableStateFlow<Long>(java.time.YearMonth.now().atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
    val customStartDate: StateFlow<Long> = _customStartDate

    private val _customEndDate = MutableStateFlow<Long>(java.time.YearMonth.now().atEndOfMonth().atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
    val customEndDate: StateFlow<Long> = _customEndDate

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _expenseBreakdown = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val expenseBreakdown: StateFlow<List<Pair<String, Double>>> = _expenseBreakdown

    private val _incomeBreakdown = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val incomeBreakdown: StateFlow<List<Pair<String, Double>>> = _incomeBreakdown

    private val _monthlyTrend = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val monthlyTrend: StateFlow<List<Pair<String, Double>>> = _monthlyTrend

    private val _totalIncome = MutableStateFlow<Double>(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome

    private val _totalExpense = MutableStateFlow<Double>(0.0)
    val totalExpense: StateFlow<Double> = _totalExpense

    private val _balance = MutableStateFlow<Double>(0.0)
    val balance: StateFlow<Double> = _balance

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Loading())
    val uiState: StateFlow<UiState<Unit>> = _uiState

    init {
        loadCategories()
        loadReportData()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.value = categoryRepository.getAllCategories()
            } catch (e: Exception) { }
        }
    }

    fun loadReportData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading()
            try {
                val (startDate, endDate) = getDateRange()
                val transactions = transactionRepository.getTransactionsByDateRange(startDate, endDate)

                val income = transactions.filter { it.type == TransactionType.Income }
                val expense = transactions.filter { it.type == TransactionType.Expense }

                val totalIncome = income.sumOf { it.amount }
                val totalExpense = expense.sumOf { it.amount }
                val balance = totalIncome - totalExpense

                _totalIncome.value = totalIncome
                _totalExpense.value = totalExpense
                _balance.value = balance

                val expenseByCategory = expense
                    .groupBy { it.categoryId }
                    .map { (catId, txns) ->
                        val cat = _categories.value.find { it.id == catId }
                        cat?.name ?: "Unknown" to txns.sumOf { it.amount }
                    }
                    .sortedByDescending { it.second }

                val incomeByCategory = income
                    .groupBy { it.categoryId }
                    .map { (catId, txns) ->
                        val cat = _categories.value.find { it.id == catId }
                        cat?.name ?: "Unknown" to txns.sumOf { it.amount }
                    }
                    .sortedByDescending { it.second }

                _expenseBreakdown.value = expenseByCategory
                _incomeBreakdown.value = incomeByCategory

                val trend = generateMonthlyTrend()
                _monthlyTrend.value = trend

                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load report")
            }
        }
    }

    private fun getDateRange(): Pair<Long, Long> {
        val now = java.time.LocalDate.now()
        return when (_selectedPeriod.value) {
            Period.Week -> {
                val start = now.minusWeeks(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val end = now.atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                start to end
            }
            Period.Month -> {
                val yearMonth = java.time.YearMonth.now()
                val start = yearMonth.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val end = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                start to end
            }
            Period.Quarter -> {
                val quarter = java.time.YearMonth.now().month.ordinal / 3
                val startMonth = quarter * 3 + 1
                val start = java.time.YearMonth.now().withMonth(startMonth).atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val end = java.time.YearMonth.now().withMonth(startMonth + 2).atEndOfMonth().atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                start to end
            }
            Period.Year -> {
                val year = java.time.Year.now()
                val start = year.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val end = year.atDay(year.lengthOfYear()).atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                start to end
            }
            Period.Custom -> _customStartDate.value to _customEndDate.value
        }
    }

    private fun generateMonthlyTrend(): List<Pair<String, Double>> {
        val now = java.time.YearMonth.now()
        return (0..5).map { monthsAgo ->
            val month = now.minusMonths(monthsAgo)
            val start = month.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val end = month.atEndOfMonth().atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val txns = transactionRepository.getTransactionsByDateRange(start, end)
            val expense = txns.filter { it.type == TransactionType.Expense }.sumOf { it.amount }
            month.month.name.substring(0, 3) to expense
        }.reversed()
    }

    fun setPeriod(period: Period) {
        _selectedPeriod.value = period
        loadReportData()
    }

    fun setCustomDateRange(start: Long, end: Long) {
        _customStartDate.value = start
        _customEndDate.value = end
        _selectedPeriod.value = Period.Custom
        loadReportData()
    }

    fun setCategoryFilter(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
        loadReportData()
    }
}