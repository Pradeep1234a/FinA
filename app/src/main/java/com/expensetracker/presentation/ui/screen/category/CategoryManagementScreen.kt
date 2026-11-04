package com.expensetracker.presentation.ui.screen.category

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Category
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
import com.expensetracker.domain.model.CategoryType
import com.expensetracker.presentation.theme.AppTheme
import com.expensetracker.presentation.theme.Green600
import com.expensetracker.presentation.ui.component.CategoryChip
import com.expensetracker.presentation.ui.component.EmptyState
import com.expensetracker.presentation.ui.component.LoadingState
import com.expensetracker.presentation.viewmodel.CategoryManagementViewModel

@Composable
fun CategoryManagementScreen(
    viewModel: CategoryManagementViewModel,
    onNavigateBack: () -> Unit
) {
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryIcon by remember { mutableStateOf("food") }
    var newCategoryColor by remember { mutableStateOf(0xFF1B5E20) }
    var newCategoryType by remember { mutableStateOf(CategoryType.Expense) }

    val icons = listOf("food", "transport", "shopping", "entertainment", "health", "education", "salary", "investment", "gift", "other")
    val colors = listOf(0xFF1B5E20, 0xFF1565C0, 0xFFBF360C, 0xFF6A1B9A, 0xFF00695C, 0xFFE65100, 0xFFC62828, 0xFF37474F, 0xFF5D4037, 0xFF455A64)

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
                title = { Text(text = "Categories", fontSize = 20.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C)) },
                actions = {
                    androidx.compose.material3.IconButton(onClick = {
                        editingCategory = null
                        newCategoryName = ""
                        newCategoryIcon = "food"
                        newCategoryColor = 0xFF1B5E20
                        newCategoryType = CategoryType.Expense
                        showAddDialog = true
                    }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Category", tint = Color(0xFF1C1C1C))
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
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Expense Categories
                    CategorySection(
                        title = "Expenses",
                        categories = expenseCategories,
                        type = CategoryType.Expense,
                        onAddClick = {
                            editingCategory = null
                            newCategoryName = ""
                            newCategoryIcon = "food"
                            newCategoryColor = 0xFF1B5E20
                            newCategoryType = CategoryType.Expense
                            showAddDialog = true
                        },
                        onEditClick = { category ->
                            editingCategory = category
                            newCategoryName = category.name
                            newCategoryIcon = category.icon
                            newCategoryColor = category.color
                            newCategoryType = category.type
                            showEditDialog = true
                        },
                        onDeleteClick = { category ->
                            viewModel.deleteCategory(category.id) { }
                        },
                        onReorder = { from, to ->
                            viewModel.reorderCategories(CategoryType.Expense, from, to)
                        }
                    )

                    // Income Categories
                    CategorySection(
                        title = "Income",
                        categories = incomeCategories,
                        type = CategoryType.Income,
                        onAddClick = {
                            editingCategory = null
                            newCategoryName = ""
                            newCategoryIcon = "salary"
                            newCategoryColor = 0xFF1B5E20
                            newCategoryType = CategoryType.Income
                            showAddDialog = true
                        },
                        onEditClick = { category ->
                            editingCategory = category
                            newCategoryName = category.name
                            newCategoryIcon = category.icon
                            newCategoryColor = category.color
                            newCategoryType = category.type
                            showEditDialog = true
                        },
                        onDeleteClick = { category ->
                            viewModel.deleteCategory(category.id) { }
                        },
                        onReorder = { from, to ->
                            viewModel.reorderCategories(CategoryType.Income, from, to)
                        }
                    )
                }
            }
        }
    }

    // Add/Edit Category Dialog
    if (showAddDialog || showEditDialog) {
        CategoryDialog(
            title = if (editingCategory != null) "Edit Category" else "Add Category",
            categoryName = newCategoryName,
            onNameChange = { newCategoryName = it },
            selectedIcon = newCategoryIcon,
            onIconChange = { newCategoryIcon = it },
            selectedColor = newCategoryColor,
            onColorChange = { newCategoryColor = it },
            selectedType = newCategoryType,
            onTypeChange = { newCategoryType = it },
            icons = icons,
            colors = colors,
            onConfirm = {
                if (editingCategory != null) {
                    viewModel.updateCategory(editingCategory!!.id, newCategoryName, newCategoryIcon, newCategoryColor) {
                        showEditDialog = false
                        editingCategory = null
                    }
                } else {
                    viewModel.addCategory(newCategoryName, newCategoryIcon, newCategoryColor, newCategoryType) {
                        showAddDialog = false
                    }
                }
            },
            onDismiss = {
                showAddDialog = false
                showEditDialog = false
                editingCategory = null
            }
        )
    }
}

