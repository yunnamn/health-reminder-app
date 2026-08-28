package com.example.healthreminderapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medication_schedules",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medication_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medication_id"])]
)
data class MedicationScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "medication_id")
    val medicationId: Long,

    @ColumnInfo(name = "schedule_type")
    val scheduleType: ScheduleType,

    @ColumnInfo(name = "time_hour")
    val timeHour: Int,

    @ColumnInfo(name = "time_minute")
    val timeMinute: Int,

    @ColumnInfo(name = "monday")
    val monday: Boolean = false,

    @ColumnInfo(name = "tuesday")
    val tuesday: Boolean = false,

    @ColumnInfo(name = "wednesday")
    val wednesday: Boolean = false,

    @ColumnInfo(name = "thursday")
    val thursday: Boolean = false,

    @ColumnInfo(name = "friday")
    val friday: Boolean = false,

    @ColumnInfo(name = "saturday")
    val saturday: Boolean = false,

    @ColumnInfo(name = "sunday")
    val sunday: Boolean = false
)