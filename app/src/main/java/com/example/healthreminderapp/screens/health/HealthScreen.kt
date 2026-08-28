package com.example.healthreminderapp.screens.health

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthreminderapp.HealthReminderApplication
import com.example.healthreminderapp.data.local.entity.DailyHealthRecordEntity
import com.example.healthreminderapp.data.local.entity.ProfileSex
import com.example.healthreminderapp.data.model.AnalysisCardData
import com.example.healthreminderapp.data.model.AnalysisDetailData
import com.example.healthreminderapp.data.model.AnalysisResultHistoryItem
import com.example.healthreminderapp.data.model.AnalysisSuggestion
import com.example.healthreminderapp.util.startOfDay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HealthScreen(
    initialSection: HealthSection = HealthSection.PROFILE,
    initialAction: HealthEntryAction = HealthEntryAction.NONE
) {
    val application = LocalContext.current.applicationContext as HealthReminderApplication
    val viewModel: HealthViewModel = viewModel(
        factory = HealthViewModelFactory(application)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(initialSection, initialAction) {
        viewModel.handleEntry(initialSection, initialAction)
    }

    HealthContent(
        uiState = uiState,
        onSectionSelected = viewModel::selectSection,
        onStartEditingProfile = viewModel::startEditingProfile,
        onNameChange = viewModel::updateName,
        onSexSelected = viewModel::updateSex,
        onBirthDateSelected = viewModel::updateBirthDate,
        onHeightChange = viewModel::updateHeightCm,
        onSaveProfile = viewModel::saveProfile,
        onStartAddingHealthRecord = viewModel::startAddingHealthRecord,
        onCancelAddingHealthRecord = viewModel::cancelAddingHealthRecord,
        onHealthRecordDateSelected = viewModel::updateHealthRecordDate,
        onHealthRecordTimeSelected = viewModel::updateHealthRecordTime,
        onSystolicChange = viewModel::updateSystolicInput,
        onDiastolicChange = viewModel::updateDiastolicInput,
        onPulseChange = viewModel::updatePulseInput,
        onWeightChange = viewModel::updateWeightInput,
        onTemperatureChange = viewModel::updateTemperatureInput,
        onGlucoseChange = viewModel::updateGlucoseInput,
        onSaveHealthRecord = viewModel::saveHealthRecord,
        onStartAddingAnalysis = viewModel::startAddingAnalysis,
        onStartEditingSelectedAnalysis = viewModel::startEditingSelectedAnalysis,
        onCancelAnalysisEditing = viewModel::cancelAnalysisEditing,
        onAnalysisNameChange = viewModel::updateAnalysisName,
        onAnalysisUnitChange = viewModel::updateAnalysisUnit,
        onApplyAnalysisSuggestion = viewModel::applyAnalysisSuggestion,
        onSaveAnalysis = viewModel::saveAnalysis,
        onOpenAnalysis = viewModel::openAnalysis,
        onOpenArchivedAnalyses = viewModel::openArchivedAnalyses,
        onBackToAnalysesList = viewModel::backToAnalysesList,
        onBackToActiveAnalysesList = viewModel::backToActiveAnalysesList,
        onStartAddingResult = viewModel::startAddingResult,
        onCancelAddingResult = viewModel::cancelAddingResult,
        onAnalysisResultValueChange = viewModel::updateAnalysisResultValue,
        onAnalysisResultDateChange = viewModel::updateAnalysisResultDate,
        onAnalysisResultNoteChange = viewModel::updateAnalysisResultNote,
        onArchiveSelectedAnalysis = viewModel::archiveSelectedAnalysis,
        onRestoreSelectedAnalysis = viewModel::restoreSelectedAnalysis,
        onSaveAnalysisResult = viewModel::saveAnalysisResult,
        onClearInfoMessage = viewModel::clearInfoMessage,
        onClearErrorMessage = viewModel::clearErrorMessage
    )
}

@Composable
private fun HealthContent(
    uiState: HealthUiState,
    onSectionSelected: (HealthSection) -> Unit,
    onStartEditingProfile: () -> Unit,
    onNameChange: (String) -> Unit,
    onSexSelected: (ProfileSex) -> Unit,
    onBirthDateSelected: (Long) -> Unit,
    onHeightChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onStartAddingHealthRecord: () -> Unit,
    onCancelAddingHealthRecord: () -> Unit,
    onHealthRecordDateSelected: (Long) -> Unit,
    onHealthRecordTimeSelected: (Int, Int) -> Unit,
    onSystolicChange: (String) -> Unit,
    onDiastolicChange: (String) -> Unit,
    onPulseChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onTemperatureChange: (String) -> Unit,
    onGlucoseChange: (String) -> Unit,
    onSaveHealthRecord: () -> Unit,
    onStartAddingAnalysis: () -> Unit,
    onStartEditingSelectedAnalysis: () -> Unit,
    onCancelAnalysisEditing: () -> Unit,
    onAnalysisNameChange: (String) -> Unit,
    onAnalysisUnitChange: (String) -> Unit,
    onApplyAnalysisSuggestion: (AnalysisSuggestion) -> Unit,
    onSaveAnalysis: () -> Unit,
    onOpenAnalysis: (Long) -> Unit,
    onOpenArchivedAnalyses: () -> Unit,
    onBackToAnalysesList: () -> Unit,
    onBackToActiveAnalysesList: () -> Unit,
    onStartAddingResult: () -> Unit,
    onCancelAddingResult: () -> Unit,
    onAnalysisResultValueChange: (String) -> Unit,
    onAnalysisResultDateChange: (Long) -> Unit,
    onAnalysisResultNoteChange: (String) -> Unit,
    onArchiveSelectedAnalysis: () -> Unit,
    onRestoreSelectedAnalysis: () -> Unit,
    onSaveAnalysisResult: () -> Unit,
    onClearInfoMessage: () -> Unit,
    onClearErrorMessage: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Здоровье",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Раздел содержит профиль, показатели и анализы.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HealthSection.entries.forEach { section ->
                FilterChip(
                    selected = uiState.selectedSection == section,
                    onClick = { onSectionSelected(section) },
                    label = { Text(section.title) }
                )
            }
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
                    TextButton(onClick = onClearInfoMessage) {
                        Text("Скрыть")
                    }
                }
            }
        }

        uiState.errorMessage?.let {
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
                    TextButton(onClick = onClearErrorMessage) {
                        Text("Скрыть")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState.selectedSection) {
            HealthSection.PROFILE -> {
                ProfileSection(
                    uiState = uiState,
                    onStartEditingProfile = onStartEditingProfile,
                    onNameChange = onNameChange,
                    onSexSelected = onSexSelected,
                    onBirthDateSelected = onBirthDateSelected,
                    onHeightChange = onHeightChange,
                    onSaveProfile = onSaveProfile
                )
            }

            HealthSection.INDICATORS -> {
                IndicatorsSection(
                    uiState = uiState,
                    onStartAddingRecord = onStartAddingHealthRecord,
                    onCancelAddingRecord = onCancelAddingHealthRecord,
                    onRecordDateSelected = onHealthRecordDateSelected,
                    onRecordTimeSelected = onHealthRecordTimeSelected,
                    onSystolicChange = onSystolicChange,
                    onDiastolicChange = onDiastolicChange,
                    onPulseChange = onPulseChange,
                    onWeightChange = onWeightChange,
                    onTemperatureChange = onTemperatureChange,
                    onGlucoseChange = onGlucoseChange,
                    onSaveRecord = onSaveHealthRecord
                )
            }

            HealthSection.ANALYSES -> {
                AnalysesSection(
                    uiState = uiState,
                    onStartAddingAnalysis = onStartAddingAnalysis,
                    onStartEditingSelectedAnalysis = onStartEditingSelectedAnalysis,
                    onCancelAnalysisEditing = onCancelAnalysisEditing,
                    onAnalysisNameChange = onAnalysisNameChange,
                    onAnalysisUnitChange = onAnalysisUnitChange,
                    onApplyAnalysisSuggestion = onApplyAnalysisSuggestion,
                    onSaveAnalysis = onSaveAnalysis,
                    onOpenAnalysis = onOpenAnalysis,
                    onOpenArchivedAnalyses = onOpenArchivedAnalyses,
                    onBackToAnalysesList = onBackToAnalysesList,
                    onBackToActiveAnalysesList = onBackToActiveAnalysesList,
                    onStartAddingResult = onStartAddingResult,
                    onCancelAddingResult = onCancelAddingResult,
                    onAnalysisResultValueChange = onAnalysisResultValueChange,
                    onAnalysisResultDateChange = onAnalysisResultDateChange,
                    onAnalysisResultNoteChange = onAnalysisResultNoteChange,
                    onArchiveSelectedAnalysis = onArchiveSelectedAnalysis,
                    onRestoreSelectedAnalysis = onRestoreSelectedAnalysis,
                    onSaveAnalysisResult = onSaveAnalysisResult
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileSection(
    uiState: HealthUiState,
    onStartEditingProfile: () -> Unit,
    onNameChange: (String) -> Unit,
    onSexSelected: (ProfileSex) -> Unit,
    onBirthDateSelected: (Long) -> Unit,
    onHeightChange: (String) -> Unit,
    onSaveProfile: () -> Unit
) {
    if (uiState.hasSavedProfile && !uiState.isEditingProfile) {
        ProfileViewCard(
            uiState = uiState,
            onEditClick = onStartEditingProfile
        )
    } else {
        ProfileEditCard(
            uiState = uiState,
            onNameChange = onNameChange,
            onSexSelected = onSexSelected,
            onBirthDateSelected = onBirthDateSelected,
            onHeightChange = onHeightChange,
            onSaveProfile = onSaveProfile
        )
    }
}

@Composable
private fun ProfileViewCard(
    uiState: HealthUiState,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Личные данные",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (uiState.nameInput.isNotBlank()) {
                ProfileInfoRow("Имя", uiState.nameInput)
            }

            ProfileInfoRow(
                "Пол",
                uiState.sex?.toRussianLabel() ?: "Не указано"
            )

            ProfileInfoRow(
                "Дата рождения",
                uiState.birthDateMillis?.let(::formatBirthDate) ?: "Не указано"
            )

            ProfileInfoRow(
                "Возраст",
                uiState.calculatedAge?.toString() ?: "Не указано"
            )

            ProfileInfoRow(
                "Рост",
                uiState.heightCmInput.takeIf { it.isNotBlank() }?.let { "$it см" } ?: "Не указано"
            )

            Button(
                onClick = onEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                Text("Редактировать")
            }
        }
    }
}

@Composable
private fun ProfileEditCard(
    uiState: HealthUiState,
    onNameChange: (String) -> Unit,
    onSexSelected: (ProfileSex) -> Unit,
    onBirthDateSelected: (Long) -> Unit,
    onHeightChange: (String) -> Unit,
    onSaveProfile: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Личные данные",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = uiState.nameInput,
                onValueChange = onNameChange,
                label = { Text("Имя (необязательно)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                singleLine = true
            )

            Text(
                text = "Пол",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileSex.entries.forEach { sex ->
                    FilterChip(
                        selected = uiState.sex == sex,
                        onClick = { onSexSelected(sex) },
                        label = { Text(sex.toRussianLabel()) }
                    )
                }
            }

            Text(
                text = "Дата рождения",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )

            OutlinedTextField(
                value = uiState.birthDateMillis?.let(::formatBirthDate).orEmpty(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Дата рождения") },
                placeholder = { Text("Выберите дату") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clickable {
                        showBirthDatePicker(
                            context = context,
                            currentBirthDateMillis = uiState.birthDateMillis,
                            onDateSelected = onBirthDateSelected
                        )
                    }
            )

            Button(
                onClick = {
                    showBirthDatePicker(
                        context = context,
                        currentBirthDateMillis = uiState.birthDateMillis,
                        onDateSelected = onBirthDateSelected
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Изменить дату рождения")
            }

            uiState.calculatedAge?.let { age ->
                Text(
                    text = "Возраст: $age",
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = uiState.heightCmInput,
                onValueChange = onHeightChange,
                label = { Text("Рост (см)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Button(
                onClick = onSaveProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                enabled = !uiState.isSaving
            ) {
                Text(if (uiState.isSaving) "Сохранение..." else "Сохранить")
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String
) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun IndicatorsSection(
    uiState: HealthUiState,
    onStartAddingRecord: () -> Unit,
    onCancelAddingRecord: () -> Unit,
    onRecordDateSelected: (Long) -> Unit,
    onRecordTimeSelected: (Int, Int) -> Unit,
    onSystolicChange: (String) -> Unit,
    onDiastolicChange: (String) -> Unit,
    onPulseChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onTemperatureChange: (String) -> Unit,
    onGlucoseChange: (String) -> Unit,
    onSaveRecord: () -> Unit
) {
    val context = LocalContext.current

    Column {
        Button(
            onClick = onStartAddingRecord,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить запись")
        }

        if (uiState.isAddingHealthRecord) {
            HealthRecordFormCard(
                uiState = uiState,
                onDateClick = {
                    showRecordDatePicker(
                        context = context,
                        currentDateTimeMillis = uiState.recordDateTimeMillis,
                        onDateSelected = onRecordDateSelected
                    )
                },
                onTimeClick = {
                    showRecordTimePicker(
                        context = context,
                        currentDateTimeMillis = uiState.recordDateTimeMillis,
                        onTimeSelected = onRecordTimeSelected
                    )
                },
                onSystolicChange = onSystolicChange,
                onDiastolicChange = onDiastolicChange,
                onPulseChange = onPulseChange,
                onWeightChange = onWeightChange,
                onTemperatureChange = onTemperatureChange,
                onGlucoseChange = onGlucoseChange,
                onCancel = onCancelAddingRecord,
                onSave = onSaveRecord
            )
        }

        LatestIndicatorsCard(
            records = uiState.dailyHealthRecords,
            modifier = Modifier.padding(top = 16.dp)
        )

        HealthHistorySection(
            records = uiState.dailyHealthRecords,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun HealthRecordFormCard(
    uiState: HealthUiState,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onSystolicChange: (String) -> Unit,
    onDiastolicChange: (String) -> Unit,
    onPulseChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onTemperatureChange: (String) -> Unit,
    onGlucoseChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Новая запись",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = formatRecordDateTime(uiState.recordDateTimeMillis),
                onValueChange = {},
                readOnly = true,
                label = { Text("Дата и время") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDateClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Дата")
                }

                Button(
                    onClick = onTimeClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Время")
                }
            }

            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.systolicInput,
                    onValueChange = onSystolicChange,
                    label = { Text("Давление верхнее") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.diastolicInput,
                    onValueChange = onDiastolicChange,
                    label = { Text("Давление нижнее") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = uiState.pulseInput,
                onValueChange = onPulseChange,
                label = { Text("Пульс") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.weightInput,
                onValueChange = onWeightChange,
                label = { Text("Вес (кг)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.temperatureInput,
                onValueChange = onTemperatureChange,
                label = { Text("Температура (°C)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.glucoseInput,
                onValueChange = onGlucoseChange,
                label = { Text("Глюкоза (ммоль/л)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isSaving
                ) {
                    Text(if (uiState.isSaving) "Сохранение..." else "Сохранить")
                }
            }
        }
    }
}

@Composable
private fun LatestIndicatorsCard(
    records: List<DailyHealthRecordEntity>,
    modifier: Modifier = Modifier
) {
    val latestPressure = records.firstOrNull { it.systolicPressure != null && it.diastolicPressure != null }
    val latestPulse = records.firstOrNull { it.pulse != null }
    val latestWeight = records.firstOrNull { it.weightKg != null }
    val latestTemperature = records.firstOrNull { it.temperatureC != null }
    val latestGlucose = records.firstOrNull { it.glucoseMmolL != null }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Последние значения",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IndicatorValueRow(
                title = "Давление",
                value = latestPressure?.let { "${it.systolicPressure}/${it.diastolicPressure} мм рт. ст." } ?: "Нет данных"
            )

            IndicatorValueRow(
                title = "Пульс",
                value = latestPulse?.pulse?.let { "$it уд/мин" } ?: "Нет данных"
            )

            IndicatorValueRow(
                title = "Вес",
                value = latestWeight?.weightKg?.let { "${formatDecimal(it)} кг" } ?: "Нет данных"
            )

            IndicatorValueRow(
                title = "Температура",
                value = latestTemperature?.temperatureC?.let { "${formatDecimal(it)} °C" } ?: "Нет данных"
            )

            IndicatorValueRow(
                title = "Глюкоза",
                value = latestGlucose?.glucoseMmolL?.let { "${formatDecimal(it)} ммоль/л" } ?: "Нет данных"
            )
        }
    }
}

@Composable
private fun IndicatorValueRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value)
    }
}

@Composable
private fun HealthHistorySection(
    records: List<DailyHealthRecordEntity>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "История",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (records.isEmpty()) {
                Text(
                    text = "Записей пока нет.",
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val grouped = records.groupBy { startOfDay(it.recordedAtMillis) }
                    .toSortedMap(compareByDescending { it })

                grouped.forEach { (dateMillis, dayRecords) ->
                    Text(
                        text = formatHistoryDate(dateMillis),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    dayRecords.forEach { record ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = formatHistoryTime(record.recordedAtMillis),
                                    fontWeight = FontWeight.SemiBold
                                )

                                record.systolicPressure?.let { sys ->
                                    record.diastolicPressure?.let { dia ->
                                        Text(
                                            text = "Давление: $sys/$dia мм рт. ст.",
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }
                                }

                                record.pulse?.let {
                                    Text(
                                        text = "Пульс: $it уд/мин",
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                record.weightKg?.let {
                                    Text(
                                        text = "Вес: ${formatDecimal(it)} кг",
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                record.temperatureC?.let {
                                    Text(
                                        text = "Температура: ${formatDecimal(it)} °C",
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                record.glucoseMmolL?.let {
                                    Text(
                                        text = "Глюкоза: ${formatDecimal(it)} ммоль/л",
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysesSection(
    uiState: HealthUiState,
    onStartAddingAnalysis: () -> Unit,
    onStartEditingSelectedAnalysis: () -> Unit,
    onCancelAnalysisEditing: () -> Unit,
    onAnalysisNameChange: (String) -> Unit,
    onAnalysisUnitChange: (String) -> Unit,
    onApplyAnalysisSuggestion: (AnalysisSuggestion) -> Unit,
    onSaveAnalysis: () -> Unit,
    onOpenAnalysis: (Long) -> Unit,
    onOpenArchivedAnalyses: () -> Unit,
    onBackToAnalysesList: () -> Unit,
    onBackToActiveAnalysesList: () -> Unit,
    onStartAddingResult: () -> Unit,
    onCancelAddingResult: () -> Unit,
    onAnalysisResultValueChange: (String) -> Unit,
    onAnalysisResultDateChange: (Long) -> Unit,
    onAnalysisResultNoteChange: (String) -> Unit,
    onArchiveSelectedAnalysis: () -> Unit,
    onRestoreSelectedAnalysis: () -> Unit,
    onSaveAnalysisResult: () -> Unit
) {
    val context = LocalContext.current

    when (uiState.analysisScreenMode) {
        AnalysisScreenMode.LIST -> {
            AnalysisListScreen(
                title = "Анализы",
                cards = uiState.analysisCards,
                emptyTitle = "Пока нет сохранённых анализов.",
                emptySubtitle = "Нажмите «Добавить анализ», чтобы создать первый анализ.",
                primaryButtonText = "Добавить анализ",
                secondaryButtonText = if (uiState.archivedAnalysisCards.isEmpty()) null else "Архив",
                onPrimaryClick = onStartAddingAnalysis,
                onSecondaryClick = onOpenArchivedAnalyses,
                onCardClick = onOpenAnalysis
            )
        }

        AnalysisScreenMode.ARCHIVED_LIST -> {
            AnalysisListScreen(
                title = "Архив анализов",
                cards = uiState.archivedAnalysisCards,
                emptyTitle = "Архив пока пуст.",
                emptySubtitle = "Здесь будут храниться архивные анализы, которые можно восстановить.",
                primaryButtonText = "К активным анализам",
                secondaryButtonText = null,
                onPrimaryClick = onBackToActiveAnalysesList,
                onSecondaryClick = null,
                onCardClick = onOpenAnalysis
            )
        }

        AnalysisScreenMode.ADD_ANALYSIS,
        AnalysisScreenMode.EDIT_ANALYSIS -> {
            AnalysisEditorCard(
                title = if (uiState.analysisScreenMode == AnalysisScreenMode.EDIT_ANALYSIS) {
                    "Редактировать анализ"
                } else {
                    "Добавить анализ"
                },
                name = uiState.analysisNameInput,
                unit = uiState.analysisUnitInput,
                suggestions = uiState.analysisSuggestions,
                isSaving = uiState.isSaving,
                onNameChange = onAnalysisNameChange,
                onUnitChange = onAnalysisUnitChange,
                onSuggestionClick = onApplyAnalysisSuggestion,
                onCancel = onCancelAnalysisEditing,
                onSave = onSaveAnalysis
            )
        }

        AnalysisScreenMode.DETAIL -> {
            AnalysisDetailCard(
                detail = uiState.selectedAnalysisDetail,
                isLoading = uiState.isAnalysisDetailLoading,
                isSaving = uiState.isSaving,
                onBackClick = onBackToAnalysesList,
                onAddResultClick = onStartAddingResult,
                onEditAnalysisClick = onStartEditingSelectedAnalysis,
                onArchiveClick = onArchiveSelectedAnalysis,
                onRestoreClick = onRestoreSelectedAnalysis
            )
        }

        AnalysisScreenMode.ADD_RESULT -> {
            AnalysisResultEditorCard(
                detail = uiState.selectedAnalysisDetail,
                value = uiState.analysisResultValueInput,
                dateMillis = uiState.analysisResultDateMillis,
                note = uiState.analysisResultNoteInput,
                isSaving = uiState.isSaving,
                onBackClick = onBackToAnalysesList,
                onValueChange = onAnalysisResultValueChange,
                onDateClick = {
                    showAnalysisDatePicker(
                        context = context,
                        currentDateMillis = uiState.analysisResultDateMillis,
                        onDateSelected = onAnalysisResultDateChange
                    )
                },
                onNoteChange = onAnalysisResultNoteChange,
                onCancel = onCancelAddingResult,
                onSave = onSaveAnalysisResult
            )
        }
    }
}

@Composable
private fun AnalysisListScreen(
    title: String,
    cards: List<AnalysisCardData>,
    emptyTitle: String,
    emptySubtitle: String,
    primaryButtonText: String,
    secondaryButtonText: String?,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: (() -> Unit)?,
    onCardClick: (Long) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onPrimaryClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(primaryButtonText)
            }

            if (secondaryButtonText != null && onSecondaryClick != null) {
                TextButton(
                    onClick = onSecondaryClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(secondaryButtonText)
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (cards.isEmpty()) {
                    Text(
                        text = emptyTitle,
                        modifier = Modifier.padding(top = 12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = emptySubtitle,
                        modifier = Modifier.padding(top = 6.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    cards.forEach { card ->
                        AnalysisOverviewCard(
                            card = card,
                            onClick = { onCardClick(card.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisEditorCard(
    title: String,
    name: String,
    unit: String,
    suggestions: List<AnalysisSuggestion>,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onSuggestionClick: (AnalysisSuggestion) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Название анализа") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                singleLine = true
            )

            if (suggestions.isNotEmpty()) {
                Text(
                    text = "Подсказки",
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                suggestions.forEach { suggestion ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clickable { onSuggestionClick(suggestion) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = suggestion.name,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Единица измерения: ${suggestion.defaultUnit}",
                                modifier = Modifier.padding(top = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = unit,
                onValueChange = onUnitChange,
                label = { Text("Единица измерения") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isSaving) "Сохранение..." else "Сохранить")
                }
            }
        }
    }
}

@Composable
private fun AnalysisDetailCard(
    detail: AnalysisDetailData?,
    isLoading: Boolean,
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onAddResultClick: () -> Unit,
    onEditAnalysisClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onRestoreClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TextButton(onClick = onBackClick) {
                Text("Назад к анализам")
            }

            if (isLoading) {
                Text(
                    text = "Загрузка...",
                    modifier = Modifier.padding(top = 8.dp)
                )
                return@Column
            }

            if (detail == null) {
                Text(
                    text = "Не удалось открыть анализ.",
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            Text(
                text = detail.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Единица измерения: ${detail.defaultUnit}",
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = if (detail.isArchived) "Статус: в архиве" else "Статус: активный",
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!detail.isArchived) {
                    Button(
                        onClick = onAddResultClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Добавить результат")
                    }
                }

                TextButton(
                    onClick = onEditAnalysisClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Редактировать анализ")
                }
            }

            TextButton(
                onClick = if (detail.isArchived) onRestoreClick else onArchiveClick,
                enabled = !isSaving,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    if (isSaving) {
                        "Сохранение..."
                    } else if (detail.isArchived) {
                        "Восстановить из архива"
                    } else {
                        "Переместить в архив"
                    }
                )
            }

            Text(
                text = "История результатов",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 20.dp),
                fontWeight = FontWeight.Bold
            )

            if (detail.results.isEmpty()) {
                Text(
                    text = "Пока нет результатов.",
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                detail.results.forEach { result ->
                    AnalysisHistoryItemCard(result = result)
                }
            }
        }
    }
}

@Composable
private fun AnalysisResultEditorCard(
    detail: AnalysisDetailData?,
    value: String,
    dateMillis: Long,
    note: String,
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onValueChange: (String) -> Unit,
    onDateClick: () -> Unit,
    onNoteChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TextButton(onClick = onBackClick) {
                Text("Назад к анализам")
            }

            Text(
                text = "Добавить результат",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            detail?.let {
                Text(
                    text = it.name,
                    modifier = Modifier.padding(top = 8.dp),
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Единица измерения: ${it.defaultUnit}",
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text("Значение") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            OutlinedTextField(
                value = formatAnalysisDate(dateMillis),
                onValueChange = {},
                readOnly = true,
                label = { Text("Дата") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )

            Button(
                onClick = onDateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Выбрать дату")
            }

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                label = { Text("Заметка (необязательно)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isSaving) "Сохранение..." else "Сохранить")
                }
            }
        }
    }
}

@Composable
private fun AnalysisOverviewCard(
    card: AnalysisCardData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = card.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Последнее значение: ${card.latestValueText}",
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Дата: ${card.latestDateMillis?.let(::formatAnalysisDate) ?: "Нет даты"}",
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = card.helperText,
                modifier = Modifier.padding(top = 10.dp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AnalysisHistoryItemCard(
    result: AnalysisResultHistoryItem
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = result.valueText,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = formatAnalysisDate(result.resultDateMillis),
                modifier = Modifier.padding(top = 4.dp)
            )

            result.note?.let { note ->
                Text(
                    text = note,
                    modifier = Modifier.padding(top = 6.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun ProfileSex.toRussianLabel(): String {
    return when (this) {
        ProfileSex.FEMALE -> "Женский"
        ProfileSex.MALE -> "Мужской"
        ProfileSex.OTHER -> "Другой"
    }
}

private fun formatBirthDate(timestampMillis: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    return formatter.format(Date(timestampMillis))
}

private fun formatRecordDateTime(timestampMillis: Long): String {
    return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
        .format(Date(timestampMillis))
}

private fun formatHistoryDate(timestampMillis: Long): String {
    return SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        .format(Date(timestampMillis))
}

private fun formatHistoryTime(timestampMillis: Long): String {
    return SimpleDateFormat("HH:mm", Locale("ru"))
        .format(Date(timestampMillis))
}

private fun formatAnalysisDate(timestampMillis: Long): String {
    return SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
        .format(Date(timestampMillis))
}

private fun formatDecimal(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else value.toString().replace(".", ",")
}

private fun showBirthDatePicker(
    context: Context,
    currentBirthDateMillis: Long?,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = currentBirthDateMillis ?: System.currentTimeMillis()
    }

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedMillis = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            onDateSelected(startOfDay(selectedMillis))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.maxDate = System.currentTimeMillis()
    }.show()
}

private fun showRecordDatePicker(
    context: Context,
    currentDateTimeMillis: Long,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = currentDateTimeMillis
    }

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedMillis = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            onDateSelected(selectedMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun showRecordTimePicker(
    context: Context,
    currentDateTimeMillis: Long,
    onTimeSelected: (Int, Int) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = currentDateTimeMillis
    }

    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onTimeSelected(hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    ).show()
}

private fun showAnalysisDatePicker(
    context: Context,
    currentDateMillis: Long,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = currentDateMillis
    }

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedMillis = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            onDateSelected(selectedMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}