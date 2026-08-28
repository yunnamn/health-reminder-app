package com.example.healthreminderapp.screens.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.healthreminderapp.HealthReminderApplication

class MedicationViewModelFactory(
    private val application: HealthReminderApplication
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicationViewModel(
                application = application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}