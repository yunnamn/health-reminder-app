package com.example.healthreminderapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.healthreminderapp.data.local.entity.MedicationScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: MedicationScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<MedicationScheduleEntity>)

    @Query("SELECT * FROM medication_schedules WHERE medication_id = :medicationId ORDER BY time_hour ASC, time_minute ASC")
    fun getSchedulesForMedication(medicationId: Long): Flow<List<MedicationScheduleEntity>>

    @Query("DELETE FROM medication_schedules WHERE medication_id = :medicationId")
    suspend fun deleteSchedulesForMedication(medicationId: Long)
}