package com.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expensetracker.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: SettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(settings: List<SettingsEntity>)

    @Update
    suspend fun update(setting: SettingsEntity)

    @Query("SELECT * FROM settings WHERE key = :key")
    suspend fun getByKey(key: String): SettingsEntity?

    @Query("SELECT * FROM settings")
    fun getAll(): Flow<List<SettingsEntity>>

    @Query("SELECT * FROM settings WHERE key IN (:keys)")
    suspend fun getByKeys(keys: List<String>): List<SettingsEntity>

    @Query("DELETE FROM settings WHERE key = :key")
    suspend fun deleteByKey(key: String)
}