package com.expensetracker.util

import com.expensetracker.domain.model.Budget
import com.expensetracker.domain.model.Category
import com.expensetracker.domain.model.RecurringTransaction
import com.expensetracker.domain.model.Transaction
import kotlinx.datetime.LocalDate
import java.io.File

object ExportUtils {

    fun exportTransactionsToCSV(transactions: List<Transaction>, file: File): Result<File> {
        return try {
            file.outputStream().use { out ->
                out.write("Date,Type,Category,Amount,Note,Recurring\n".toByteArray())
                for (t in transactions) {
                    val line = "${formatDate(t.date.toLocalDate())},${t.type},${t.category.name},${t.amount},${escapeCsv(t.note.orEmpty())},${t.isRecurring}\n"
                    out.write(line.toByteArray())
                }
            }
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportCategoriesToCSV(categories: List<Category>, file: File): Result<File> {
        return try {
            file.outputStream().use { out ->
                out.write("Name,Icon,Color,Type,System,SortOrder\n".toByteArray())
                for (c in categories) {
                    val line = "${escapeCsv(c.name)},${c.icon},${c.color},${c.type},${c.isSystem},${c.sortOrder}\n"
                    out.write(line.toByteArray())
                }
            }
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportBudgetsToCSV(budgets: List<Budget>, file: File): Result<File> {
        return try {
            file.outputStream().use { out ->
                out.write("Category,Month,Amount,Threshold,Spent\n".toByteArray())
                for (b in budgets) {
                    val line = "${escapeCsv(b.category.name)},${b.monthYear},${b.amount},${b.alertThreshold},${b.spent}\n"
                    out.write(line.toByteArray())
                }
            }
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportRecurringToCSV(recurring: List<RecurringTransaction>, file: File): Result<File> {
        return try {
            file.outputStream().use { out ->
                out.write("Amount,Type,Category,Note,Frequency,Interval,StartDate,EndDate,NextDue,Active\n".toByteArray())
                for (r in recurring) {
                    val line = "${r.amount},${r.type},${escapeCsv(r.category.name)},${escapeCsv(r.note.orEmpty())},${r.recurrenceType},${r.recurrenceInterval},${r.startDate},${r.endDate?.toString() ?: ""},${r.nextDueDate},${r.isActive}\n"
                    out.write(line.toByteArray())
                }
            }
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatDate(date: LocalDate): String {
        return date.toString()
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }

    // CSV Parsing functions for import
    fun parseTransactionsCsv(file: File, categories: List<Category>): Result<List<Transaction>> {
        return try {
            val categoryMap = categories.associateBy { it.name }
            val lines = file.readLines()
            if (lines.isEmpty()) return Result.success(emptyList())

            val transactions = mutableListOf<Transaction>()
            for (i in 1 until lines.size) { // Skip header
                val line = lines[i].trim()
                if (line.isEmpty()) continue

                val parts = parseCsvLine(line)
                if (parts.size < 6) continue

                val date = LocalDate.parse(parts[0]).atStartOfDay()
                val type = TransactionType.valueOf(parts[1])
                val categoryName = parts[2]
                val amount = parts[3].toDouble()
                val note = if (parts[4].isNotEmpty()) parts[4] else null
                val isRecurring = parts[5].toBoolean()

                val category = categoryMap[categoryName] ?: continue
                transactions.add(Transaction(
                    amount = amount,
                    type = type,
                    category = category,
                    date = date,
                    note = note,
                    isRecurring = isRecurring
                ))
            }
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseCategoriesCsv(file: File): Result<List<Category>> {
        return try {
            val lines = file.readLines()
            if (lines.isEmpty()) return Result.success(emptyList())

            val categories = mutableListOf<Category>()
            for (i in 1 until lines.size) { // Skip header
                val line = lines[i].trim()
                if (line.isEmpty()) continue

                val parts = parseCsvLine(line)
                if (parts.size < 6) continue

                val name = parts[0]
                val icon = parts[1]
                val color = parts[2].toInt()
                val type = CategoryType.valueOf(parts[3])
                val isSystem = parts[4].toBoolean()
                val sortOrder = parts[5].toInt()

                categories.add(Category(
                    name = name,
                    icon = icon,
                    color = color,
                    type = type,
                    isSystem = isSystem,
                    sortOrder = sortOrder
                ))
            }
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseBudgetsCsv(file: File, categories: List<Category>): Result<List<Budget>> {
        return try {
            val categoryMap = categories.associateBy { it.name }
            val lines = file.readLines()
            if (lines.isEmpty()) return Result.success(emptyList())

            val budgets = mutableListOf<Budget>()
            for (i in 1 until lines.size) { // Skip header
                val line = lines[i].trim()
                if (line.isEmpty()) continue

                val parts = parseCsvLine(line)
                if (parts.size < 5) continue

                val categoryName = parts[0]
                val monthYear = kotlinx.datetime.YearMonth.parse(parts[1])
                val amount = parts[2].toDouble()
                val alertThreshold = parts[3].toDouble()
                val spent = parts[4].toDouble()

                val category = categoryMap[categoryName] ?: continue
                budgets.add(Budget(
                    category = category,
                    amount = amount,
                    monthYear = monthYear,
                    alertThreshold = alertThreshold,
                    spent = spent
                ))
            }
            Result.success(budgets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseRecurringCsv(file: File, categories: List<Category>): Result<List<RecurringTransaction>> {
        return try {
            val categoryMap = categories.associateBy { it.name }
            val lines = file.readLines()
            if (lines.isEmpty()) return Result.success(emptyList())

            val recurring = mutableListOf<RecurringTransaction>()
            for (i in 1 until lines.size) { // Skip header
                val line = lines[i].trim()
                if (line.isEmpty()) continue

                val parts = parseCsvLine(line)
                if (parts.size < 10) continue

                val amount = parts[0].toDouble()
                val type = TransactionType.valueOf(parts[1])
                val categoryName = parts[2]
                val note = if (parts[3].isNotEmpty()) parts[3] else null
                val recurrenceType = RecurrenceType.valueOf(parts[4])
                val recurrenceInterval = parts[5].toInt()
                val startDate = LocalDate.parse(parts[6])
                val endDate = if (parts[7].isNotEmpty()) LocalDate.parse(parts[7]) else null
                val nextDueDate = LocalDate.parse(parts[8])
                val isActive = parts[9].toBoolean()

                val category = categoryMap[categoryName] ?: continue
                recurring.add(RecurringTransaction(
                    amount = amount,
                    type = type,
                    category = category,
                    note = note,
                    recurrenceType = recurrenceType,
                    recurrenceInterval = recurrenceInterval,
                    startDate = startDate,
                    endDate = endDate,
                    nextDueDate = nextDueDate,
                    isActive = isActive
                ))
            }
            Result.success(recurring)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val char = line[i]
            when {
                char == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i += 2
                }
                char == '"' -> {
                    inQuotes = !inQuotes
                    i++
                }
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                    i++
                }
                else -> {
                    current.append(char)
                    i++
                }
            }
        }
        result.add(current.toString())
        return result
    }
}