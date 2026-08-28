package com.example.healthreminderapp.data.model

data class ScheduledDose(
    val medicationId: Long,
    val medicationName: String,
    val scheduledDateMillis: Long,
    val timeHour: Int,
    val timeMinute: Int,
    val isLogged: Boolean,
    val canEditLog: Boolean,
    val courseEndDateMillis: Long?,
    val daysRemaining: Int?
)