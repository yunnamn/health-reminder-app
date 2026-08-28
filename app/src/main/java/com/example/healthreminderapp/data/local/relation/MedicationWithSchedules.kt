package com.example.healthreminderapp.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.healthreminderapp.data.local.entity.MedicationEntity
import com.example.healthreminderapp.data.local.entity.MedicationScheduleEntity

data class MedicationWithSchedules(
    @Embedded
    val medication: MedicationEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "medication_id"
    )
    val schedules: List<MedicationScheduleEntity>
)