package com.example.healthreminderapp.navigation

import com.example.healthreminderapp.screens.health.HealthEntryAction
import com.example.healthreminderapp.screens.health.HealthSection

sealed class AppDestinations(val route: String, val title: String) {
    data object Home : AppDestinations("home", "Главная")
    data object Medications : AppDestinations("medications", "Лекарства")
    data object Schedule : AppDestinations("schedule", "Расписание")

    data object Health : AppDestinations("health", "Здоровье") {
        const val sectionArg = "section"
        const val actionArg = "action"

        const val routePattern =
            "health?$sectionArg={$sectionArg}&$actionArg={$actionArg}"

        fun createRoute(
            section: HealthSection = HealthSection.PROFILE,
            action: HealthEntryAction = HealthEntryAction.NONE
        ): String {
            return "$route?$sectionArg=${section.name}&$actionArg=${action.name}"
        }
    }
}