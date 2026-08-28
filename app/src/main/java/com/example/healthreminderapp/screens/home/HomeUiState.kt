package com.example.healthreminderapp.screens.home

import com.example.healthreminderapp.data.model.AnalysisCardData
import com.example.healthreminderapp.data.model.ScheduledDose

data class HomeUiState(
    val todayDateMillis: Long = System.currentTimeMillis(),
    val isLoading: Boolean = true,
    val todayDoses: List<HomeDoseItem> = emptyList(),
    val takenCount: Int = 0,
    val totalCount: Int = 0,
    val latestIndicators: List<HomeIndicatorItem> = emptyList(),
    val latestAnalyses: List<AnalysisCardData> = emptyList(),
    val infoMessage: String? = null,
    val errorMessage: String? = null
)

data class HomeDoseItem(
    val dose: ScheduledDose,
    val stateText: String,
    val isTaken: Boolean,
    val isDue: Boolean,
    val isSnoozed: Boolean,
    val canMarkTaken: Boolean,
    val canSnooze: Boolean
)

data class HomeIndicatorItem(
    val title: String,
    val value: String
)