package com.expensetracker.presentation.viewmodel

import androidx.hilt.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.model.Category
import com.expensetracker.domain.repository.BudgetRepository
import com.expensetracker.domain.repository.CategoryRepository
import com.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetManagementViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : BaseViewModel() {

    private val _budgets = MutableStateFlow<List<BudgetUiState>>(emptyList())
    val budgets: StateFlow<List<BudgetUiState>> = _budgets

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _selectedMonth = MutableStateFlow<Int>(java.time.Month.from(java.time.LocalDate.now()).value)
    val selectedMonth: StateFlow<Int> = _selectedMonth

    private val _selectedYear = MutableStateFlow<Int>(java.time.Year.now().value)
    val selectedYear: StateFlow<Int> = _selectedYear

    private val _saveState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val saveState: StateFlow<UiState<Unit>> = _saveState

    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val deleteState: StateFlow<UiState<Unit>> = _deleteState

    data class BudgetUiState(
        val budget: Budget,
        val category: Category?,
        val spent: Double,
        val progress: Float,
        val isOverBudget: Boolean
    )

    init {
        loadCategories()
        loadBudgets()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.value = categoryRepository.getCategoriesByType(com.expensetracker.domain.model.CategoryType.Expense)
            } catch (e: Exception) { }
        }
    }

    fun loadBudgets() {
        viewModelScope.launch {
            try {
                val budgets = budgetRepository.getBudgetsForMonth(_selectedMonth.value, _selectedYear.value)
                val transactions = transactionRepository.getTransactionsByDateRange(
                    java.time.YearMonth.of(_selectedYear.value, _selectedMonth.value).atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    java.time.YearMonth.of(_selectedYear.value, _selectedMonth.value).atEndOfMonth().atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                )

                val budgetStates = budgets.map { budget ->
                    val category = _categories.value.find { it.id == budget.categoryId }
                    val spent = transactions
                        .filter { it.categoryId == budget.categoryId && it.type == com.expensetracker.domain.model.TransactionType.Expense }
                        .sumOf { it.amount }
                    val progress = if (budget.amount > 0) (spent / budget.amount).toFloat() else 0f
                    BudgetUiState(budget, category, spent, progress, spent > budget.amount)
                }
                _budgets.value = budgetStates
            } catch (e: Exception) { }
        }
    }

    fun setMonthYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
        loadBudgets()
    }

    fun addBudget(categoryId: Long, amount: Double, onSuccess: () -> Unit) {
        if (amount <= 0) return

        viewModelScope.launch {
            _saveState.value = UiState.Loading()
            try {
                val budget = Budget(
                    id = 0,
                    categoryId = categoryId,
                    amount = amount,
                    month = _selectedMonth.value,
                    year = _selectedYear.value,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                budgetRepository.insertBudget(budget)
                _saveState.value = UiState.Success(Unit)
                loadBudgets()
                onSuccess()
            } catch (e: Exception) {
                _saveState.value = UiState.Error(e.message ?: "Failed to add budget")
            }
        }
    }

    fun updateBudget(id: Long, amount: Double, onSuccess: () -> Unit) {
        if (amount <= 0) return

        viewModelScope.launch {
            _saveState.value = UiState.Loading()
            try {
                val budget = budgetRepository.getBudgetById(id)?.copy(
                    amount = amount,
                    updatedAt = System.currentTimeMillis()
                )
                budget?.let {
                    budgetRepository.updateBudget(it)
                    _saveState.value = UiState.Success(Unit)
                    loadBudgets()
                    onSuccess()
                }
            } catch (e: Exception) {
                _saveState.value = UiState.Error(e.message ?: "Failed to update budget")
            }
        }
    }

    fun deleteBudget(id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _deleteState.value = UiState.Loading()
            try {
                budgetRepository.deleteBudget(id)
                _deleteState.value = UiState.Success(Unit)
                loadBudgets()
                onSuccess()
            } catch (e: Exception) {
                _deleteState.value = UiState.Error(e.message ?: "Failed to delete budget")
            }
        }
    }

    fun getAvailableCategories(): List<Category> {
        val usedCategoryIds = _budgets.value.map { it.budget.categoryId }
        return _categories.value.filter { it.id !in usedCategoryIds }
    }
}