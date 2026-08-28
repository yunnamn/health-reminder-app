package com.example.healthreminderapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.healthreminderapp.HealthReminderApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestCode = intent?.getIntExtra(
                    MedicationReminderScheduler.EXTRA_REQUEST_CODE,
                    -1
                ) ?: -1

                if (requestCode >= 0) {
                    MedicationReminderScheduler.removeDeliveredAlarmRequestCode(
                        context = context,
                        requestCode = requestCode
                    )
                }

                val dose = MedicationReminderScheduler.doseFromIntent(intent) ?: return@launch
                val application = context.applicationContext as HealthReminderApplication

                MedicationReminderScheduler.clearSnoozeState(context, dose)

                val isAlreadyLogged = application.medicationRepository.isDoseLogged(dose)
                if (!isAlreadyLogged) {
                    NotificationHelper.showReminderNotification(context, dose)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}