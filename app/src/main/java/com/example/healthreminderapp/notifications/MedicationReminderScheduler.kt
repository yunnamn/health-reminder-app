package com.example.healthreminderapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.example.healthreminderapp.HealthReminderApplication
import com.example.healthreminderapp.data.model.ScheduledDose
import kotlin.math.abs

object MedicationReminderScheduler {

    const val EXTRA_MEDICATION_ID = "extra_medication_id"
    const val EXTRA_MEDICATION_NAME = "extra_medication_name"
    const val EXTRA_SCHEDULED_DATE_MILLIS = "extra_scheduled_date_millis"
    const val EXTRA_TIME_HOUR = "extra_time_hour"
    const val EXTRA_TIME_MINUTE = "extra_time_minute"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val EXTRA_REQUEST_CODE = "extra_request_code"

    private const val PREFS_NAME = "reminder_scheduler_prefs"
    private const val KEY_REQUEST_CODES = "request_codes"
    private const val KEY_SNOOZE_ENTRIES = "snooze_entries"

    private const val DAYS_AHEAD_TO_SCHEDULE = 7
    private const val SNOOZE_DELAY_MILLIS = 60_000L
    // private const val SNOOZE_DELAY_MILLIS = 30L * 60L * 1000L
    private const val ACTION_SHOW_REMINDER = "com.example.healthreminderapp.SHOW_REMINDER"

    suspend fun rescheduleAll(context: Context) {
        cancelAllScheduledAlarms(context)
        clearExpiredSnoozeStates(context)

        val application = context.applicationContext as HealthReminderApplication
        val upcomingDoses = application.medicationRepository.getUpcomingScheduledDoses(
            daysAhead = DAYS_AHEAD_TO_SCHEDULE
        )

        upcomingDoses.forEach { dose ->
            scheduleDoseReminder(context, dose)
        }
    }

    fun scheduleSnoozedReminder(
        context: Context,
        dose: ScheduledDose
    ) {
        val triggerAtMillis = System.currentTimeMillis() + SNOOZE_DELAY_MILLIS
        val requestCode = buildSnoozeRequestCode(dose)
        val notificationId = buildNotificationId(dose)

        val pendingIntent = buildAlarmPendingIntent(
            context = context,
            requestCode = requestCode,
            dose = dose,
            notificationId = notificationId
        )

        scheduleAlarm(
            context = context,
            triggerAtMillis = triggerAtMillis,
            pendingIntent = pendingIntent
        )

        storeRequestCode(context, requestCode)
        storeSnoozedUntil(context, dose, triggerAtMillis)
    }

    fun cancelDoseReminders(
        context: Context,
        dose: ScheduledDose
    ) {
        cancelAlarmByRequestCode(context, buildRegularRequestCode(dose))
        cancelAlarmByRequestCode(context, buildSnoozeRequestCode(dose))
        removeSnoozeState(context, dose)
        NotificationManagerCompat.from(context).cancel(buildNotificationId(dose))
    }

    fun removeDeliveredAlarmRequestCode(
        context: Context,
        requestCode: Int
    ) {
        removeStoredRequestCode(context, requestCode)
    }

    fun clearSnoozeState(
        context: Context,
        dose: ScheduledDose
    ) {
        removeSnoozeState(context, dose)
    }

    fun getSnoozedUntil(
        context: Context,
        dose: ScheduledDose
    ): Long? {
        clearExpiredSnoozeStates(context)

        val key = buildDoseKey(dose)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val entries = prefs.getStringSet(KEY_SNOOZE_ENTRIES, emptySet()).orEmpty()

        val entry = entries.firstOrNull { it.startsWith("$key|") } ?: return null
        val untilMillis = entry.substringAfterLast("|").toLongOrNull() ?: return null

        return if (untilMillis > System.currentTimeMillis()) {
            untilMillis
        } else {
            removeSnoozeState(context, dose)
            null
        }
    }

    fun isDoseSnoozed(
        context: Context,
        dose: ScheduledDose,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        val snoozedUntil = getSnoozedUntil(context, dose)
        return snoozedUntil != null && snoozedUntil > nowMillis
    }

    fun buildNotificationId(dose: ScheduledDose): Int {
        return safePositiveHash(
            "notification-${dose.medicationId}-${dose.scheduledDateMillis}-${dose.timeHour}-${dose.timeMinute}"
        )
    }

    fun putDoseExtras(intent: Intent, dose: ScheduledDose): Intent {
        intent.putExtra(EXTRA_MEDICATION_ID, dose.medicationId)
        intent.putExtra(EXTRA_MEDICATION_NAME, dose.medicationName)
        intent.putExtra(EXTRA_SCHEDULED_DATE_MILLIS, dose.scheduledDateMillis)
        intent.putExtra(EXTRA_TIME_HOUR, dose.timeHour)
        intent.putExtra(EXTRA_TIME_MINUTE, dose.timeMinute)
        return intent
    }

