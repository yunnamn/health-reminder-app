package com.example.healthreminderapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medication_logs",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medication_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["medication_id"]),
        Index(
            value = ["medication_id", "scheduled_date_millis", "time_hour", "time_minute"],
            unique = true
        )
    ]
)
data class MedicationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "medication_id")
    val medicationId: Long,

    @ColumnInfo(name = "scheduled_date_millis")
    val scheduledDateMillis: Long,

    @ColumnInfo(name = "time_hour")
    val timeHour: Int,

    @ColumnInfo(name = "time_minute")
    val timeMinute: Int,

    @ColumnInfo(name = "logged_at_millis")
    val loggedAtMillis: Long = System.currentTimeMillis()
)