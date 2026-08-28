package com.example.healthreminderapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.healthreminderapp.data.local.entity.MedicationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MedicationLogEntity)

    @Query(
        """
        DELETE FROM medication_logs
        WHERE medication_id = :medicationId
        AND scheduled_date_millis = :scheduledDateMillis
        AND time_hour = :timeHour
        AND time_minute = :timeMinute
        """
    )
    suspend fun deleteLog(
        medicationId: Long,
        scheduledDateMillis: Long,
        timeHour: Int,
        timeMinute: Int
    )

    @Query("SELECT * FROM medication_logs WHERE scheduled_date_millis = :scheduledDateMillis")
    fun getLogsForDate(scheduledDateMillis: Long): Flow<List<MedicationLogEntity>>

    @Query("SELECT * FROM medication_logs WHERE scheduled_date_millis = :scheduledDateMillis")
    suspend fun getLogsForDateOnce(scheduledDateMillis: Long): List<MedicationLogEntity>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM medication_logs
            WHERE medication_id = :medicationId
            AND scheduled_date_millis = :scheduledDateMillis
            AND time_hour = :timeHour
            AND time_minute = :timeMinute
        )
        """
    )
    suspend fun hasLog(
        medicationId: Long,
        scheduledDateMillis: Long,
        timeHour: Int,
        timeMinute: Int
    ): Boolean
}