package com.example.healthreminderapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "analysis_results",
    foreignKeys = [
        ForeignKey(
            entity = AnalysisEntity::class,
            parentColumns = ["id"],
            childColumns = ["analysis_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["analysis_id"])]
)
data class AnalysisResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "analysis_id")
    val analysisId: Long,

    @ColumnInfo(name = "value")
    val value: Double,

    @ColumnInfo(name = "unit")
    val unit: String,

    @ColumnInfo(name = "result_date_millis")
    val resultDateMillis: Long,

    @ColumnInfo(name = "note")
    val note: String? = null
)