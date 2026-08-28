package com.example.healthreminderapp.screens.schedule

import com.example.healthreminderapp.data.model.ScheduledDose
import com.example.healthreminderapp.util.startOfDay

data class ScheduleUiState(
    val selectedDateMillis: Long = startOfDay(System.currentTimeMillis()),
    val doses: List<ScheduledDose> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)