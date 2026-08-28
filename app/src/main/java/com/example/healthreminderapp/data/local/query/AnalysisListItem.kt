package com.example.healthreminderapp.data.local.query

import androidx.room.ColumnInfo

data class AnalysisListItem(
    val id: Long,
    val name: String,

    @ColumnInfo(name = "default_unit")
    val defaultUnit: String,

    @ColumnInfo(name = "latest_value")
    val latestValue: Double?,

    @ColumnInfo(name = "latest_unit")
    val latestUnit: String?,

    @ColumnInfo(name = "latest_date_millis")
    val latestDateMillis: Long?
)