@Composable
fun CategorySection(
    title: String,
    categories: List<Category>,
    type: CategoryType,
    onAddClick: () -> Unit,
    onEditClick: (Category) -> Unit,
    onDeleteClick: (Category) -> Unit,
    onReorder: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C))
            androidx.compose.material3.TextButton(onClick = onAddClick) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Green600, modifier = Modifier.size(20.dp))
                    Text(text = "Add", fontSize = 14.sp, color = Green600)
                }
            }
        }

        if (categories.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "No $title categories yet", fontSize = 16.sp, color = Color(0xFF757575))
                    Text(text = "Tap \"Add\" to create your first category", fontSize = 14.sp, color = Color(0xFF9E9E9E), modifier = Modifier.padding(top = 4.dp))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        onEditClick = { onEditClick(category) },
                        onDeleteClick = { onDeleteClick(category) },
                        isDefault = category.isDefault
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isDefault: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.size(40.dp).background(Color(category.color).copy(alpha = 0.2f))
                ) {
                    // Category icon placeholder
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                androidx.compose.foundation.layout.Column {
                    Text(text = category.name, fontSize = 16.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
                    if (isDefault) {
                        Text(text = "Default", fontSize = 10.sp, color = Color(0xFF1B5E20), fontWeight = FontWeight.W600)
                    }
                }
            }
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isDefault) {
                    androidx.compose.material3.IconButton(onClick = onEditClick) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF757575))
                    }
                    androidx.compose.material3.IconButton(onClick = onDeleteClick) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC62828))
                    }
                }
                Icon(imageVector = Icons.Default.DragHandle, contentDescription = "Drag to reorder", tint = Color(0xFF9E9E9E))
            }
        }
    }
}

@Composable
fun CategoryDialog(
    title: String,
    categoryName: String,
    onNameChange: (String) -> Unit,
    selectedIcon: String,
    onIconChange: (String) -> Unit,
    selectedColor: Int,
    onColorChange: (Int) -> Unit,
    selectedType: CategoryType,
    onTypeChange: (CategoryType) -> Unit,
    icons: List<String>,
    colors: List<Int>,
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
                TextField(
                    value = categoryName,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Category name") },
                    singleLine = true
                )

                // Type selection
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryType.values().forEach { type ->
                        val isSelected = selectedType == type
                        Chip(
                            onClick = { onTypeChange(type) },
                            selected = isSelected,
                            colors = androidx.compose.material3.ChipDefaults.colors(
                                selectedContainerColor = Green600,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFEEEEEE)
                            )
                        ) { Text(text = type.name, fontSize = 12.sp) }
                    }
                }

                // Icon selection
                Text(text = "Icon", fontSize = 14.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
                androidx.compose.foundation.layout.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(icons) { icon ->
                        val isSelected = selectedIcon == icon
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(if (isSelected) Color(selectedColor).copy(alpha = 0.2f) else Color(0xFFEEEEEE))
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        ) {
                            // Icon placeholder
                            androidx.compose.material3.Text(text = icon.substring(0, 1).uppercase(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(selectedColor), modifier = Modifier.align(Alignment.Center))
                        }.clickable { onIconChange(icon) }
                    }
                }

                // Color selection
                Text(text = "Color", fontSize = 14.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
                androidx.compose.foundation.layout.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colors) { color ->
                        val isSelected = selectedColor == color
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color(color))
                                .border(if (isSelected) androidx.compose.ui.graphics.BorderStroke(3.dp, Color.White) else androidx.compose.ui.graphics.BorderStroke(0.dp, Color.Transparent))
                        ).clickable { onColorChange(color) }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = categoryName.isNotBlank(),
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