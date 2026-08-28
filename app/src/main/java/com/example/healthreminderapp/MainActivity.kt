package com.example.healthreminderapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.healthreminderapp.navigation.AppDestinations
import com.example.healthreminderapp.notifications.MedicationReminderScheduler
import com.example.healthreminderapp.notifications.NotificationHelper
import com.example.healthreminderapp.screens.health.HealthEntryAction
import com.example.healthreminderapp.screens.health.HealthScreen
import com.example.healthreminderapp.screens.health.HealthSection
import com.example.healthreminderapp.screens.home.HomeScreen
import com.example.healthreminderapp.screens.medications.MedicationsScreen
import com.example.healthreminderapp.screens.schedule.ScheduleScreen
import com.example.healthreminderapp.ui.theme.HealthReminderAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            lifecycleScope.launch {
                MedicationReminderScheduler.rescheduleAll(this@MainActivity)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

        enableEdgeToEdge()
        setContent {
            HealthReminderAppTheme {
                HealthReminderApp()
            }
        }

        lifecycleScope.launch {
            MedicationReminderScheduler.rescheduleAll(this@MainActivity)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationHelper.hasNotificationPermission(this)
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun HealthReminderApp() {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        AppDestinations.Home,
        AppDestinations.Medications,
        AppDestinations.Schedule,
        AppDestinations.Health
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentDestination =
                    navController.currentBackStackEntryAsState().value?.destination

                bottomNavItems.forEach { destination ->
                    val icon = when (destination) {
                        AppDestinations.Home -> Icons.Default.Home
                        AppDestinations.Medications -> Icons.Default.Medication
                        AppDestinations.Schedule -> Icons.Default.DateRange
                        AppDestinations.Health -> Icons.Default.Favorite
                    }

                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { navDestination ->
                            navDestination.route?.startsWith(destination.route) == true
                        } == true,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = destination.title
                            )
                        },
                        label = {
                            Text(text = destination.title)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestinations.Home.route) {
                HomeScreen(
                    onOpenSchedule = {
                        navController.navigate(AppDestinations.Schedule.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenAddIndicator = {
                        navController.navigate(
                            AppDestinations.Health.createRoute(
                                section = HealthSection.INDICATORS,
                                action = HealthEntryAction.ADD_INDICATOR
                            )
                        ) {
                            launchSingleTop = true
                        }
                    },
                    onOpenAddAnalysis = {
                        navController.navigate(
                            AppDestinations.Health.createRoute(
                                section = HealthSection.ANALYSES,
                                action = HealthEntryAction.ADD_ANALYSIS
                            )
                        ) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(AppDestinations.Medications.route) {
                MedicationsScreen()
            }

            composable(AppDestinations.Schedule.route) {
                ScheduleScreen()
            }

            composable(
                route = AppDestinations.Health.routePattern,
                arguments = listOf(
                    navArgument(AppDestinations.Health.sectionArg) {
                        type = NavType.StringType
                        defaultValue = HealthSection.PROFILE.name
                    },
                    navArgument(AppDestinations.Health.actionArg) {
                        type = NavType.StringType
                        defaultValue = HealthEntryAction.NONE.name
                    }
                )
            ) { backStackEntry ->
                val sectionName = backStackEntry.arguments
                    ?.getString(AppDestinations.Health.sectionArg)
                val actionName = backStackEntry.arguments
                    ?.getString(AppDestinations.Health.actionArg)

                val initialSection = HealthSection.entries.firstOrNull {
                    it.name == sectionName
                } ?: HealthSection.PROFILE

                val initialAction = HealthEntryAction.entries.firstOrNull {
                    it.name == actionName
                } ?: HealthEntryAction.NONE

                HealthScreen(
                    initialSection = initialSection,
                    initialAction = initialAction
                )
            }
        }
    }
}