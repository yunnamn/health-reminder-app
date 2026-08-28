package com.example.healthreminderapp.data.model

data class AnalysisCardData(
    val id: Long,
    val name: String,
    val latestValueText: String,
    val latestDateMillis: Long?,
    val helperText: String = "Нажмите, чтобы открыть"
)