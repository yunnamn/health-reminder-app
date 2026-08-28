package com.example.healthreminderapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.healthreminderapp.data.local.entity.MedicationEntity
import com.example.healthreminderapp.data.local.relation.MedicationWithSchedules
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)

    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Transaction
    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllMedicationsWithSchedules(): Flow<List<MedicationWithSchedules>>

    @Transaction
    @Query("SELECT * FROM medications ORDER BY name ASC")
    suspend fun getAllMedicationsWithSchedulesOnce(): List<MedicationWithSchedules>

    @Query("SELECT * FROM medications WHERE id = :medicationId")
    suspend fun getMedicationById(medicationId: Long): MedicationEntity?
}