package com.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expensetracker.data.local.entity.TransactionEntity
import com.expensetracker.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.YearMonth

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByTypeAndDateRange(type: TransactionType, startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByCategoryAndDateRange(categoryId: Long, startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE (note LIKE :query OR note IS NULL) AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun searchTransactions(query: String, startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalAmountByType(type: TransactionType, startDate: Long, endDate: Long): Double?

    @Query("SELECT categoryId, SUM(amount) FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate GROUP BY categoryId")
    suspend fun getAmountByCategory(type: TransactionType, startDate: Long, endDate: Long): List<Pair<Long, Double>>

    @Query("SELECT * FROM transactions WHERE isRecurring = 1 AND recurringTransactionId = :recurringTransactionId")
    suspend fun getByRecurringTransactionId(recurringTransactionId: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE date >= :startOfMonth AND date <= :endOfMonth ORDER BY date DESC")
    fun getByMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<TransactionEntity>>

    @Query("SELECT strftime('%Y-%m', date/1000, 'unixepoch') as monthYear, type, SUM(amount) as total FROM transactions GROUP BY monthYear, type ORDER BY monthYear DESC")
    suspend fun getMonthlyTotals(): List<MonthlyTotal>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getAllByDateRange(startDate: Long, endDate: Long): List<TransactionEntity>

    data class MonthlyTotal(
        val monthYear: String,
        val type: TransactionType,
        val total: Double
    )
}