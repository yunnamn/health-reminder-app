package com.example.healthreminderapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "type")
    val type: MedicationType,

    @ColumnInfo(name = "strength_amount")
    val strengthAmount: Double?,

    @ColumnInfo(name = "strength_unit")
    val strengthUnit: String?,

    @ColumnInfo(name = "start_date_millis")
    val startDateMillis: Long?,

    @ColumnInfo(name = "end_date_millis")
    val endDateMillis: Long?,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)