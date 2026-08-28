package com.example.healthreminderapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.healthreminderapp.data.local.entity.DailyHealthRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyHealthRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DailyHealthRecordEntity): Long

    @Query("SELECT * FROM daily_health_records ORDER BY recorded_at_millis DESC")
    fun observeAllRecords(): Flow<List<DailyHealthRecordEntity>>
}