    fun doseFromIntent(intent: Intent?): ScheduledDose? {
        if (intent == null) return null

        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L)
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME).orEmpty()
        val scheduledDateMillis = intent.getLongExtra(EXTRA_SCHEDULED_DATE_MILLIS, -1L)
        val timeHour = intent.getIntExtra(EXTRA_TIME_HOUR, -1)
        val timeMinute = intent.getIntExtra(EXTRA_TIME_MINUTE, -1)

        if (
            medicationId < 0 ||
            medicationName.isBlank() ||
            scheduledDateMillis < 0 ||
            timeHour !in 0..23 ||
            timeMinute !in 0..59
        ) {
            return null
        }

        return ScheduledDose(
            medicationId = medicationId,
            medicationName = medicationName,
            scheduledDateMillis = scheduledDateMillis,
            timeHour = timeHour,
            timeMinute = timeMinute,
            isLogged = false,
            canEditLog = true,
            courseEndDateMillis = null,
            daysRemaining = null
        )
    }

    private fun scheduleDoseReminder(
        context: Context,
        dose: ScheduledDose
    ) {
        val triggerAtMillis = dose.toTriggerAtMillis()
        if (triggerAtMillis < System.currentTimeMillis()) return

        val requestCode = buildRegularRequestCode(dose)
        val notificationId = buildNotificationId(dose)

        val pendingIntent = buildAlarmPendingIntent(
            context = context,
            requestCode = requestCode,
            dose = dose,
            notificationId = notificationId
        )

        scheduleAlarm(
            context = context,
            triggerAtMillis = triggerAtMillis,
            pendingIntent = pendingIntent
        )

        storeRequestCode(context, requestCode)
    }

    private fun buildAlarmPendingIntent(
        context: Context,
        requestCode: Int,
        dose: ScheduledDose,
        notificationId: Int
    ): PendingIntent {
        val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
            action = ACTION_SHOW_REMINDER
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_REQUEST_CODE, requestCode)
            putDoseExtras(this, dose)
        }

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun scheduleAlarm(
        context: Context,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun cancelAllScheduledAlarms(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedCodes = prefs.getStringSet(KEY_REQUEST_CODES, emptySet()).orEmpty()

        storedCodes.forEach { codeString ->
            codeString.toIntOrNull()?.let { requestCode ->
                cancelAlarmByRequestCode(context, requestCode)
            }
        }

        prefs.edit().remove(KEY_REQUEST_CODES).apply()
    }

    private fun cancelAlarmByRequestCode(
        context: Context,
        requestCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
            action = ACTION_SHOW_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }

        removeStoredRequestCode(context, requestCode)
    }

    private fun storeRequestCode(context: Context, requestCode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_REQUEST_CODES, emptySet()).orEmpty().toMutableSet()
        current.add(requestCode.toString())
        prefs.edit().putStringSet(KEY_REQUEST_CODES, current).apply()
    }

    private fun removeStoredRequestCode(context: Context, requestCode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_REQUEST_CODES, emptySet()).orEmpty().toMutableSet()
        current.remove(requestCode.toString())
        prefs.edit().putStringSet(KEY_REQUEST_CODES, current).apply()
    }

    private fun storeSnoozedUntil(
        context: Context,
        dose: ScheduledDose,
        untilMillis: Long
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_SNOOZE_ENTRIES, emptySet()).orEmpty().toMutableSet()

        val doseKey = buildDoseKey(dose)
        current.removeAll { it.startsWith("$doseKey|") }
        current.add("$doseKey|$untilMillis")

        prefs.edit().putStringSet(KEY_SNOOZE_ENTRIES, current).apply()
    }

    private fun removeSnoozeState(
        context: Context,
        dose: ScheduledDose
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_SNOOZE_ENTRIES, emptySet()).orEmpty().toMutableSet()

        val doseKey = buildDoseKey(dose)
        current.removeAll { it.startsWith("$doseKey|") }

        prefs.edit().putStringSet(KEY_SNOOZE_ENTRIES, current).apply()
    }

    private fun clearExpiredSnoozeStates(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_SNOOZE_ENTRIES, emptySet()).orEmpty().toMutableSet()
        val nowMillis = System.currentTimeMillis()

        val filtered = current.filter { entry ->
            val untilMillis = entry.substringAfterLast("|").toLongOrNull()
            untilMillis != null && untilMillis > nowMillis
        }.toMutableSet()

        if (filtered != current) {
            prefs.edit().putStringSet(KEY_SNOOZE_ENTRIES, filtered).apply()
        }
    }

    private fun buildDoseKey(dose: ScheduledDose): String {
        return "${dose.medicationId}_${dose.scheduledDateMillis}_${dose.timeHour}_${dose.timeMinute}"
    }

    private fun buildRegularRequestCode(dose: ScheduledDose): Int {
        return safePositiveHash(
            "regular-${dose.medicationId}-${dose.scheduledDateMillis}-${dose.timeHour}-${dose.timeMinute}"
        )
    }

    private fun buildSnoozeRequestCode(dose: ScheduledDose): Int {
        return safePositiveHash(
            "snooze-${dose.medicationId}-${dose.scheduledDateMillis}-${dose.timeHour}-${dose.timeMinute}"
        )
    }

    private fun ScheduledDose.toTriggerAtMillis(): Long {
        return scheduledDateMillis +
                timeHour * 60L * 60L * 1000L +
                timeMinute * 60L * 1000L
    }

    private fun safePositiveHash(value: String): Int {
        val hash = value.hashCode()
        return if (hash == Int.MIN_VALUE) 0 else abs(hash)
    }
}