package com.expensetracker.data.local.converter

import androidx.room.TypeConverter
import com.expensetracker.domain.model.CategoryType
import com.expensetracker.domain.model.RecurrenceType
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.domain.model.ThemeMode

class TypeConverters {

    @TypeConverter
    fun fromTransactionType(value: Int): TransactionType {
        return TransactionType.values()[value]
    }

    @TypeConverter
    fun toTransactionType(value: TransactionType): Int {
        return value.ordinal
    }

    @TypeConverter
    fun fromCategoryType(value: Int): CategoryType {
        return CategoryType.values()[value]
    }

    @TypeConverter
    fun toCategoryType(value: CategoryType): Int {
        return value.ordinal
    }

    @TypeConverter
    fun fromRecurrenceType(value: Int): RecurrenceType {
        return RecurrenceType.values()[value]
    }

    @TypeConverter
    fun toRecurrenceType(value: RecurrenceType): Int {
        return value.ordinal
    }

    @TypeConverter
    fun fromThemeMode(value: Int): ThemeMode {
        return ThemeMode.values()[value]
    }

    @TypeConverter
    fun toThemeMode(value: ThemeMode): Int {
        return value.ordinal
    }
}