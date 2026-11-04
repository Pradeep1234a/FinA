package com.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expensetracker.data.local.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rt: RecurringTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rts: List<RecurringTransactionEntity>): List<Long>

    @Update
    suspend fun update(rt: RecurringTransactionEntity)

    @Delete
    suspend fun delete(rt: RecurringTransactionEntity)

    @Query("DELETE FROM recurring_transactions WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getById(id: Long): RecurringTransactionEntity?

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 ORDER BY nextDueDate ASC")
    fun getActive(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions ORDER BY nextDueDate ASC")
    fun getAll(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 AND nextDueDate <= :now ORDER BY nextDueDate ASC")
    suspend fun getDue(now: Long): List<RecurringTransactionEntity>

    @Query("SELECT * FROM recurring_transactions WHERE categoryId = :categoryId")
    suspend fun getByCategory(categoryId: Long): List<RecurringTransactionEntity>

    @Query("UPDATE recurring_transactions SET lastGeneratedDate = :generatedDate, nextDueDate = :nextDueDate WHERE id = :id")
    suspend fun updateGeneratedDate(id: Long, generatedDate: Long, nextDueDate: Long)

    @Query("UPDATE recurring_transactions SET isActive = :isActive WHERE id = :id")
    suspend fun setActive(id: Long, isActive: Boolean)
}