package com.expensetracker.presentation.viewmodel

import androidx.hilt.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.domain.model.Category
import com.expensetracker.domain.model.RecurringTransaction
import com.expensetracker.domain.model.RecurrenceType
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.domain.repository.CategoryRepository
import com.expensetracker.domain.repository.RecurringTransactionRepository
import com.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecurringTransactionsViewModel @Inject constructor(
    private val recurringRepository: RecurringTransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : BaseViewModel() {

    private val _recurringTransactions = MutableStateFlow<List<RecurringTransactionUiState>>(emptyList())
    val recurringTransactions: StateFlow<List<RecurringTransactionUiState>> = _recurringTransactions

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _saveState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val saveState: StateFlow<UiState<Unit>> = _saveState

    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val deleteState: StateFlow<UiState<Unit>> = _deleteState

    private val _toggleState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val toggleState: StateFlow<UiState<Unit>> = _toggleState

    data class RecurringTransactionUiState(
        val recurring: RecurringTransaction,
        val category: Category?,
        val isDueSoon: Boolean,
        val nextOccurrence: Long
    )

    init {
        loadCategories()
        loadRecurringTransactions()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.value = categoryRepository.getAllCategories()
            } catch (e: Exception) { }
        }
    }

    fun loadRecurringTransactions() {
        viewModelScope.launch {
            try {
                val recurring = recurringRepository.getAllRecurringTransactions()
                val now = System.currentTimeMillis()
                val states = recurring.map { recurring ->
                    val category = _categories.value.find { it.id == recurring.categoryId }
                    val nextOccurrence = calculateNextOccurrence(recurring)
                    val isDueSoon = nextOccurrence <= now + 86400000 // Within 24 hours
                    RecurringTransactionUiState(recurring, category, isDueSoon, nextOccurrence)
                }
                _recurringTransactions.value = states
            } catch (e: Exception) { }
        }
    }

    private fun calculateNextOccurrence(recurring: RecurringTransaction): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = recurring.startDate
        val now = System.currentTimeMillis()

        while (calendar.timeInMillis <= now) {
            when (recurring.recurrenceType) {
                RecurrenceType.Daily -> calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                RecurrenceType.Weekly -> calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
                RecurrenceType.Monthly -> calendar.add(java.util.Calendar.MONTH, 1)
                RecurrenceType.Yearly -> calendar.add(java.util.Calendar.YEAR, 1)
            }
        }
        return calendar.timeInMillis
    }

    fun addRecurringTransaction(
        type: TransactionType,
        amount: Double,
        categoryId: Long,
        note: String,
        recurrenceType: RecurrenceType,
        startDate: Long,
        endDate: Long?,
        onSuccess: () -> Unit
    ) {
        if (amount <= 0) return

        viewModelScope.launch {
            _saveState.value = UiState.Loading()
            try {
                val recurring = RecurringTransaction(
                    id = 0,
                    type = type,
                    amount = amount,
                    categoryId = categoryId,
                    note = note,
                    recurrenceType = recurrenceType,
                    startDate = startDate,
                    endDate = endDate,
                    isActive = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                recurringRepository.insertRecurringTransaction(recurring)
                _saveState.value = UiState.Success(Unit)
                loadRecurringTransactions()
                onSuccess()
            } catch (e: Exception) {
                _saveState.value = UiState.Error(e.message ?: "Failed to add recurring transaction")
            }
        }
    }

    fun updateRecurringTransaction(
        id: Long,
        amount: Double,
        categoryId: Long,
        note: String,
        recurrenceType: RecurrenceType,
        startDate: Long,
        endDate: Long?,
        isActive: Boolean,
        onSuccess: () -> Unit
    ) {
        if (amount <= 0) return

        viewModelScope.launch {
            _saveState.value = UiState.Loading()
            try {
                val recurring = recurringRepository.getRecurringTransactionById(id)?.copy(
                    amount = amount,
                    categoryId = categoryId,
                    note = note,
                    recurrenceType = recurrenceType,
                    startDate = startDate,
                    endDate = endDate,
                    isActive = isActive,
                    updatedAt = System.currentTimeMillis()
                )
                recurring?.let {
                    recurringRepository.updateRecurringTransaction(it)
                    _saveState.value = UiState.Success(Unit)
                    loadRecurringTransactions()
                    onSuccess()
                }
            } catch (e: Exception) {
                _saveState.value = UiState.Error(e.message ?: "Failed to update recurring transaction")
            }
        }
    }

    fun deleteRecurringTransaction(id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _deleteState.value = UiState.Loading()
            try {
                recurringRepository.deleteRecurringTransaction(id)
                _deleteState.value = UiState.Success(Unit)
                loadRecurringTransactions()
                onSuccess()
            } catch (e: Exception) {
                _deleteState.value = UiState.Error(e.message ?: "Failed to delete recurring transaction")
            }
        }
    }

    fun toggleActive(id: Long, isActive: Boolean) {
        viewModelScope.launch {
            _toggleState.value = UiState.Loading()
            try {
                val recurring = recurringRepository.getRecurringTransactionById(id)?.copy(
                    isActive = isActive,
                    updatedAt = System.currentTimeMillis()
                )
                recurring?.let {
                    recurringRepository.updateRecurringTransaction(it)
                    _toggleState.value = UiState.Success(Unit)
                    loadRecurringTransactions()
                }
            } catch (e: Exception) {
                _toggleState.value = UiState.Error(e.message ?: "Failed to toggle recurring transaction")
            }
        }
    }

    fun processDueTransactions() {
        viewModelScope.launch {
            val due = _recurringTransactions.value.filter { it.isDueSoon && it.recurring.isActive }
            due.forEach { state ->
                val transaction = com.expensetracker.domain.model.Transaction(
                    id = null,
                    type = state.recurring.type,
                    amount = state.recurring.amount,
                    categoryId = state.recurring.categoryId,
                    note = state.recurring.note,
                    date = System.currentTimeMillis(),
                    categoryName = state.category?.name ?: "",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                transactionRepository.insertTransaction(transaction)
            }
        }
    }
}