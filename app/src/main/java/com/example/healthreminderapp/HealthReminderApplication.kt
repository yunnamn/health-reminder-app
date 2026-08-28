package com.example.healthreminderapp

import android.app.Application
import com.example.healthreminderapp.data.local.AppDatabase
import com.example.healthreminderapp.data.repository.HealthRepository
import com.example.healthreminderapp.data.repository.MedicationRepository
import com.example.healthreminderapp.notifications.NotificationHelper

class HealthReminderApplication : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val medicationRepository: MedicationRepository by lazy {
        MedicationRepository(database)
    }

    val healthRepository: HealthRepository by lazy {
        HealthRepository(database)
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}