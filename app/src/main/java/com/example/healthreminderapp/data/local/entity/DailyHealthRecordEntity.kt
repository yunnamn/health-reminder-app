package com.example.healthreminderapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_health_records")
data class DailyHealthRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "recorded_at_millis")
    val recordedAtMillis: Long,

    @ColumnInfo(name = "systolic_pressure")
    val systolicPressure: Int?,

    @ColumnInfo(name = "diastolic_pressure")
    val diastolicPressure: Int?,

    @ColumnInfo(name = "pulse")
    val pulse: Int?,

    @ColumnInfo(name = "weight_kg")
    val weightKg: Double?,

    @ColumnInfo(name = "temperature_c")
    val temperatureC: Double?,

    @ColumnInfo(name = "glucose_mmol_l")
    val glucoseMmolL: Double?
)