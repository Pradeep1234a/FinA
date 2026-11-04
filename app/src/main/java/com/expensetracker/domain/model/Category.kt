package com.expensetracker.domain.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: Int,
    val type: CategoryType = CategoryType.EXPENSE,
    val isSystem: Boolean = false,
    val sortOrder: Int = 0
) {
    val colorValue: Color
        get() = Color(color)

    val isCustom: Boolean
        get() = !isSystem

    companion object {
        fun copy(category: Category, name: String? = null, icon: String? = null, color: Int? = null, type: CategoryType? = null, sortOrder: Int? = null): Category {
            return category.copy(
                name = name ?: category.name,
                icon = icon ?: category.icon,
                color = color ?: category.color,
                type = type ?: category.type,
                sortOrder = sortOrder ?: category.sortOrder
            )
        }
    }
}