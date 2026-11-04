package com.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expensetracker.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetEntity>): List<Long>

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getById(id: Long): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear ORDER BY categoryId")
    fun getByMonth(monthYear: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND monthYear = :monthYear")
    suspend fun getByCategoryAndMonth(categoryId: Long, monthYear: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId ORDER BY monthYear DESC")
    suspend fun getByCategory(categoryId: Long): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    suspend fun getAllByMonth(monthYear: String): List<BudgetEntity>

    @Query("SELECT * FROM budgets ORDER BY monthYear DESC, categoryId")
    suspend fun getAll(): List<BudgetEntity>
}