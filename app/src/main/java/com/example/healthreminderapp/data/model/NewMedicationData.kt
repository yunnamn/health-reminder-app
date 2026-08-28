package com.example.healthreminderapp.data.model

import com.example.healthreminderapp.data.local.entity.MedicationType
import com.example.healthreminderapp.data.local.entity.ScheduleType

enum class WeekDay(val shortLabel: String) {
    MONDAY("Пн"),
    TUESDAY("Вт"),
    WEDNESDAY("Ср"),
    THURSDAY("Чт"),
    FRIDAY("Пт"),
    SATURDAY("Сб"),
    SUNDAY("Вс")
}

data class MedicationTime(
    val hour: Int,
    val minute: Int
)

data class NewMedicationData(
    val name: String,
    val type: MedicationType,
    val strengthAmount: Double?,
    val strengthUnit: String?,
    val scheduleType: ScheduleType,
    val times: List<MedicationTime>,
    val selectedDays: Set<WeekDay>,
    val durationDays: Int?
)