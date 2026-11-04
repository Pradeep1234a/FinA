package com.expensetracker.data.local.converter

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.YearMonth
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class DateConverter {

    @TypeConverter
    fun fromLocalDateTime(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofInstant(it, TimeZone.currentSystemDefault()) }
    }

    @TypeConverter
    fun toLocalDateTime(value: LocalDateTime?): Long? {
        return value?.toInstant(TimeZone.currentSystemDefault())?.epochMilliseconds
    }

    @TypeConverter
    fun fromLocalDate(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofInstant(it, TimeZone.currentSystemDefault()) }
    }

    @TypeConverter
    fun toLocalDate(value: LocalDate?): Long? {
        return value?.atStartOfDay(TimeZone.currentSystemDefault())?.toInstant()?.epochMilliseconds
    }

    @TypeConverter
    fun fromYearMonth(value: String?): YearMonth? {
        return value?.let { YearMonth.parse(it) }
    }

    @TypeConverter
    fun toYearMonth(value: YearMonth?): String? {
        return value?.toString()
    }
}