package com.example.healthreminderapp.screens.medications

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthreminderapp.HealthReminderApplication
import com.example.healthreminderapp.data.local.entity.MedicationEntity
import com.example.healthreminderapp.data.local.entity.MedicationType
import com.example.healthreminderapp.data.local.entity.ScheduleType
import com.example.healthreminderapp.data.local.relation.MedicationWithSchedules
import com.example.healthreminderapp.data.model.MedicationTime
import com.example.healthreminderapp.data.model.WeekDay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MedicationsScreen() {
    val application = LocalContext.current.applicationContext as HealthReminderApplication
    val viewModel: MedicationViewModel = viewModel(
        factory = MedicationViewModelFactory(application)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.showDeleteDialog && uiState.medicationPendingDelete != null) {
        DeleteMedicationDialog(
            medication = uiState.medicationPendingDelete!!,
            onConfirm = viewModel::confirmDeleteMedication,
            onDismiss = viewModel::dismissDeleteDialog
        )
    }

    if (uiState.isAddingMedication) {
        AddMedicationFlow(
            uiState = uiState,
            onCancel = viewModel::cancelAddingMedication,
            onNext = viewModel::goToNextStep,
            onPrevious = viewModel::goToPreviousStep,
            onNameChange = viewModel::updateName,
            onTypeSelected = viewModel::updateType,
            onStrengthAmountChange = viewModel::updateStrengthAmount,
            onStrengthUnitChange = viewModel::updateStrengthUnit,
            onScheduleTypeSelected = viewModel::updateScheduleType,
            onDayToggle = viewModel::toggleDay,
            onTimeHourChange = viewModel::updateTimeHourInput,
            onTimeMinuteChange = viewModel::updateTimeMinuteInput,
            onAddTime = viewModel::addTime,
            onRemoveTime = viewModel::removeTime,
            onDurationDaysChange = viewModel::updateDurationDays,
            onSave = viewModel::saveMedication
        )
    } else {
        MedicationListContent(
            uiState = uiState,
            onAddMedicationClick = viewModel::startAddingMedication,
            onEditMedicationClick = viewModel::startEditingMedication,
            onDeleteMedicationClick = viewModel::requestDeleteMedication,
            onClearMessage = viewModel::clearInfoMessage
        )
    }
}

