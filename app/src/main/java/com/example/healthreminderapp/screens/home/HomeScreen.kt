package com.example.healthreminderapp.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.healthreminderapp.data.model.AnalysisCardData
import com.example.healthreminderapp.data.model.ScheduledDose
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onOpenSchedule: () -> Unit,
    onOpenAddIndicator: () -> Unit,
    onOpenAddAnalysis: () -> Unit
) {
    val application = LocalContext.current.applicationContext as HealthReminderApplication
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(application)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onMarkDoseTaken = viewModel::markDoseAsTaken,
        onSnoozeDose = viewModel::snoozeDose,
        onClearInfoMessage = viewModel::clearInfoMessage,
        onClearErrorMessage = viewModel::clearErrorMessage
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onMarkDoseTaken: (ScheduledDose) -> Unit,
    onSnoozeDose: (ScheduledDose) -> Unit,
    onClearInfoMessage: () -> Unit,
    onClearErrorMessage: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Главная",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = formatHomeDate(uiState.todayDateMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        uiState.infoMessage?.let { message ->
            item {
                HomeMessageCard(
                    message = message,
                    onDismiss = onClearInfoMessage
                )
            }
        }

        uiState.errorMessage?.let { message ->
            item {
                HomeMessageCard(
                    message = message,
                    onDismiss = onClearErrorMessage
                )
            }
        }

        item {
            DailySummaryCard(
                takenCount = uiState.takenCount,
                totalCount = uiState.totalCount
            )
        }

        item {
            TodayMedicationsCard(
                doses = uiState.todayDoses,
                isLoading = uiState.isLoading,
                onMarkDoseTaken = onMarkDoseTaken,
                onSnoozeDose = onSnoozeDose
            )
        }

        item {
            LatestIndicatorsCard(indicators = uiState.latestIndicators)
        }

        item {
            LatestAnalysesCard(analyses = uiState.latestAnalyses)
        }
    }
}

@Composable
private fun HomeMessageCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f)
            )

            TextButton(onClick = onDismiss) {
                Text("Скрыть")
            }
        }
    }
}

@Composable
private fun DailySummaryCard(
    takenCount: Int,
    totalCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Краткая сводка за день",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Принято: $takenCount из $totalCount",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun TodayMedicationsCard(
    doses: List<HomeDoseItem>,
    isLoading: Boolean,
    onMarkDoseTaken: (ScheduledDose) -> Unit,
    onSnoozeDose: (ScheduledDose) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Сегодняшние лекарства",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> Text("Загрузка...")
                doses.isEmpty() -> Text("На сегодня приёмов нет.")
                else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    doses.forEach { item ->
                        DoseRow(
                            item = item,
                            onMarkDoseTaken = { onMarkDoseTaken(item.dose) },
                            onSnoozeDose = { onSnoozeDose(item.dose) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DoseRow(
    item: HomeDoseItem,
    onMarkDoseTaken: () -> Unit,
    onSnoozeDose: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.dose.medicationName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Время: ${formatTime(item.dose.timeHour, item.dose.timeMinute)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp)
            )

            Text(
                text = "Состояние: ${item.stateText}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.isTaken) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!item.isTaken) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onMarkDoseTaken,
                        enabled = item.canMarkTaken,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Принято")
                    }

                    if (item.canSnooze) {
                        OutlinedButton(
                            onClick = onSnoozeDose,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Напомнить позже")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LatestIndicatorsCard(indicators: List<HomeIndicatorItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Последние показатели",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (indicators.isEmpty()) {
                Text("Пока нет сохранённых показателей.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    indicators.forEach { indicator ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(indicator.title)
                            Text(
                                text = indicator.value,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LatestAnalysesCard(analyses: List<AnalysisCardData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Последние анализы",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (analyses.isEmpty()) {
                Text("Пока нет сохранённых результатов анализов.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    analyses.forEach { analysis ->
                        Column {
                            Text(
                                text = analysis.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = analysis.latestValueText,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            analysis.latestDateMillis?.let { dateMillis ->
                                Text(
                                    text = formatAnalysisDate(dateMillis),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    return "%02d:%02d".format(hour, minute)
}

@SuppressLint("SimpleDateFormat")
private fun formatHomeDate(timestampMillis: Long): String {
    return SimpleDateFormat("d MMMM, EEEE", Locale("ru")).format(Date(timestampMillis))
}

@SuppressLint("SimpleDateFormat")
private fun formatAnalysisDate(timestampMillis: Long): String {
    return SimpleDateFormat("d MMMM yyyy", Locale("ru")).format(Date(timestampMillis))
}