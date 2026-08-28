package com.example.healthreminderapp.data.model

data class AnalysisDetailData(
    val id: Long,
    val name: String,
    val defaultUnit: String,
    val isArchived: Boolean,
    val results: List<AnalysisResultHistoryItem>
)

data class AnalysisResultHistoryItem(
    val id: Long,
    val valueText: String,
    val resultDateMillis: Long,
    val note: String?
)
