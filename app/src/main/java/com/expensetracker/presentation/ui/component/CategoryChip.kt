package com.expensetracker.presentation.ui.component

import androidx.compose.material3.Chip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.domain.model.Category

@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Chip(
        modifier = modifier,
        onClick = onClick,
        selected = isSelected,
        colors = androidx.compose.material3.ChipDefaults.colors(
            selectedContainerColor = Color(0xFF1B5E20),
            selectedLabelColor = Color.White,
            containerColor = Color(0xFFEEEEEE),
            labelColor = Color(0xFF1C1C1C)
        ),
        shape = androidx.compose.material3.Shapes.small
    ) {
        Text(text = category.name, fontSize = 14.sp, color = if (isSelected) Color.White else Color(0xFF1C1C1C))
    }
}

@Composable
fun CategoryFilterChips(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategoryClick: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        CategoryChip(
            category = Category(id = 0, name = "All", icon = "all", color = 0xFF757575, type = com.expensetracker.domain.model.CategoryType.Expense, isDefault = true),
            isSelected = selectedCategoryId == null,
            onClick = { onCategoryClick(null) }
        )
        categories.forEach { category ->
            CategoryChip(category = category, isSelected = selectedCategoryId == category.id, onClick = { onCategoryClick(category.id) })
        }
    }
}