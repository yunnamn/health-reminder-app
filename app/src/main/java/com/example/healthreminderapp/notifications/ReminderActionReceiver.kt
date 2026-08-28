package com.example.healthreminderapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.healthreminderapp.HealthReminderApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dose = MedicationReminderScheduler.doseFromIntent(intent)
                val notificationId = intent?.getIntExtra(
                    MedicationReminderScheduler.EXTRA_NOTIFICATION_ID,
                    -1
                ) ?: -1

                if (dose != null) {
                    val application = context.applicationContext as HealthReminderApplication

                    when (intent?.action) {
                        ACTION_MARK_TAKEN -> {
                            application.medicationRepository.markDoseAsTaken(dose)
                            MedicationReminderScheduler.cancelDoseReminders(context, dose)
                        }

                        ACTION_SNOOZE -> {
                            MedicationReminderScheduler.cancelDoseReminders(context, dose)
                            MedicationReminderScheduler.scheduleSnoozedReminder(context, dose)
                        }
                    }
                }

                if (notificationId >= 0) {
                    NotificationManagerCompat.from(context).cancel(notificationId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_MARK_TAKEN = "com.example.healthreminderapp.ACTION_MARK_TAKEN"
        const val ACTION_SNOOZE = "com.example.healthreminderapp.ACTION_SNOOZE"
    }
}