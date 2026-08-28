package com.example.healthreminderapp.screens.schedule

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import com.example.healthreminderapp.data.model.ScheduledDose
import com.example.healthreminderapp.util.isWithinEditableLoggingWindow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleScreen() {
    val application = LocalContext.current.applicationContext as HealthReminderApplication
    val viewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModelFactory(application)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScheduleContent(
        uiState = uiState,
        onPreviousDay = viewModel::goToPreviousDay,
        onNextDay = viewModel::goToNextDay,
        onToggleDoseLogged = viewModel::toggleDoseLogged,
        onClearInfoMessage = viewModel::clearInfoMessage,
        onClearErrorMessage = viewModel::clearErrorMessage
    )
}

@Composable
private fun ScheduleContent(
    uiState: ScheduleUiState,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToggleDoseLogged: (ScheduledDose) -> Unit,
    onClearInfoMessage: () -> Unit,
    onClearErrorMessage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Расписание",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Просмотр приёмов по дням.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onPreviousDay) {
                Text("← Предыдущий день")
            }

            TextButton(onClick = onNextDay) {
                Text("Следующий день →")
            }
        }

        Text(
            text = formatSelectedDate(uiState.selectedDateMillis),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = when {
                isWithinEditableLoggingWindow(uiState.selectedDateMillis) ->
                    "Можно отмечать приёмы за выбранный день."
                uiState.selectedDateMillis > System.currentTimeMillis() ->
                    "Будущие дни доступны только для просмотра."
                else ->
                    "Дни старше 7 дней доступны только для просмотра."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        uiState.infoMessage?.let {
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
                    Text(it)
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
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(it)
                    TextButton(onClick = onClearErrorMessage) {
                        Text("Скрыть")
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

        when {
            uiState.isLoading -> {
                Text("Загрузка...")
            }

            uiState.doses.isEmpty() -> {
                Text("На выбранный день приёмов нет.")
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.doses) { dose ->
                        ScheduledDoseCard(
                            dose = dose,
                            onToggleLogged = { onToggleDoseLogged(dose) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduledDoseCard(
    dose: ScheduledDose,
    onToggleLogged: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${formatTime(dose.timeHour, dose.timeMinute)} — ${dose.medicationName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = if (dose.isLogged) "Принято" else "Не отмечено",
                color = if (dose.isLogged) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(top = 8.dp)
            )

            dose.courseEndDateMillis?.let { endDate ->
                Text(
                    text = "Курс до: ${formatCourseDate(endDate)}",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            dose.daysRemaining?.let { days ->
                Text(
                    text = "Осталось: $days дн.",
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (dose.canEditLog) {
                Button(
                    onClick = onToggleLogged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text(
                        if (dose.isLogged) {
                            "Убрать отметку"
                        } else {
                            "Отметить как принято"
                        }
                    )
                }
            }
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    return "%02d:%02d".format(hour, minute)
}

@SuppressLint("SimpleDateFormat")
private fun formatSelectedDate(timestampMillis: Long): String {
    val formatter = SimpleDateFormat("d MMMM", Locale("ru"))
    return formatter.format(Date(timestampMillis))
}

@SuppressLint("SimpleDateFormat")
private fun formatCourseDate(timestampMillis: Long): String {
    val formatter = SimpleDateFormat("d MMMM", Locale("ru"))
    return formatter.format(Date(timestampMillis))
}