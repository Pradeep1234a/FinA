package com.expensetracker.presentation.ui.screen.budget

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AccountBalanceWallet
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
import com.expensetracker.presentation.theme.AppTheme
import com.expensetracker.presentation.theme.Green600
import com.expensetracker.presentation.ui.component.BudgetProgressCard
import com.expensetracker.presentation.ui.component.DatePicker
import com.expensetracker.presentation.ui.component.EmptyBudgetsState
import com.expensetracker.presentation.ui.component.LoadingState
import com.expensetracker.presentation.viewmodel.BudgetManagementViewModel
import java.time.Month
import java.time.Year

@Composable
fun BudgetManagementScreen(
    viewModel: BudgetManagementViewModel,
    onNavigateBack: () -> Unit
) {
    val budgets by viewModel.budgets.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<BudgetManagementViewModel.BudgetUiState?>(null) }
    var newBudgetCategoryId by remember { mutableStateOf<Long?>(null) }
    var newBudgetAmount by remember { mutableStateOf("") }

    AppTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            androidx.compose.material3.TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = { androidx.compose.material3.IconButton(onClick = onNavigateBack) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1C1C1C)) } },
                title = { Text(text = "Budgets", fontSize = 20.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C)) },
                actions = {
                    androidx.compose.material3.IconButton(onClick = {
                        editingBudget = null
                        newBudgetCategoryId = viewModel.getAvailableCategories().firstOrNull()?.id
                        newBudgetAmount = ""
                        showAddDialog = true
                    }) { Icon(imageVector = Icons.Default.Add, contentDescription = "Add Budget", tint = Color(0xFF1C1C1C)) }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1C1C1C))
            )

            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    MonthYearSelector(selectedMonth = selectedMonth, selectedYear = selectedYear, onMonthChange = { m -> viewModel.setMonthYear(m, selectedYear) }, onYearChange = { y -> viewModel.setMonthYear(selectedMonth, y) })
                    if (budgets.isEmpty()) {
                        EmptyBudgetsState(onAddClick = {
                            editingBudget = null
                            newBudgetCategoryId = viewModel.getAvailableCategories().firstOrNull()?.id
                            newBudgetAmount = ""
                            showAddDialog = true
                        })
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(budgets) { budgetState ->
                                BudgetItem(budgetState = budgetState, onEditClick = {
                                    editingBudget = budgetState
                                    newBudgetCategoryId = budgetState.budget.categoryId
                                    newBudgetAmount = budgetState.budget.amount.toString()
                                    showEditDialog = true
                                }, onDeleteClick = { viewModel.deleteBudget(budgetState.budget.id) { } })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || showEditDialog) {
        BudgetDialog(
            title = if (editingBudget != null) "Edit Budget" else "Add Budget",
            categories = viewModel.getAvailableCategories(),
            selectedCategoryId = newBudgetCategoryId,
            onCategoryChange = { newBudgetCategoryId = it },
            amount = newBudgetAmount,
            onAmountChange = { newBudgetAmount = it },
            onConfirm = {
                if (editingBudget != null) {
                    viewModel.updateBudget(editingBudget!!.budget.id, newBudgetAmount.toDoubleOrNull() ?: 0.0) { showEditDialog = false; editingBudget = null }
                } else {
                    newBudgetCategoryId?.let { catId -> viewModel.addBudget(catId, newBudgetAmount.toDoubleOrNull() ?: 0.0) { showAddDialog = false } }
                }
            },
            onDismiss = { showAddDialog = false; showEditDialog = false; editingBudget = null }
        )
    }
}

@Composable
fun MonthYearSelector(selectedMonth: Int, selectedYear: Int, onMonthChange: (Int) -> Unit, onYearChange: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Budget Period", fontSize = 16.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = Month.of(selectedMonth).name, fontSize = 16.sp, color = Color(0xFF1C1C1C))
                Text(text = selectedYear.toString(), fontSize = 16.sp, color = Color(0xFF1C1C1C))
            }
        }
    }
}

@Composable
fun BudgetItem(budgetState: BudgetManagementViewModel.BudgetUiState, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    budgetState.category?.let { cat ->
                        androidx.compose.foundation.layout.Box(modifier = Modifier.size(40.dp).background(Color(cat.color).copy(alpha = 0.2f))) { }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column {
                        budgetState.category?.let { cat -> Text(text = cat.name, fontSize = 16.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C)) }
                        Text(text = "Spent ${String.format("$%.2f", budgetState.spent)} of ${String.format("$%.2f", budgetState.budget.amount)}", fontSize = 14.sp, color = Color(0xFF757575))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.IconButton(onClick = onEditClick) { Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF757575)) }
                    androidx.compose.material3.IconButton(onClick = onDeleteClick) { Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC62828)) }
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                val progressColor = if (budgetState.isOverBudget) Color(0xFFC62828) else Green600
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val progress = budgetState.progress.coerceIn(0f, 1f)
                    val width = size.width * progress
                    drawRect(color = Color(0xFFEEEEEE), size = size, cornerRadius = 4.dp.toPx())
                    drawRect(color = progressColor, topLeft = androidx.compose.ui.geometry.Offset.Zero, size = androidx.compose.ui.geometry.Size(width, size.height), cornerRadius = 4.dp.toPx())
                }
            }
            if (budgetState.isOverBudget) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Over budget by ${String.format("$%.2f", budgetState.spent - budgetState.budget.amount)}", fontSize = 12.sp, color = Color(0xFFC62828), fontWeight = FontWeight.W600)
            }
        }
    }
}

@Composable
fun BudgetDialog(title: String, categories: List<Category>, selectedCategoryId: Long?, onCategoryChange: (Long?) -> Unit, amount: String, onAmountChange: (String) -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C)) }, text = {
        Column(modifier = Modifier.padding(24.dp).width(280.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "Category", fontSize = 14.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
            Text(text = "Budget Amount", fontSize = 14.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
            TextField(value = amount, onValueChange = onAmountChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text("0.00") }, keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number), singleLine = true)
        }
    }, confirmButton = {
        Button(onClick = onConfirm, enabled = selectedCategoryId != null && amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Green600, contentColor = Color.White, disabledContainerColor = Green600.copy(alpha = 0.5f))) { Text(text = "Save", fontSize = 14.sp, fontWeight = FontWeight.W600) }
    }, dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text(text = "Cancel", fontSize = 14.sp) } })
}