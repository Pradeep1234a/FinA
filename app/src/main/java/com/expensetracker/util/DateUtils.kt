package com.expensetracker.util

import android.content.Context
import kotlinx.datetime.*
import kotlinx.datetime.format.DateTimeFormatter
import kotlinx.datetime.format.DateTimeFormatterBuilder
import kotlinx.datetime.format.date
import kotlinx.datetime.format.time
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for date formatting, parsing, and range calculations.
 */
object DateUtils {

    // =========================================================================
    // Formatters
    // =========================================================================

    private val SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    private val LONG_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
    private val MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy")
    private val YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM")
    private val SHORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
    private val ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
    private val ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // =========================================================================
    // Current Date/Time
    // =========================================================================

    fun today(): LocalDate {
        return LocalDate.now(TimeZone.currentSystemDefault())
    }

    fun now(): LocalDateTime {
        return LocalDateTime.now(TimeZone.currentSystemDefault())
    }

    fun currentYearMonth(): YearMonth {
        return YearMonth.now(TimeZone.currentSystemDefault())
    }

    // =========================================================================
    // Formatting
    // =========================================================================

    fun formatShortDate(date: LocalDate): String {
        return date.format(SHORT_DATE_FORMATTER)
    }

    fun formatLongDate(date: LocalDate): String {
        return date.format(LONG_DATE_FORMATTER)
    }

    fun formatMonthYear(date: LocalDate): String {
        return date.format(MONTH_YEAR_FORMATTER)
    }

    fun formatMonthYear(yearMonth: YearMonth): String {
        return yearMonth.format(MONTH_YEAR_FORMATTER)
    }

    fun formatYearMonth(yearMonth: YearMonth): String {
        return yearMonth.format(YEAR_MONTH_FORMATTER)
    }

    fun formatShortTime(dateTime: LocalDateTime): String {
        return dateTime.format(SHORT_TIME_FORMATTER)
    }

    fun formatDateTime(dateTime: LocalDateTime): String {
        return "${formatShortDate(dateTime.toLocalDate())} ${formatShortTime(dateTime)}"
    }

    fun formatIsoDate(date: LocalDate): String {
        return date.format(ISO_DATE_FORMATTER)
    }

