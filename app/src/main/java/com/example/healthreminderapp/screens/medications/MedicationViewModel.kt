package com.example.healthreminderapp.screens.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthreminderapp.HealthReminderApplication
import com.example.healthreminderapp.data.local.entity.MedicationEntity
import com.example.healthreminderapp.data.local.entity.MedicationType
import com.example.healthreminderapp.data.local.entity.ScheduleType
import com.example.healthreminderapp.data.local.relation.MedicationWithSchedules
import com.example.healthreminderapp.data.model.MedicationTime
import com.example.healthreminderapp.data.model.NewMedicationData
import com.example.healthreminderapp.data.model.WeekDay
import com.example.healthreminderapp.notifications.MedicationReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MedicationViewModel(
    private val application: HealthReminderApplication
) : ViewModel() {

    private val repository = application.medicationRepository

    private val _uiState = MutableStateFlow(MedicationUiState())
    val uiState: StateFlow<MedicationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllMedicationsWithSchedules().collect { medications ->
                _uiState.update {
                    it.copy(
                        medications = medications,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun startAddingMedication() {
        _uiState.update {
            it.copy(
                isAddingMedication = true,
                currentStep = 1,
                editingMedicationId = null,
                originalMedicationEntity = null,
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    fun startEditingMedication(item: MedicationWithSchedules) {
        val medication = item.medication
        val schedules = item.schedules.sortedWith(compareBy({ it.timeHour }, { it.timeMinute }))
        val firstSchedule = schedules.firstOrNull()

        val selectedDays = buildSet {
            firstSchedule?.let {
                if (it.monday) add(WeekDay.MONDAY)
                if (it.tuesday) add(WeekDay.TUESDAY)
                if (it.wednesday) add(WeekDay.WEDNESDAY)
                if (it.thursday) add(WeekDay.THURSDAY)
                if (it.friday) add(WeekDay.FRIDAY)
                if (it.saturday) add(WeekDay.SATURDAY)
                if (it.sunday) add(WeekDay.SUNDAY)
            }
        }

        val durationDays = if (
            medication.startDateMillis != null &&
            medication.endDateMillis != null &&
            medication.endDateMillis > medication.startDateMillis
        ) {
            val diff = medication.endDateMillis - medication.startDateMillis
            (diff / (24L * 60L * 60L * 1000L)).toString()
        } else {
            ""
        }

        _uiState.update {
            it.copy(
                isAddingMedication = true,
                currentStep = 1,
                editingMedicationId = medication.id,
                originalMedicationEntity = medication,
                name = medication.name,
                type = medication.type,
                strengthAmount = medication.strengthAmount?.let(::formatStrengthForInput).orEmpty(),
                strengthUnit = medication.strengthUnit.orEmpty(),
                scheduleType = firstSchedule?.scheduleType ?: ScheduleType.DAILY,
                selectedDays = selectedDays,
                times = schedules.map { schedule ->
                    MedicationTime(
                        hour = schedule.timeHour,
                        minute = schedule.timeMinute
                    )
                },
                durationDays = durationDays,
                timeHourInput = "",
                timeMinuteInput = "",
                isSaving = false,
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    fun requestDeleteMedication(medication: MedicationEntity) {
        _uiState.update {
            it.copy(
                medicationPendingDelete = medication,
                showDeleteDialog = true
            )
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update {
            it.copy(
                medicationPendingDelete = null,
                showDeleteDialog = false
            )
        }
    }

    fun confirmDeleteMedication() {
        val medicationToDelete = _uiState.value.medicationPendingDelete ?: return

        viewModelScope.launch {
            try {
                repository.deleteMedication(medicationToDelete)
                MedicationReminderScheduler.rescheduleAll(application)

                _uiState.update {
                    it.copy(
                        medicationPendingDelete = null,
                        showDeleteDialog = false,
                        infoMessage = "Лекарство удалено"
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        medicationPendingDelete = null,
                        showDeleteDialog = false,
                        errorMessage = "Не удалось удалить лекарство"
                    )
                }
            }
        }
    }

    fun cancelAddingMedication() {
        resetForm()
    }

    fun goToNextStep() {
        _uiState.update {
            it.copy(
                currentStep = (it.currentStep + 1).coerceAtMost(6),
                errorMessage = null
            )
        }
    }

    fun goToPreviousStep() {
        _uiState.update {
            it.copy(
                currentStep = (it.currentStep - 1).coerceAtLeast(1),
                errorMessage = null
            )
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateType(type: MedicationType) {
        _uiState.update { it.copy(type = type) }
    }

    fun updateStrengthAmount(value: String) {
        _uiState.update { it.copy(strengthAmount = value) }
    }

    fun updateStrengthUnit(value: String) {
        _uiState.update { it.copy(strengthUnit = value) }
    }

    fun updateScheduleType(type: ScheduleType) {
        _uiState.update {
            it.copy(
                scheduleType = type,
                selectedDays = if (type == ScheduleType.DAILY) emptySet() else it.selectedDays
            )
        }
    }

    fun toggleDay(day: WeekDay) {
        _uiState.update { state ->
            val newDays = state.selectedDays.toMutableSet()
            if (newDays.contains(day)) {
                newDays.remove(day)
            } else {
                newDays.add(day)
            }
            state.copy(selectedDays = newDays)
        }
    }

    fun updateTimeHourInput(value: String) {
        _uiState.update { it.copy(timeHourInput = value.filter(Char::isDigit).take(2)) }
    }

    fun updateTimeMinuteInput(value: String) {
        _uiState.update { it.copy(timeMinuteInput = value.filter(Char::isDigit).take(2)) }
    }

    fun addTime() {
        val state = _uiState.value
        val hour = state.timeHourInput.toIntOrNull()
        val minute = state.timeMinuteInput.toIntOrNull()

        if (hour == null || minute == null || hour !in 0..23 || minute !in 0..59) {
            _uiState.update { it.copy(errorMessage = "Введите корректное время") }
            return
        }

        val newTime = MedicationTime(hour = hour, minute = minute)

        if (state.times.contains(newTime)) {
            _uiState.update { it.copy(errorMessage = "Это время уже добавлено") }
            return
        }

        _uiState.update {
            it.copy(
                times = (it.times + newTime).sortedWith(compareBy({ time -> time.hour }, { time -> time.minute })),
                timeHourInput = "",
                timeMinuteInput = "",
                errorMessage = null
            )
        }
    }

    fun removeTime(time: MedicationTime) {
        _uiState.update {
            it.copy(times = it.times - time)
        }
    }

    fun updateDurationDays(value: String) {
        _uiState.update { it.copy(durationDays = value.filter(Char::isDigit)) }
    }

    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun saveMedication() {
        val state = _uiState.value

        val parsedStrength = state.strengthAmount
            .replace(",", ".")
            .toDoubleOrNull()

        val parsedDurationDays = state.durationDays.toIntOrNull()

        when {
            state.name.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Введите название лекарства") }
                return
            }

            state.strengthAmount.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Введите дозировку") }
                return
            }

            parsedStrength == null || parsedStrength <= 0 -> {
                _uiState.update { it.copy(errorMessage = "Дозировка должна быть больше нуля") }
                return
            }

            state.strengthUnit.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Введите единицу измерения") }
                return
            }

            state.times.isEmpty() -> {
                _uiState.update { it.copy(errorMessage = "Добавьте хотя бы одно время приёма") }
                return
            }

            state.scheduleType == ScheduleType.SPECIFIC_DAYS && state.selectedDays.isEmpty() -> {
                _uiState.update { it.copy(errorMessage = "Выберите хотя бы один день недели") }
                return
            }

            parsedDurationDays != null && parsedDurationDays <= 0 -> {
                _uiState.update { it.copy(errorMessage = "Длительность должна быть больше нуля") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                val medicationData = NewMedicationData(
                    name = state.name.trim(),
                    type = state.type,
                    strengthAmount = parsedStrength,
                    strengthUnit = state.strengthUnit.trim(),
                    scheduleType = state.scheduleType,
                    times = state.times,
                    selectedDays = state.selectedDays,
                    durationDays = parsedDurationDays
                )

                if (state.editingMedicationId != null && state.originalMedicationEntity != null) {
                    repository.updateMedication(
                        medicationId = state.editingMedicationId,
                        originalMedication = state.originalMedicationEntity,
                        updatedMedication = medicationData
                    )
                    MedicationReminderScheduler.rescheduleAll(application)

                    _uiState.update {
                        MedicationUiState(
                            medications = it.medications,
                            isLoading = false,
                            infoMessage = "Лекарство обновлено"
                        )
                    }
                } else {
                    repository.addMedication(medicationData)
                    MedicationReminderScheduler.rescheduleAll(application)

                    _uiState.update {
                        MedicationUiState(
                            medications = it.medications,
                            isLoading = false,
                            infoMessage = "Лекарство сохранено"
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Не удалось сохранить изменения"
                    )
                }
            }
        }
    }

    private fun resetForm() {
        _uiState.update {
            it.copy(
                isAddingMedication = false,
                currentStep = 1,
                editingMedicationId = null,
                originalMedicationEntity = null,
                name = "",
                type = MedicationType.TABLET,
                strengthAmount = "",
                strengthUnit = "мг",
                scheduleType = ScheduleType.DAILY,
                selectedDays = emptySet(),
                timeHourInput = "",
                timeMinuteInput = "",
                times = emptyList(),
                durationDays = "",
                isSaving = false,
                errorMessage = null
            )
        }
    }

    private fun formatStrengthForInput(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            value.toString()
        }
    }
}