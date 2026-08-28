package com.example.healthreminderapp.data.model

data class NewAnalysisResultData(
    val analysisId: Long,
    val value: Double,
    val resultDateMillis: Long,
    val note: String?
)