@Composable
private fun DeleteMedicationDialog(
    medication: MedicationEntity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Удалить лекарство?")
        },
        text = {
            Text("Лекарство \"${medication.name}\" будет удалено вместе с его расписанием.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun MedicationListContent(
    uiState: MedicationUiState,
    onAddMedicationClick: () -> Unit,
    onEditMedicationClick: (MedicationWithSchedules) -> Unit,
    onDeleteMedicationClick: (MedicationEntity) -> Unit,
    onClearMessage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Лекарства",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Добавляйте лекарства и храните их расписание.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        Button(
            onClick = onAddMedicationClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить лекарство")
        }

        uiState.infoMessage?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = it)
                    TextButton(onClick = onClearMessage) {
                        Text("Скрыть")
                    }
                }
            }
        }

        when {
            uiState.isLoading -> {
                Text(
                    text = "Загрузка...",
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            uiState.medications.isEmpty() -> {
                Text(
                    text = "Пока нет добавленных лекарств.",
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.medications) { medicationItem ->
                        MedicationCard(
                            medicationItem = medicationItem,
                            onClick = { onEditMedicationClick(medicationItem) },
                            onDeleteClick = { onDeleteMedicationClick(medicationItem.medication) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicationCard(
    medicationItem: MedicationWithSchedules,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val medication = medicationItem.medication

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить лекарство"
                    )
                }
            }

            Text(
                text = "Тип: ${medication.type.toRussianLabel()}",
                modifier = Modifier.padding(top = 8.dp)
            )

            medication.strengthAmount?.let { amount ->
                Text(
                    text = "Дозировка: ${formatStrength(amount)} ${medication.strengthUnit.orEmpty()}",
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Text(
                text = "Расписание: ${formatScheduleText(medicationItem)}",
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Длительность: ${formatDurationText(medication.startDateMillis, medication.endDateMillis)}",
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Нажмите, чтобы редактировать",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun AddMedicationFlow(
    uiState: MedicationUiState,
    onCancel: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onNameChange: (String) -> Unit,
    onTypeSelected: (MedicationType) -> Unit,
    onStrengthAmountChange: (String) -> Unit,
    onStrengthUnitChange: (String) -> Unit,
    onScheduleTypeSelected: (ScheduleType) -> Unit,
    onDayToggle: (WeekDay) -> Unit,
    onTimeHourChange: (String) -> Unit,
    onTimeMinuteChange: (String) -> Unit,
    onAddTime: () -> Unit,
    onRemoveTime: (MedicationTime) -> Unit,
    onDurationDaysChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isEditing = uiState.editingMedicationId != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = if (isEditing) "Редактирование лекарства" else "Добавление лекарства",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Шаг ${uiState.currentStep} из 6",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        uiState.errorMessage?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        when (uiState.currentStep) {
            1 -> StepMedicationName(
                name = uiState.name,
                onNameChange = onNameChange
            )
            2 -> StepMedicationType(
                selectedType = uiState.type,
                onTypeSelected = onTypeSelected
            )
            3 -> StepMedicationStrength(
                strengthAmount = uiState.strengthAmount,
                strengthUnit = uiState.strengthUnit,
                onStrengthAmountChange = onStrengthAmountChange,
                onStrengthUnitChange = onStrengthUnitChange
            )
            4 -> StepMedicationSchedule(
                scheduleType = uiState.scheduleType,
                selectedDays = uiState.selectedDays,
                timeHourInput = uiState.timeHourInput,
                timeMinuteInput = uiState.timeMinuteInput,
                times = uiState.times,
                onScheduleTypeSelected = onScheduleTypeSelected,
                onDayToggle = onDayToggle,
                onTimeHourChange = onTimeHourChange,
                onTimeMinuteChange = onTimeMinuteChange,
                onAddTime = onAddTime,
                onRemoveTime = onRemoveTime
            )
            5 -> StepMedicationDuration(
                durationDays = uiState.durationDays,
                onDurationDaysChange = onDurationDaysChange
            )
            6 -> StepMedicationReview(uiState = uiState)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onCancel) {
                Text("Отмена")
            }

            Row {
                if (uiState.currentStep > 1) {
                    TextButton(onClick = onPrevious) {
                        Text("Назад")
                    }
                }

                if (uiState.currentStep < 6) {
                    Button(onClick = onNext) {
                        Text("Далее")
                    }
                } else {
                    Button(
                        onClick = onSave,
                        enabled = !uiState.isSaving
                    ) {
                        Text(
                            when {
                                uiState.isSaving -> "Сохранение..."
                                isEditing -> "Обновить"
                                else -> "Сохранить"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepMedicationName(
    name: String,
    onNameChange: (String) -> Unit
) {
    Column {
        Text(
            text = "1. Введите название лекарства",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Название") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true
        )
    }
}

@Composable
private fun StepMedicationType(
    selectedType: MedicationType,
    onTypeSelected: (MedicationType) -> Unit
) {
    Column {
        Text(
            text = "2. Выберите тип лекарства",
            style = MaterialTheme.typography.titleMedium
        )

        FlowRow(
            modifier = Modifier.padding(top = 12.dp)
        ) {
            MedicationType.entries.forEach { type ->
                FilterChip(
                    selected = type == selectedType,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type.toRussianLabel()) },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StepMedicationStrength(
    strengthAmount: String,
    strengthUnit: String,
    onStrengthAmountChange: (String) -> Unit,
    onStrengthUnitChange: (String) -> Unit
) {
    Column {
        Text(
            text = "3. Введите дозировку",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = strengthAmount,
            onValueChange = onStrengthAmountChange,
            label = { Text("Количество") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = strengthUnit,
            onValueChange = onStrengthUnitChange,
            label = { Text("Единица измерения") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true
        )
    }
}

@Composable
private fun StepMedicationSchedule(
    scheduleType: ScheduleType,
    selectedDays: Set<WeekDay>,
    timeHourInput: String,
    timeMinuteInput: String,
    times: List<MedicationTime>,
    onScheduleTypeSelected: (ScheduleType) -> Unit,
    onDayToggle: (WeekDay) -> Unit,
    onTimeHourChange: (String) -> Unit,
    onTimeMinuteChange: (String) -> Unit,
    onAddTime: () -> Unit,
    onRemoveTime: (MedicationTime) -> Unit
) {
    Column {
        Text(
            text = "4. Настройте расписание",
            style = MaterialTheme.typography.titleMedium
        )

        Row(modifier = Modifier.padding(top = 12.dp)) {
            FilterChip(
                selected = scheduleType == ScheduleType.DAILY,
                onClick = { onScheduleTypeSelected(ScheduleType.DAILY) },
                label = { Text("Ежедневно") },
                modifier = Modifier.padding(end = 8.dp)
            )

            FilterChip(
                selected = scheduleType == ScheduleType.SPECIFIC_DAYS,
                onClick = { onScheduleTypeSelected(ScheduleType.SPECIFIC_DAYS) },
                label = { Text("По дням недели") }
            )
        }

        if (scheduleType == ScheduleType.SPECIFIC_DAYS) {
            FlowRow(
                modifier = Modifier.padding(top = 12.dp)
            ) {
                WeekDay.entries.forEach { day ->
                    FilterChip(
                        selected = selectedDays.contains(day),
                        onClick = { onDayToggle(day) },
                        label = { Text(day.shortLabel) },
                        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                    )
                }
            }
        }

        Text(
            text = "Добавьте одно или несколько времён приёма",
            modifier = Modifier.padding(top = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            OutlinedTextField(
                value = timeHourInput,
                onValueChange = onTimeHourChange,
                label = { Text("Часы") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Spacer(modifier = Modifier.padding(4.dp))

            OutlinedTextField(
                value = timeMinuteInput,
                onValueChange = onTimeMinuteChange,
                label = { Text("Минуты") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Button(
            onClick = onAddTime,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text("Добавить время")
        }

        if (times.isNotEmpty()) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                times.forEach { time ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(formatTime(time.hour, time.minute))
                            IconButton(onClick = { onRemoveTime(time) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Удалить время"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepMedicationDuration(
    durationDays: String,
    onDurationDaysChange: (String) -> Unit
) {
    Column {
        Text(
            text = "5. Укажите длительность",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Введите количество дней. Если оставить поле пустым, курс будет без даты окончания.",
            modifier = Modifier.padding(top = 12.dp)
        )

        OutlinedTextField(
            value = durationDays,
            onValueChange = onDurationDaysChange,
            label = { Text("Количество дней") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true
        )
    }
}

@Composable
private fun StepMedicationReview(
    uiState: MedicationUiState
) {
    Column {
        Text(
            text = "6. Проверьте данные",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Название: ${uiState.name}",
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(text = "Тип: ${uiState.type.toRussianLabel()}")
        Text(text = "Дозировка: ${uiState.strengthAmount} ${uiState.strengthUnit}")

        Text(
            text = "Расписание: ${
                if (uiState.scheduleType == ScheduleType.DAILY) {
                    "Ежедневно"
                } else {
                    "По дням: " + uiState.selectedDays
                        .sortedBy { it.ordinal }
                        .joinToString(", ") { it.shortLabel }
                }
            }",
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = "Время приёма: ${
                if (uiState.times.isEmpty()) {
                    "Не добавлено"
                } else {
                    uiState.times.joinToString(", ") { formatTime(it.hour, it.minute) }
                }
            }",
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = "Длительность: ${
                if (uiState.durationDays.isBlank()) {
                    "Без даты окончания"
                } else {
                    "${uiState.durationDays} дн."
                }
            }",
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private fun MedicationType.toRussianLabel(): String {
    return when (this) {
        MedicationType.TABLET -> "Таблетка"
        MedicationType.CAPSULE -> "Капсула"
        MedicationType.LIQUID -> "Жидкость"
        MedicationType.DROPS -> "Капли"
        MedicationType.INJECTION -> "Инъекция"
        MedicationType.INHALER -> "Ингалятор"
        MedicationType.OINTMENT -> "Мазь"
        MedicationType.POWDER -> "Порошок"
        MedicationType.OTHER -> "Другое"
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    return "%02d:%02d".format(hour, minute)
}

private fun formatStrength(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
        amount.toInt().toString()
    } else {
        amount.toString()
    }
}

private fun formatScheduleText(item: MedicationWithSchedules): String {
    val sortedSchedules = item.schedules.sortedWith(
        compareBy({ it.timeHour }, { it.timeMinute })
    )

    if (sortedSchedules.isEmpty()) {
        return "Не указано"
    }

    val timeText = sortedSchedules.joinToString(", ") {
        formatTime(it.timeHour, it.timeMinute)
    }

    val firstSchedule = sortedSchedules.first()

    return if (firstSchedule.scheduleType == ScheduleType.DAILY) {
        "Ежедневно, $timeText"
    } else {
        val days = buildList {
            if (firstSchedule.monday) add("Пн")
            if (firstSchedule.tuesday) add("Вт")
            if (firstSchedule.wednesday) add("Ср")
            if (firstSchedule.thursday) add("Чт")
            if (firstSchedule.friday) add("Пт")
            if (firstSchedule.saturday) add("Сб")
            if (firstSchedule.sunday) add("Вс")
        }.joinToString(", ")

        "По дням ($days), $timeText"
    }
}

@SuppressLint("SimpleDateFormat")
private fun formatDurationText(startDateMillis: Long?, endDateMillis: Long?): String {
    if (startDateMillis == null && endDateMillis == null) {
        return "Не указана"
    }

    if (startDateMillis != null && endDateMillis == null) {
        return "Без даты окончания"
    }

    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    val startText = startDateMillis?.let { formatter.format(Date(it)) } ?: "?"
    val endText = endDateMillis?.let { formatter.format(Date(it)) } ?: "?"

    return "$startText - $endText"
}