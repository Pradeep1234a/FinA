package com.expensetracker.data.mapper

import com.expensetracker.data.local.entity.CategoryEntity
import com.expensetracker.domain.model.Category
import com.expensetracker.domain.model.CategoryType

object CategoryMapper {

    fun toDomain(entity: CategoryEntity): Category {
        return Category(
            id = entity.id,
            name = entity.name,
            icon = entity.icon,
            color = entity.color,
            type = entity.type,
            isSystem = entity.isSystem,
            sortOrder = entity.sortOrder
        )
    }

    fun toEntity(domain: Category): CategoryEntity {
        return CategoryEntity(
            id = domain.id,
            name = domain.name,
            icon = domain.icon,
            color = domain.color,
            type = domain.type,
            isSystem = domain.isSystem,
            sortOrder = domain.sortOrder
        )
    }

    fun toEntityForInsert(domain: Category): CategoryEntity {
        return CategoryEntity(
            id = 0,
            name = domain.name,
            icon = domain.icon,
            color = domain.color,
            type = domain.type,
            isSystem = domain.isSystem,
            sortOrder = domain.sortOrder
        )
    }
}