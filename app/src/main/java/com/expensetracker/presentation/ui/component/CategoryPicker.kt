package com.expensetracker.presentation.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Chip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.BottomSheetState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetLayout
import androidx.compose.material3.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.domain.model.Category
import com.expensetracker.domain.model.CategoryType

@Composable
fun CategoryPicker(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            CategoryPickerContent(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = { categoryId ->
                    onCategorySelected(categoryId)
                    coroutineScope.launch { bottomSheetState.hide() }
                },
                onDismiss = {
                    coroutineScope.launch { bottomSheetState.hide() }
                    onDismiss()
                }
            )
        },
        sheetShape = androidx.compose.material3.Shapes.large.copy(topStart = androidx.compose.foundation.shape.CornerSize(24.dp), topEnd = androidx.compose.foundation.shape.CornerSize(24.dp))
    ) {
        androidx.compose.material3.TextButton(onClick = { coroutineScope.launch { bottomSheetState.show() } }) {
            Text("Select Category")
        }
    }
}

@Composable
fun CategoryPickerContent(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Select Category", fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.W600, color = Color(0xFF1C1C1C))
            androidx.compose.material3.IconButton(onClick = onDismiss) {
                Icon(imageVector = androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF757575))
            }
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onCategorySelected(null) },
                    selected = selectedCategoryId == null,
                    colors = androidx.compose.material3.ChipDefaults.colors(
                        selectedContainerColor = Color(0xFF1B5E20),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFEEEEEE),
                        labelColor = Color(0xFF1C1C1C)
                    )
                ) {
                    Text(text = "All Categories", fontSize = 16.sp, color = if (selectedCategoryId == null) Color.White else Color(0xFF1C1C1C))
                }
            }
            items(categories) { category ->
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onCategorySelected(category.id) },
                    selected = selectedCategoryId == category.id,
                    colors = androidx.compose.material3.ChipDefaults.colors(
                        selectedContainerColor = Color(category.color),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFEEEEEE),
                        labelColor = Color(0xFF1C1C1C)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        androidx.compose.ui.graphics.vector.ImageVector
                        Icon(
                            imageVector = getCategoryIcon(category.icon),
                            contentDescription = null,
                            tint = if (selectedCategoryId == category.id) Color.White else Color(category.color),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(text = category.name, fontSize = 16.sp, color = if (selectedCategoryId == category.id) Color.White else Color(0xFF1C1C1C))
                    }
                }
            }
        }
    }
}

@Composable
fun getCategoryIcon(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName) {
        "food" -> androidx.compose.material.icons.Icons.Default.Fastfood
        "transport" -> androidx.compose.material.icons.Icons.Default.DirectionsBus
        "shopping" -> androidx.compose.material.icons.Icons.Default.ShoppingCart
        "entertainment" -> androidx.compose.material.icons.Icons.Default.Movie
        "health" -> androidx.compose.material.icons.Icons.Default.LocalHospital
        "education" -> androidx.compose.material.icons.Icons.Default.School
        "salary" -> androidx.compose.material.icons.Icons.Default.AttachMoney
        "investment" -> androidx.compose.material.icons.Icons.Default.TrendingUp
        "gift" -> androidx.compose.material.icons.Icons.Default.CardGiftcard
        "other" -> androidx.compose.material.icons.Icons.Default.Category
        else -> androidx.compose.material.icons.Icons.Default.Category
    }
}