package com.expensetracker.presentation.ui.screen.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Chip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.domain.model.Category
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.presentation.theme.AppTheme
import com.expensetracker.presentation.theme.Green600
import com.expensetracker.presentation.ui.component.CategoryPicker
import com.expensetracker.presentation.ui.component.DatePicker
import com.expensetracker.presentation.ui.component.ErrorState
import com.expensetracker.presentation.ui.component.LoadingState
import com.expensetracker.presentation.viewmodel.AddEditTransactionViewModel

@Composable
fun AddEditTransactionScreen(
    viewModel: AddEditTransactionViewModel,
    transactionType: TransactionType,
    transactionId: Long?,
    onNavigateBack: () -> Unit
) {
    val amount by viewModel.amount.collectAsState()
    val categoryId by viewModel.categoryId.collectAsState()
    val note by viewModel.note.collectAsState()
    val date by viewModel.date.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val validationErrors by viewModel.validationErrors.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()

    var showCategoryPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    AppTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            androidx.compose.material3.TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1C1C1C))
                    }
                },
                title = {
                    Text(
                        text = if (isEditing) "Edit Transaction" else "Add ${transactionType.name}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.W600,
                        color = Color(0xFF1C1C1C)
                    )
                },
                actions = {
                    if (isEditing) {
                        androidx.compose.material3.IconButton(onClick = { viewModel.deleteTransaction(onNavigateBack) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC62828))
                        }
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1C1C1C)
                )
            )

            // Form
            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (saveState is com.expensetracker.presentation.viewmodel.UiState.Loading) {
                    LoadingState()
                } else {
                    TransactionForm(
                        amount = amount,
                        onAmountChange = viewModel::setAmount,
                        amountError = validationErrors["amount"],
                        categoryId = categoryId,
                        onCategoryClick = { showCategoryPicker = true },
                        categories = categories,
                        note = note,
                        onNoteChange = viewModel::setNote,
                        date = date,
                        onDateClick = { showDatePicker = true },
                        transactionType = transactionType,
                        isEditing = isEditing,
                        onSave = { viewModel.saveTransaction(onNavigateBack) },
                        isSaving = saveState is com.expensetracker.presentation.viewmodel.UiState.Loading
                    )
                }
            }
        }
    }

    // Category Picker Bottom Sheet
    CategoryPicker(
        categories = categories,
        selectedCategoryId = categoryId,
        onCategorySelected = { id ->
            viewModel.setCategoryId(id)
            showCategoryPicker = false
        },
        onDismiss = { showCategoryPicker = false }
    )

    // Date Picker
    if (showDatePicker) {
        DatePickerButton(
            selectedDate = java.time.Instant.ofEpochMilli(date).atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
            onDateSelected = { newDate ->
                viewModel.setDate(newDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                showDatePicker = false
            }
        )
    }
}

@Composable
fun TransactionForm(
    amount: String,
    onAmountChange: (String) -> Unit,
    amountError: String?,
    categoryId: Long?,
    onCategoryClick: () -> Unit,
    categories: List<Category>,
    note: String,
    onNoteChange: (String) -> Unit,
    date: Long,
    onDateClick: () -> Unit,
    transactionType: TransactionType,
    isEditing: Boolean,
    onSave: () -> Unit,
    isSaving: Boolean
) {
    val selectedCategory = categories.find { it.id == categoryId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Amount Field
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (transactionType == TransactionType.Income) "+" else "-", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (transactionType == TransactionType.Income) Color(0xFF1B5E20) else Color(0xFFB71C1C))
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = amount,
                        onValueChange = onAmountChange,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        placeholder = { Text("0.00", color = Color(0xFF9E9E9E)) },
                        singleLine = true,
                        colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
                            containerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color.White
                        )
                    )
                }
                amountError?.let { error ->
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                    Text(text = error, fontSize = 12.sp, color = Color(0xFFC62828))
                }
            }
        }

        // Category Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            androidx.compose.material3.TextButton(
                onClick = onCategoryClick,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Category, contentDescription = null, tint = Color(0xFF757575))
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Category", fontSize = 16.sp, color = Color(0xFF1C1C1C))
                            selectedCategory?.let { cat ->
                                Text(text = cat.name, fontSize = 14.sp, color = Color(0xFF757575))
                            }
                        }
                    }
                    Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color(0xFF9E9E9E))
                }
            }
        }

        // Date Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            androidx.compose.material3.TextButton(
                onClick = onDateClick,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF757575))
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Date", fontSize = 16.sp, color = Color(0xFF1C1C1C))
                            Text(text = java.time.Instant.ofEpochMilli(date).atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")), fontSize = 14.sp, color = Color(0xFF757575))
                        }
                    }
                    Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color(0xFF9E9E9E))
                }
            }
        }

        // Note Field
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Note, contentDescription = null, tint = Color(0xFF757575))
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Note (Optional)", fontSize = 16.sp, color = Color(0xFF1C1C1C))
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = note,
                    onValueChange = onNoteChange,
                    modifier = Modifier.fillMaxWidth().padding(start = 28.dp),
                    placeholder = { Text("Add a note...", color = Color(0xFF9E9E9E)) },
                    minLines = 3,
                    maxLines = 5,
                    colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color.White
                    )
                )
            }
        }

        // Save Button
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isSaving,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Green600,
                contentColor = Color.White,
                disabledContainerColor = Green600.copy(alpha = 0.5f)
            )
        ) {
            if (isSaving) {
                androidx.compose.material3.CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp).padding(end = 8.dp))
            }
            Text(text = if (isEditing) "Update Transaction" else "Save Transaction", fontSize = 16.sp, fontWeight = FontWeight.W600)
        }
    }
}