package com.example.healthreminderapp.screens.medications

import com.example.healthreminderapp.data.local.entity.MedicationEntity
import com.example.healthreminderapp.data.local.entity.MedicationType
import com.example.healthreminderapp.data.local.entity.ScheduleType
import com.example.healthreminderapp.data.local.relation.MedicationWithSchedules
import com.example.healthreminderapp.data.model.MedicationTime
import com.example.healthreminderapp.data.model.WeekDay

data class MedicationUiState(
    val medications: List<MedicationWithSchedules> = emptyList(),
    val isLoading: Boolean = true,

    val isAddingMedication: Boolean = false,
    val currentStep: Int = 1,

    val editingMedicationId: Long? = null,
    val originalMedicationEntity: MedicationEntity? = null,

    val medicationPendingDelete: MedicationEntity? = null,
    val showDeleteDialog: Boolean = false,

    val name: String = "",
    val type: MedicationType = MedicationType.TABLET,
    val strengthAmount: String = "",
    val strengthUnit: String = "мг",

    val scheduleType: ScheduleType = ScheduleType.DAILY,
    val selectedDays: Set<WeekDay> = emptySet(),

    val timeHourInput: String = "",
    val timeMinuteInput: String = "",
    val times: List<MedicationTime> = emptyList(),

    val durationDays: String = "",

    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)