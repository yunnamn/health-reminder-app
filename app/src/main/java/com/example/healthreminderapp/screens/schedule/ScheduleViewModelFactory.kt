package com.example.healthreminderapp.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.healthreminderapp.HealthReminderApplication

class ScheduleViewModelFactory(
    private val application: HealthReminderApplication
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(
                application = application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}