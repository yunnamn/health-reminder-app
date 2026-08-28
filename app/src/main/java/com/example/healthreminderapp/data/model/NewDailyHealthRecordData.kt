package com.example.healthreminderapp.data.model

data class NewDailyHealthRecordData(
    val recordedAtMillis: Long,
    val systolicPressure: Int?,
    val diastolicPressure: Int?,
    val pulse: Int?,
    val weightKg: Double?,
    val temperatureC: Double?,
    val glucoseMmolL: Double?
)