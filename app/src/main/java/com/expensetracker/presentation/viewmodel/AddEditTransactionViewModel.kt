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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : BaseViewModel() {

    private val _transactionType = MutableStateFlow<TransactionType>(TransactionType.Expense)
    val transactionType: StateFlow<TransactionType> = _transactionType

    private val _amount = MutableStateFlow<String>("")
    val amount: StateFlow<String> = _amount

    private val _categoryId = MutableStateFlow<Long?>(null)
    val categoryId: StateFlow<Long?> = _categoryId

    private val _note = MutableStateFlow<String>("")
    val note: StateFlow<String> = _note

    private val _date = MutableStateFlow<Long>(System.currentTimeMillis())
    val date: StateFlow<Long> = _date

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _isEditing = MutableStateFlow<Boolean>(false)
    val isEditing: StateFlow<Boolean> = _isEditing

    private val _transactionId = MutableStateFlow<Long?>(null)
    val transactionId: StateFlow<Long?> = _transactionId

    private val _validationErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val validationErrors: StateFlow<Map<String, String>> = _validationErrors

    private val _saveState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val saveState: StateFlow<UiState<Unit>> = _saveState

    init {
        loadCategories()
    }

    fun initialize(transactionType: TransactionType, transactionId: Long?) {
        _transactionType.value = transactionType
        _transactionId.value = transactionId
        _isEditing.value = transactionId != null

        if (transactionId != null) {
            loadTransaction(transactionId)
        } else {
            loadDefaultCategory()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val cats = categoryRepository.getCategoriesByType(_transactionType.value)
                _categories.value = cats
                if (_categoryId.value == null && cats.isNotEmpty()) {
                    _categoryId.value = cats.first().id
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun loadDefaultCategory() {
        viewModelScope.launch {
            try {
                val cats = categoryRepository.getCategoriesByType(_transactionType.value)
                if (cats.isNotEmpty()) {
                    _categoryId.value = cats.first().id
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            _saveState.value = UiState.Loading()
            try {
                val transaction = transactionRepository.getTransactionById(id)
                if (transaction != null) {
                    _amount.value = transaction.amount.toString()
                    _categoryId.value = transaction.categoryId
                    _note.value = transaction.note
                    _date.value = transaction.date
                }
                _saveState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _saveState.value = UiState.Error(e.message ?: "Failed to load transaction")
            }
        }
    }

    fun setAmount(value: String) {
        _amount.value = value
        validateAmount(value)
    }

    fun setCategoryId(id: Long?) {
        _categoryId.value = id
    }

    fun setNote(value: String) {
        _note.value = value
    }

    fun setDate(value: Long) {
        _date.value = value
    }

    private fun validateAmount(value: String) {
        val errors = mutableMapOf<String, String>()
        if (value.isBlank()) {
            errors["amount"] = "Amount is required"
        } else {
            try {
                val amount = value.toDouble()
                if (amount <= 0) {
                    errors["amount"] = "Amount must be greater than 0"
                }
            } catch (e: NumberFormatException) {
                errors["amount"] = "Invalid amount format"
            }
        }
        _validationErrors.value = errors
    }

    fun isValid(): Boolean {
        validateAmount(_amount.value)
        return _validationErrors.value.isEmpty() && _categoryId.value != null
    }

    fun saveTransaction(onSuccess: () -> Unit) {
        if (!isValid()) return

        viewModelScope.launch {
            _saveState.value = UiState.Loading()
            try {
                val transaction = Transaction(
                    id = _transactionId.value,
                    type = _transactionType.value,
                    amount = _amount.value.toDouble(),
                    categoryId = _categoryId.value!!,
                    note = _note.value,
                    date = _date.value,
                    categoryName = _categories.value.find { it.id == _categoryId.value }?.name ?: "",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                if (_isEditing.value) {
                    transactionRepository.updateTransaction(transaction)
                } else {
                    transactionRepository.insertTransaction(transaction)
                }

                _saveState.value = UiState.Success(Unit)
                onSuccess()
            } catch (e: Exception) {
                _saveState.value = UiState.Error(e.message ?: "Failed to save transaction")
            }
        }
    }

    fun deleteTransaction(onSuccess: () -> Unit) {
        _transactionId.value?.let { id ->
            viewModelScope.launch {
                _saveState.value = UiState.Loading()
                try {
                    transactionRepository.deleteTransaction(id)
                    _saveState.value = UiState.Success(Unit)
                    onSuccess()
                } catch (e: Exception) {
                    _saveState.value = UiState.Error(e.message ?: "Failed to delete transaction")
                }
            }
        }
    }
}