    fun formatIsoDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(ISO_DATE_TIME_FORMATTER)
    }

    // =========================================================================
    // Parsing
    // =========================================================================

    fun parseIsoDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, ISO_DATE_FORMATTER)
        } catch (e: Exception) {
            null
        }
    }

    fun parseIsoDateTime(dateTimeString: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(dateTimeString, ISO_DATE_TIME_FORMATTER)
        } catch (e: Exception) {
            null
        }
    }

    fun parseYearMonth(yearMonthString: String): YearMonth? {
        return try {
            YearMonth.parse(yearMonthString, YEAR_MONTH_FORMATTER)
        } catch (e: Exception) {
            null
        }
    }

    // =========================================================================
    // Date Range Calculations
    // =========================================================================

    fun startOfDay(date: LocalDate): LocalDateTime {
        return date.atTime(0, 0, 0)
    }

    fun endOfDay(date: LocalDate): LocalDateTime {
        return date.atTime(23, 59, 59, 999_000_000)
    }

    fun startOfWeek(date: LocalDate, firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY): LocalDate {
        val daysToSubtract = (date.dayOfWeek.value - firstDayOfWeek.value + 7) % 7
        return date.minusDays(daysToSubtract.toLong())
    }

    fun endOfWeek(date: LocalDate, firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY): LocalDate {
        return startOfWeek(date, firstDayOfWeek).plusDays(6)
    }

    fun startOfMonth(date: LocalDate): LocalDate {
        return date.withDayOfMonth(1)
    }

    fun endOfMonth(date: LocalDate): LocalDate {
        return date.withDayOfMonth(date.month.length(date.year))
    }

    fun startOfYear(date: LocalDate): LocalDate {
        return date.withMonth(1).withDayOfMonth(1)
    }

    fun endOfYear(date: LocalDate): LocalDate {
        return date.withMonth(12).withDayOfMonth(31)
    }

    // =========================================================================
    // Relative Date Ranges
    // =========================================================================

    fun todayRange(): Pair<LocalDateTime, LocalDateTime> {
        val today = today()
        return startOfDay(today) to endOfDay(today)
    }

    fun yesterdayRange(): Pair<LocalDateTime, LocalDateTime> {
        val yesterday = today().minusDays(1)
        return startOfDay(yesterday) to endOfDay(yesterday)
    }

    fun thisWeekRange(firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY): Pair<LocalDateTime, LocalDateTime> {
        val today = today()
        val start = startOfWeek(today, firstDayOfWeek)
        val end = endOfWeek(today, firstDayOfWeek)
        return startOfDay(start) to endOfDay(end)
    }

    fun thisMonthRange(): Pair<LocalDateTime, LocalDateTime> {
        val today = today()
        val start = startOfMonth(today)
        val end = endOfMonth(today)
        return startOfDay(start) to endOfDay(end)
    }

    fun thisYearRange(): Pair<LocalDateTime, LocalDateTime> {
        val today = today()
        val start = startOfYear(today)
        val end = endOfYear(today)
        return startOfDay(start) to endOfDay(end)
    }

    fun lastMonthRange(): Pair<LocalDateTime, LocalDateTime> {
        val today = today()
        val lastMonth = today.minusMonths(1)
        val start = startOfMonth(lastMonth)
        val end = endOfMonth(lastMonth)
        return startOfDay(start) to endOfDay(end)
    }

    fun lastYearRange(): Pair<LocalDateTime, LocalDateTime> {
        val today = today()
        val lastYear = today.minusYears(1)
        val start = startOfYear(lastYear)
        val end = endOfYear(lastYear)
        return startOfDay(start) to endOfDay(end)
    }

    fun lastDaysRange(days: Int): Pair<LocalDateTime, LocalDateTime> {
        val end = today()
        val start = end.minusDays((days - 1).toLong())
        return startOfDay(start) to endOfDay(end)
    }

    fun monthRange(year: Int, month: Int): Pair<LocalDateTime, LocalDateTime> {
        val start = LocalDate(year, month, 1)
        val end = start.withDayOfMonth(start.month.length(start.year))
        return startOfDay(start) to endOfDay(end)
    }

    // =========================================================================
    // Comparison & Checking
    // =========================================================================

    fun isToday(date: LocalDate): Boolean {
        return date == today()
    }

    fun isYesterday(date: LocalDate): Boolean {
        return date == today().minusDays(1)
    }

    fun isThisWeek(date: LocalDate, firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY): Boolean {
        val start = startOfWeek(today(), firstDayOfWeek)
        val end = endOfWeek(today(), firstDayOfWeek)
        return date >= start && date <= end
    }

    fun isThisMonth(date: LocalDate): Boolean {
        val today = today()
        return date.year == today.year && date.month == today.month
    }

    fun isThisYear(date: LocalDate): Boolean {
        return date.year == today().year
    }

    fun isInRange(dateTime: LocalDateTime, start: LocalDateTime, end: LocalDateTime): Boolean {
        return dateTime >= start && dateTime <= end
    }

    fun isInRange(date: LocalDate, start: LocalDate, end: LocalDate): Boolean {
        return date >= start && date <= end
    }

    // =========================================================================
    // Relative Time Strings
    // =========================================================================

    fun getRelativeDateString(context: Context, date: LocalDate): String {
        return when {
            isToday(date) -> context.getString(com.expensetracker.R.string.transactions_today)
            isYesterday(date) -> context.getString(com.expensetracker.R.string.transactions_yesterday)
            isThisWeek(date) -> context.getString(com.expensetracker.R.string.transactions_this_week)
            isThisMonth(date) -> context.getString(com.expensetracker.R.string.transactions_this_month)
            else -> formatShortDate(date)
        }
    }

    fun getRelativeDateTimeString(context: Context, dateTime: LocalDateTime): String {
        val date = dateTime.toLocalDate()
        val relativeDate = getRelativeDateString(context, date)
        val time = formatShortTime(dateTime)
        return "$relativeDate at $time"
    }

    // =========================================================================
    // Date Difference Calculations
    // =========================================================================

    fun daysBetween(start: LocalDate, end: LocalDate): Long {
        return start.periodUntil(end, DateTimePeriod.DAYS).days
    }

    fun monthsBetween(start: LocalDate, end: LocalDate): Long {
        return start.periodUntil(end, DateTimePeriod.MONTHS).months
    }

    fun yearsBetween(start: LocalDate, end: LocalDate): Long {
        return start.periodUntil(end, DateTimePeriod.YEARS).years
    }

    // =========================================================================
    // Recurring Transaction Helpers
    // =========================================================================

    fun calculateNextDueDate(
        currentDueDate: LocalDate,
        recurrenceType: String,
        recurrenceInterval: Int
    ): LocalDate {
        return when (recurrenceType) {
            "DAILY" -> currentDueDate.plusDays(recurrenceInterval.toLong())
            "WEEKLY" -> currentDueDate.plusWeeks(recurrenceInterval.toLong())
            "MONTHLY" -> currentDueDate.plusMonths(recurrenceInterval.toLong())
            "YEARLY" -> currentDueDate.plusYears(recurrenceInterval.toLong())
            else -> currentDueDate.plusMonths(1)
        }
    }

    fun isRecurringDue(nextDueDate: LocalDate): Boolean {
        return nextDueDate <= today()
    }

    // =========================================================================
    // Array/List Helpers
    // =========================================================================

    fun generateDateRange(start: LocalDate, end: LocalDate): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var current = start
        while (current <= end) {
            dates.add(current)
            current = current.plusDays(1)
        }
        return dates
    }

    fun generateMonthRange(start: YearMonth, end: YearMonth): List<YearMonth> {
        val months = mutableListOf<YearMonth>()
        var current = start
        while (current <= end) {
            months.add(current)
            current = current.plusMonths(1)
        }
        return months
    }

    fun generateWeekRange(start: LocalDate, end: LocalDate, firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY): List<LocalDate> {
        val weeks = mutableListOf<LocalDate>()
        var current = startOfWeek(start, firstDayOfWeek)
        val endWeek = startOfWeek(end, firstDayOfWeek)
        while (current <= endWeek) {
            weeks.add(current)
            current = current.plusWeeks(1)
        }
        return weeks
    }
}