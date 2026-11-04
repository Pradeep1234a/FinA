package com.expensetracker.presentation.viewmodel

import androidx.hilt.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.domain.repository.BudgetRepository
import com.expensetracker.domain.repository.CategoryRepository
import com.expensetracker.domain.repository.TransactionRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeUiState>>(UiState.Loading())
    val uiState: StateFlow<UiState<HomeUiState>> = _uiState

    data class HomeUiState(
        val totalIncome: Double = 0.0,
        val totalExpense: Double = 0.0,
        val balance: Double = 0.0,
        val recentTransactions: List<Transaction> = emptyList(),
        val categoryBreakdown: List<Pair<String, Double>> = emptyList(),
        val monthlyTrend: List<Pair<String, Double>> = emptyList(),
        val budgetProgress: List<BudgetProgress> = emptyList()
    )

    data class BudgetProgress(
        val categoryName: String,
        val spent: Double,
        val budget: Double,
        val progress: Float
    )

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading()
            try {
                val currentMonth = java.time.YearMonth.now()
                val startOfMonth = currentMonth.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

                val transactions = transactionRepository.getTransactionsByDateRange(startOfMonth, endOfMonth)
                val budgets = budgetRepository.getBudgetsForMonth(currentMonth.monthValue, currentMonth.year)
                val categories = categoryRepository.getAllCategories()

                val totalIncome = transactions.filter { it.type == TransactionType.Income }.sumOf { it.amount }
                val totalExpense = transactions.filter { it.type == TransactionType.Expense }.sumOf { it.amount }
                val balance = totalIncome - totalExpense

                val recentTransactions = transactions.sortedByDescending { it.date }.take(5)

                val categoryBreakdown = transactions
                    .filter { it.type == TransactionType.Expense }
                    .groupBy { it.categoryId }
                    .map { (categoryId, txns) ->
                        val category = categories.find { it.id == categoryId }
                        category?.name ?: "Unknown" to txns.sumOf { it.amount }
                    }
                    .sortedByDescending { it.second }

                val monthlyTrend = (0..5).map { monthsAgo ->
                    val month = currentMonth.minusMonths(monthsAgo)
                    val monthStart = month.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val monthEnd = month.atEndOfMonth().atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val monthTxns = transactionRepository.getTransactionsByDateRange(monthStart, monthEnd)
                    val monthExpense = monthTxns.filter { it.type == TransactionType.Expense }.sumOf { it.amount }
                    month.month.name.substring(0, 3) to monthExpense
                }.reversed()

                val budgetProgress = budgets.map { budget ->
                    val category = categories.find { it.id == budget.categoryId }
                    val spent = transactions
                        .filter { it.categoryId == budget.categoryId && it.type == TransactionType.Expense }
                        .sumOf { it.amount }
                    BudgetProgress(
                        categoryName = category?.name ?: "Unknown",
                        spent = spent,
                        budget = budget.amount,
                        progress = if (budget.amount > 0) (spent / budget.amount).toFloat() else 0f
                    )
                }

                _uiState.value = UiState.Success(HomeUiState(
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    balance = balance,
                    recentTransactions = recentTransactions,
                    categoryBreakdown = categoryBreakdown,
                    monthlyTrend = monthlyTrend,
                    budgetProgress = budgetProgress
                ))
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load dashboard")
            }
        }
    }

    fun refresh() {
        loadDashboardData()
    }
}