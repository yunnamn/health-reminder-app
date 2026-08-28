package com.example.healthreminderapp.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthreminderapp.HealthReminderApplication
import com.example.healthreminderapp.data.model.ScheduledDose
import com.example.healthreminderapp.notifications.MedicationReminderScheduler
import com.example.healthreminderapp.util.DAY_MILLIS
import com.example.healthreminderapp.util.startOfDay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val application: HealthReminderApplication
) : ViewModel() {

    private val repository = application.medicationRepository

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private var selectedDateJob: Job? = null

    init {
        observeSelectedDate(_uiState.value.selectedDateMillis)
    }

    fun goToPreviousDay() {
        val newDate = startOfDay(_uiState.value.selectedDateMillis - DAY_MILLIS)
        observeSelectedDate(newDate)
    }

    fun goToNextDay() {
        val newDate = startOfDay(_uiState.value.selectedDateMillis + DAY_MILLIS)
        observeSelectedDate(newDate)
    }

    fun toggleDoseLogged(dose: ScheduledDose) {
        if (!dose.canEditLog) return

        viewModelScope.launch {
            try {
                if (dose.isLogged) {
                    repository.unmarkDoseAsTaken(dose)
                    MedicationReminderScheduler.rescheduleAll(application)
                    _uiState.update { it.copy(infoMessage = "Отметка снята") }
                } else {
                    repository.markDoseAsTaken(dose)
                    MedicationReminderScheduler.cancelDoseReminders(application, dose)
                    _uiState.update { it.copy(infoMessage = "Приём отмечен") }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(errorMessage = "Не удалось изменить отметку") }
            }
        }
    }

    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun observeSelectedDate(selectedDateMillis: Long) {
        selectedDateJob?.cancel()

        _uiState.update {
            it.copy(
                selectedDateMillis = selectedDateMillis,
                isLoading = true,
                errorMessage = null
            )
        }

        selectedDateJob = viewModelScope.launch {
            combine(
                repository.getAllMedicationsWithSchedules(),
                repository.getLogsForDate(selectedDateMillis)
            ) { medications, logs ->
                repository.buildScheduledDosesForDate(
                    selectedDateMillis = selectedDateMillis,
                    medications = medications,
                    logs = logs
                )
            }.collect { doses ->
                _uiState.update {
                    it.copy(
                        selectedDateMillis = selectedDateMillis,
                        doses = doses,
                        isLoading = false
                    )
                }
            }
        }
    }
}