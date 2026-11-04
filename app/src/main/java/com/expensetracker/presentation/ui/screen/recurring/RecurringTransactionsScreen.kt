package com.expensetracker.presentation.ui.screen.recurring

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Chip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.ArrowBack
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
import com.expensetracker.domain.model.RecurrenceType
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.presentation.theme.AppTheme
import com.expensetracker.presentation.theme.Green600
import com.expensetracker.presentation.ui.component.DatePicker
import com.expensetracker.presentation.ui.component.EmptyRecurringState
import com.expensetracker.presentation.ui.component.LoadingState
import com.expensetracker.presentation.viewmodel.RecurringTransactionsViewModel

@Composable
fun RecurringTransactionsScreen(
    viewModel: RecurringTransactionsViewModel,
    onNavigateBack: () -> Unit
) {
    val recurringTransactions by viewModel.recurringTransactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()
    val toggleState by viewModel.toggleState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingRecurring by remember { mutableStateOf<RecurringTransactionsViewModel.RecurringTransactionUiState?>(null) }
    var newRecurringType by remember { mutableStateOf(TransactionType.Expense) }
    var newRecurringAmount by remember { mutableStateOf("") }
    var newRecurringCategoryId by remember { mutableStateOf<Long?>(null) }
    var newRecurringNote by remember { mutableStateOf("") }
    var newRecurringRecurrenceType by remember { mutableStateOf(RecurrenceType.Monthly) }
    var newRecurringStartDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var newRecurringEndDate by remember { mutableStateOf<Long?>(null) }
    var newRecurringIsActive by remember { mutableStateOf(true) }

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
                title = { Text(text = "Recurring Transactions", fontSize = 20.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C)) },
                actions = {
                    androidx.compose.material3.IconButton(onClick = {
                        editingRecurring = null
                        newRecurringType = TransactionType.Expense
                        newRecurringAmount = ""
                        newRecurringCategoryId = categories.filter { it.type == TransactionType.Expense }.firstOrNull()?.id
                        newRecurringNote = ""
                        newRecurringRecurrenceType = RecurrenceType.Monthly
                        newRecurringStartDate = System.currentTimeMillis()
                        newRecurringEndDate = null
                        newRecurringIsActive = true
                        showAddDialog = true
                    }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Recurring", tint = Color(0xFF1C1C1C))
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1C1C1C)
                )
            )

            // Content
            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (recurringTransactions.isEmpty()) {
                        EmptyRecurringState(onAddClick = {
                            editingRecurring = null
                            newRecurringType = TransactionType.Expense
                            newRecurringAmount = ""
                            newRecurringCategoryId = categories.filter { it.type == TransactionType.Expense }.firstOrNull()?.id
                            newRecurringNote = ""
                            newRecurringRecurrenceType = RecurrenceType.Monthly
                            newRecurringStartDate = System.currentTimeMillis()
                            newRecurringEndDate = null
                            newRecurringIsActive = true
                            showAddDialog = true
                        })
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(recurringTransactions) { recurringState ->
                                RecurringItem(
                                    recurringState = recurringState,
                                    categories = categories,
                                    onEditClick = {
                                        editingRecurring = recurringState
                                        newRecurringType = recurringState.recurring.type
                                        newRecurringAmount = recurringState.recurring.amount.toString()
                                        newRecurringCategoryId = recurringState.recurring.categoryId
                                        newRecurringNote = recurringState.recurring.note
                                        newRecurringRecurrenceType = recurringState.recurring.recurrenceType
                                        newRecurringStartDate = recurringState.recurring.startDate
                                        newRecurringEndDate = recurringState.recurring.endDate
                                        newRecurringIsActive = recurringState.recurring.isActive
                                        showEditDialog = true
                                    },
                                    onDeleteClick = {
                                        viewModel.deleteRecurringTransaction(recurringState.recurring.id) { }
                                    },
                                    onToggleClick = {
                                        viewModel.toggleActive(recurringState.recurring.id, !recurringState.recurring.isActive)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Recurring Transaction Dialog
    if (showAddDialog || showEditDialog) {
        RecurringTransactionDialog(
            title = if (editingRecurring != null) "Edit Recurring Transaction" else "Add Recurring Transaction",
            transactionType = newRecurringType,
            onTypeChange = { newRecurringType = it },
            amount = newRecurringAmount,
            onAmountChange = { newRecurringAmount = it },
            categories = categories.filter { it.type == newRecurringType },
            selectedCategoryId = newRecurringCategoryId,
            onCategoryChange = { newRecurringCategoryId = it },
            note = newRecurringNote,
            onNoteChange = { newRecurringNote = it },
            recurrenceType = newRecurringRecurrenceType,
            onRecurrenceTypeChange = { newRecurringRecurrenceType = it },
            startDate = newRecurringStartDate,
            onStartDateChange = { newRecurringStartDate = it },
            endDate = newRecurringEndDate,
            onEndDateChange = { newRecurringEndDate = it },
            isActive = newRecurringIsActive,
            onActiveChange = { newRecurringIsActive = it },
            onConfirm = {
                if (editingRecurring != null) {
                    viewModel.updateRecurringTransaction(
                        id = editingRecurring!!.recurring.id,
                        amount = newRecurringAmount.toDoubleOrNull() ?: 0.0,
                        categoryId = newRecurringCategoryId ?: 0,
                        note = newRecurringNote,
                        recurrenceType = newRecurringRecurrenceType,
                        startDate = newRecurringStartDate,
                        endDate = newRecurringEndDate,
                        isActive = newRecurringIsActive,
                        onSuccess = { showEditDialog = false; editingRecurring = null }
                    )
                } else {
                    newRecurringCategoryId?.let { catId ->
                        viewModel.addRecurringTransaction(
                            type = newRecurringType,
                            amount = newRecurringAmount.toDoubleOrNull() ?: 0.0,
                            categoryId = catId,
                            note = newRecurringNote,
                            recurrenceType = newRecurringRecurrenceType,
                            startDate = newRecurringStartDate,
                            endDate = newRecurringEndDate,
                            onSuccess = { showAddDialog = false }
                        )
                    }
                }
            },
            onDismiss = {
                showAddDialog = false
                showEditDialog = false
                editingRecurring = null
            }
        )
    }
}

@Composable
fun RecurringItem(
    recurringState: RecurringTransactionsViewModel.RecurringTransactionUiState,
    categories: List<Category>,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleClick: () -> Unit
) {
    val category = recurringState.category

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = if (recurringState.recurring.isActive) 2.dp else 1.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (recurringState.recurring.isActive) Color.White else Color(0xFFF5F5F5)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    category?.let { cat ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.size(40.dp).background(Color(cat.color).copy(alpha = 0.2f))
                        ) {
                            // Category icon
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = recurringState.recurring.type.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W600,
                                color = if (recurringState.recurring.type == TransactionType.Income) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                            )
                            Text(text = "·", fontSize = 16.sp, color = Color(0xFF9E9E9E))
                            category?.let { cat ->
                                Text(text = cat.name, fontSize = 16.sp, color = Color(0xFF757575))
                            }
                        }
                        Text(
                            text = String.format("$%.2f · %s", recurringState.recurring.amount, recurringState.recurring.recurrenceType.name),
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                        if (recurringState.recurring.note.isNotBlank()) {
                            Text(
                                text = recurringState.recurring.note,
                                fontSize = 12.sp,
                                color = Color(0xFF9E9E9E),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (recurringState.isDueSoon) {
                        Chip(
                            onClick = { },
                            colors = androidx.compose.material3.ChipDefaults.colors(
                                containerColor = Color(0xFFFFF3E0),
                                labelColor = Color(0xFFE65100)
                            )
                        ) {
                            Text(text = "Due Soon", fontSize = 10.sp, color = Color(0xFFE65100), fontWeight = FontWeight.W600)
                        }
                    }
                    Switch(
                        checked = recurringState.recurring.isActive,
                        onCheckedChange = { onToggleClick() },
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            thumbColor = Green600,
                            trackColor = Green600.copy(alpha = 0.3f)
                        )
                    )
                    androidx.compose.material3.IconButton(onClick = onEditClick) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF757575))
                    }
                    androidx.compose.material3.IconButton(onClick = onDeleteClick) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC62828))
                    }
                }
            }
        }
    }
}

