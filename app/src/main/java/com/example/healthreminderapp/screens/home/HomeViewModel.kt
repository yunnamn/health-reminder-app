package com.example.healthreminderapp.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthreminderapp.HealthReminderApplication
import com.example.healthreminderapp.data.local.entity.DailyHealthRecordEntity
import com.example.healthreminderapp.data.model.ScheduledDose
import com.example.healthreminderapp.data.repository.HealthRepository
import com.example.healthreminderapp.data.repository.MedicationRepository
import com.example.healthreminderapp.notifications.MedicationReminderScheduler
import com.example.healthreminderapp.util.startOfDay
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    private val application: HealthReminderApplication,
    private val medicationRepository: MedicationRepository,
    private val healthRepository: HealthRepository
) : ViewModel() {

    private val todayMillis = startOfDay(System.currentTimeMillis())
    private val currentTimeMillis = MutableStateFlow(System.currentTimeMillis())

    private val _uiState = MutableStateFlow(HomeUiState(todayDateMillis = todayMillis))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeHomeData()
        startClockTicker()
    }

    fun markDoseAsTaken(dose: ScheduledDose) {
        if (dose.isLogged) return

        viewModelScope.launch {
            try {
                medicationRepository.markDoseAsTaken(dose)
                MedicationReminderScheduler.cancelDoseReminders(application, dose)
                currentTimeMillis.value = System.currentTimeMillis()
                _uiState.update { it.copy(infoMessage = "Приём отмечен") }
            } catch (_: Exception) {
                _uiState.update { it.copy(errorMessage = "Не удалось отметить приём") }
            }
        }
    }

    fun snoozeDose(dose: ScheduledDose) {
        if (dose.isLogged) return
        if (!isDoseDue(dose, currentTimeMillis.value)) return

        viewModelScope.launch {
            try {
                MedicationReminderScheduler.cancelDoseReminders(application, dose)
                MedicationReminderScheduler.scheduleSnoozedReminder(application, dose)
                currentTimeMillis.value = System.currentTimeMillis()
                _uiState.update { it.copy(infoMessage = "Напоминание отложено на 30 минут") }
            } catch (_: Exception) {
                _uiState.update { it.copy(errorMessage = "Не удалось отложить напоминание") }
            }
        }
    }

    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun observeHomeData() {
        viewModelScope.launch {
            combine(
                medicationRepository.getAllMedicationsWithSchedules(),
                medicationRepository.getLogsForDate(todayMillis),
                healthRepository.observeDailyHealthRecords(),
                healthRepository.observeAnalysisCards(),
                currentTimeMillis
            ) { medications, logs, healthRecords, analysisCards, nowMillis ->
                val doses = medicationRepository.buildScheduledDosesForDate(
                    selectedDateMillis = todayMillis,
                    medications = medications,
                    logs = logs
                ).sortedWith(
                    compareBy<ScheduledDose>(
                        { it.timeHour },
                        { it.timeMinute },
                        { it.medicationName }
                    )
                )

                val homeDoseItems = doses.map { dose ->
                    val snoozedUntil = MedicationReminderScheduler.getSnoozedUntil(application, dose)
                    val isSnoozed = !dose.isLogged && snoozedUntil != null && snoozedUntil > nowMillis
                    val isDue = isDoseDue(dose, nowMillis)

                    HomeDoseItem(
                        dose = dose,
                        stateText = resolveDoseStateText(
                            dose = dose,
                            isDue = isDue,
                            snoozedUntil = snoozedUntil
                        ),
                        isTaken = dose.isLogged,
                        isDue = isDue,
                        isSnoozed = isSnoozed,
                        canMarkTaken = !dose.isLogged,
                        canSnooze = !dose.isLogged && isDue && !isSnoozed
                    )
                }

                HomeUiState(
                    todayDateMillis = todayMillis,
                    isLoading = false,
                    todayDoses = homeDoseItems,
                    takenCount = doses.count { it.isLogged },
                    totalCount = doses.size,
                    latestIndicators = buildLatestIndicators(healthRecords),
                    latestAnalyses = analysisCards
                        .filter { it.latestDateMillis != null }
                        .sortedByDescending { it.latestDateMillis }
                        .take(3)
                )
            }.collect { newState ->
                _uiState.update {
                    newState.copy(
                        infoMessage = it.infoMessage,
                        errorMessage = it.errorMessage
                    )
                }
            }
        }
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            while (true) {
                currentTimeMillis.value = System.currentTimeMillis()
                delay(30_000L)
            }
        }
    }

    private fun isDoseDue(
        dose: ScheduledDose,
        nowMillis: Long
    ): Boolean {
        val scheduledTimeMillis = dose.scheduledDateMillis +
                dose.timeHour * 60L * 60L * 1000L +
                dose.timeMinute * 60L * 1000L

        return scheduledTimeMillis <= nowMillis
    }

    private fun resolveDoseStateText(
        dose: ScheduledDose,
        isDue: Boolean,
        snoozedUntil: Long?
    ): String {
        if (dose.isLogged) return "Принято"

        if (snoozedUntil != null && snoozedUntil > System.currentTimeMillis()) {
            return "Отложено до ${formatClockTime(snoozedUntil)}"
        }

        if (isDue) return "Ожидает приёма"
        return "Запланировано"
    }

    private fun formatClockTime(timestampMillis: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestampMillis
        }

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return "%02d:%02d".format(hour, minute)
    }

    private fun buildLatestIndicators(records: List<DailyHealthRecordEntity>): List<HomeIndicatorItem> {
        return buildList {
            records.firstOrNull { it.systolicPressure != null && it.diastolicPressure != null }?.let { record ->
                add(
                    HomeIndicatorItem(
                        title = "Давление",
                        value = "${record.systolicPressure}/${record.diastolicPressure} мм рт. ст."
                    )
                )
            }

            records.firstOrNull { it.pulse != null }?.let { record ->
                add(
                    HomeIndicatorItem(
                        title = "Пульс",
                        value = "${record.pulse} уд/мин"
                    )
                )
            }

            records.firstOrNull { it.weightKg != null }?.let { record ->
                add(
                    HomeIndicatorItem(
                        title = "Вес",
                        value = "${formatDecimal(record.weightKg)} кг"
                    )
                )
            }

            records.firstOrNull { it.temperatureC != null }?.let { record ->
                add(
                    HomeIndicatorItem(
                        title = "Температура",
                        value = "${formatDecimal(record.temperatureC)} °C"
                    )
                )
            }

            records.firstOrNull { it.glucoseMmolL != null }?.let { record ->
                add(
                    HomeIndicatorItem(
                        title = "Глюкоза",
                        value = "${formatDecimal(record.glucoseMmolL)} ммоль/л"
                    )
                )
            }
        }
    }

    private fun formatDecimal(value: Double?): String {
        if (value == null) return ""

        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            value.toString().replace('.', ',')
        }
    }
}