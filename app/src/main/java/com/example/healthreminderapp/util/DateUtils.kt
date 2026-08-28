package com.example.healthreminderapp.util

import java.util.Calendar

const val DAY_MILLIS: Long = 24L * 60L * 60L * 1000L

fun startOfDay(timestampMillis: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestampMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

fun addDays(timestampMillis: Long, days: Int): Long {
    return startOfDay(timestampMillis) + days * DAY_MILLIS
}

fun getDayOfWeek(timestampMillis: Long): Int {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestampMillis
    }
    return calendar.get(Calendar.DAY_OF_WEEK)
}

fun isWithinEditableLoggingWindow(selectedDateMillis: Long): Boolean {
    val today = startOfDay(System.currentTimeMillis())
    val earliestEditableDay = addDays(today, -6)
    return selectedDateMillis in earliestEditableDay..today
}