@Composable
fun RecurringTransactionDialog(
    title: String,
    transactionType: TransactionType,
    onTypeChange: (TransactionType) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategoryChange: (Long?) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    recurrenceType: RecurrenceType,
    onRecurrenceTypeChange: (RecurrenceType) -> Unit,
    startDate: Long,
    onStartDateChange: (Long) -> Unit,
    endDate: Long?,
    onEndDateChange: (Long?) -> Unit,
    isActive: Boolean,
    onActiveChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C)) },
        text = {
            Column(
                modifier = Modifier.padding(24.dp).width(280.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Type selection
                Text(text = "Type", fontSize = 14.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.values().forEach { type ->
                        val isSelected = transactionType == type
                        Chip(
                            onClick = { onTypeChange(type) },
                            selected = isSelected,
                            colors = androidx.compose.material3.ChipDefaults.colors(
                                selectedContainerColor = if (type == TransactionType.Income) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFEEEEEE)
                            )
                        ) { Text(text = type.name, fontSize = 12.sp) }
                    }
                }

                // Amount
                Text(text = "Amount", fontSize = 14.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
                TextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00") },
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    singleLine = true
                )

                // Category
                Text(text = "Category", fontSize = 14.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
                androidx.compose.material3.DropdownMenuButton(onDropdownMenuOpened = { }) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        selectedCategoryId?.let { id ->
                            categories.find { it.id == id }?.let { cat ->
                                Text(text = cat.name, fontSize = 16.sp, color = Color(0xFF1C1C1C))
                            }
                        } ?: Text(text = "Select category", fontSize = 16.sp, color = Color(0xFF9E9E9E))
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF757575))
                    }
                }

                // Recurrence Type
                Text(text = "Frequency", fontSize = 14.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecurrenceType.values().forEach { type ->
                        val isSelected = recurrenceType == type
                        Chip(
                            onClick = { onRecurrenceTypeChange(type) },
                            selected = isSelected,
                            colors = androidx.compose.material3.ChipDefaults.colors(
                                selectedContainerColor = Green600,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFEEEEEE)
                            )
                        ) { Text(text = type.name, fontSize = 12.sp) }
                    }
                }

                // Start Date
                Text(text = "Start Date", fontSize = 14.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
                DatePicker.DatePickerButton(
                    selectedDate = java.time.Instant.ofEpochMilli(startDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    onDateSelected = { date ->
                        onStartDateChange(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                    }
                )

                // End Date (optional)
                Text(text = "End Date (Optional)", fontSize = 14.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
                DatePicker.DatePickerButton(
                    selectedDate = endDate?.let { java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate() } ?: java.time.LocalDate.now().plusYears(1),
                    onDateSelected = { date ->
                        onEndDateChange(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                    }
                )

                // Note
                Text(text = "Note", fontSize = 14.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
                TextField(
                    value = note,
                    onValueChange = onNoteChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Optional note") },
                    singleLine = true
                )

                // Active toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Active", fontSize = 16.sp, color = Color(0xFF1C1C1C))
                    Switch(
                        checked = isActive,
                        onCheckedChange = onActiveChange,
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            thumbColor = Green600,
                            trackColor = Green600.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0 && selectedCategoryId != null,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Green600,
                    contentColor = Color.White,
                    disabledContainerColor = Green600.copy(alpha = 0.5f)
                )
            ) { Text(text = "Save", fontSize = 14.sp, fontWeight = FontWeight.W600) }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text(text = "Cancel", fontSize = 14.sp) }
        }
    )
}