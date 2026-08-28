package com.example.healthreminderapp.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.healthreminderapp.MainActivity
import com.example.healthreminderapp.R
import com.example.healthreminderapp.data.model.ScheduledDose

object NotificationHelper {

    const val CHANNEL_ID = "medication_reminders"
    private const val CHANNEL_NAME = "Напоминания о лекарствах"
    private const val CHANNEL_DESCRIPTION = "Уведомления о приёме лекарств"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showReminderNotification(
        context: Context,
        dose: ScheduledDose
    ) {
        if (!hasNotificationPermission(context)) return

        val notificationId = MedicationReminderScheduler.buildNotificationId(dose)
        val timeText = String.format("%02d:%02d", dose.timeHour, dose.timeMinute)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takenIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderActionReceiver.ACTION_MARK_TAKEN
            putExtra(MedicationReminderScheduler.EXTRA_NOTIFICATION_ID, notificationId)
            MedicationReminderScheduler.putDoseExtras(this, dose)
        }

        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 100_000,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderActionReceiver.ACTION_SNOOZE
            putExtra(MedicationReminderScheduler.EXTRA_NOTIFICATION_ID, notificationId)
            MedicationReminderScheduler.putDoseExtras(this, dose)
        }

        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 200_000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Пора принять лекарство")
            .setContentText("${dose.medicationName}, время: $timeText")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Лекарство: ${dose.medicationName}\nЗапланированное время: $timeText"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .addAction(0, "Принято", takenPendingIntent)
            .addAction(0, "Через 30 минут", snoozePendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}