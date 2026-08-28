package com.example.healthreminderapp.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.healthreminderapp.HealthReminderApplication
import com.example.healthreminderapp.data.repository.HealthRepository
import com.example.healthreminderapp.data.repository.MedicationRepository

class HomeViewModelFactory(
    private val application: HealthReminderApplication
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val medicationRepository = MedicationRepository(application.database)
            val healthRepository = HealthRepository(application.database)

            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                application = application,
                medicationRepository = medicationRepository,
                healthRepository = healthRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}