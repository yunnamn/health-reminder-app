package com.example.healthreminderapp.screens.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.healthreminderapp.HealthReminderApplication
import com.example.healthreminderapp.data.repository.HealthRepository

class HealthViewModelFactory(
    private val application: HealthReminderApplication
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthViewModel::class.java)) {
            val repository = HealthRepository(application.database)
            @Suppress("UNCHECKED_CAST")
            return HealthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}