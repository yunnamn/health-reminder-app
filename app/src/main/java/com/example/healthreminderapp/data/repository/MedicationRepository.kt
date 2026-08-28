package com.example.healthreminderapp.data.repository

import androidx.room.withTransaction
import com.example.healthreminderapp.data.local.AppDatabase
import com.example.healthreminderapp.data.local.entity.MedicationEntity
import com.example.healthreminderapp.data.local.entity.MedicationLogEntity
import com.example.healthreminderapp.data.local.entity.MedicationScheduleEntity
import com.example.healthreminderapp.data.local.entity.ScheduleType
import com.example.healthreminderapp.data.local.relation.MedicationWithSchedules
import com.example.healthreminderapp.data.model.NewMedicationData
import com.example.healthreminderapp.data.model.ScheduledDose
import com.example.healthreminderapp.data.model.WeekDay
import com.example.healthreminderapp.util.DAY_MILLIS
import com.example.healthreminderapp.util.addDays
import com.example.healthreminderapp.util.getDayOfWeek
import com.example.healthreminderapp.util.isWithinEditableLoggingWindow
import com.example.healthreminderapp.util.startOfDay
import kotlinx.coroutines.flow.Flow

class MedicationRepository(
    private val database: AppDatabase
) {
    private val medicationDao = database.medicationDao()
    private val medicationScheduleDao = database.medicationScheduleDao()
    private val medicationLogDao = database.medicationLogDao()

    fun getAllMedicationsWithSchedules(): Flow<List<MedicationWithSchedules>> {
        return medicationDao.getAllMedicationsWithSchedules()
    }

    fun getLogsForDate(selectedDateMillis: Long): Flow<List<MedicationLogEntity>> {
        return medicationLogDao.getLogsForDate(startOfDay(selectedDateMillis))
    }

    suspend fun addMedication(newMedication: NewMedicationData) {
        database.withTransaction {
            val startDateMillis = startOfDay(System.currentTimeMillis())

            val endDateMillis = newMedication.durationDays
                ?.takeIf { it > 0 }
                ?.let { days ->
                    startDateMillis + days * DAY_MILLIS
                }

            val medicationId = medicationDao.insertMedication(
                MedicationEntity(
                    name = newMedication.name,
                    type = newMedication.type,
                    strengthAmount = newMedication.strengthAmount,
                    strengthUnit = newMedication.strengthUnit,
                    startDateMillis = startDateMillis,
                    endDateMillis = endDateMillis,
                    isActive = true
                )
            )

            val scheduleEntities = newMedication.times.map { time ->
                MedicationScheduleEntity(
                    medicationId = medicationId,
                    scheduleType = newMedication.scheduleType,
                    timeHour = time.hour,
                    timeMinute = time.minute,
                    monday = newMedication.scheduleType == ScheduleType.SPECIFIC_DAYS &&
                            newMedication.selectedDays.contains(WeekDay.MONDAY),
                    tuesday = newMedication.scheduleType == ScheduleType.SPECIFIC_DAYS &&
                            newMedication.selectedDays.contains(WeekDay.TUESDAY),
                    wednesday = newMedication.scheduleType == ScheduleType.SPECIFIC_DAYS &&
                            newMedication.selectedDays.contains(WeekDay.WEDNESDAY),
                    thursday = newMedication.scheduleType == ScheduleType.SPECIFIC_DAYS &&
                            newMedication.selectedDays.contains(WeekDay.THURSDAY),
                    friday = newMedication.scheduleType == ScheduleType.SPECIFIC_DAYS &&
                            newMedication.selectedDays.contains(WeekDay.FRIDAY),
                    saturday = newMedication.scheduleType == ScheduleType.SPECIFIC_DAYS &&
                            newMedication.selectedDays.contains(WeekDay.SATURDAY),
                    sunday = newMedication.scheduleType == ScheduleType.SPECIFIC_DAYS &&
                            newMedication.selectedDays.contains(WeekDay.SUNDAY)
                )
            }

            medicationScheduleDao.insertSchedules(scheduleEntities)
        }
    }

    suspend fun updateMedication(
        medicationId: Long,
        originalMedication: MedicationEntity,
        updatedMedication: NewMedicationData
    ) {
        database.withTransaction {
            val endDateMillis = updatedMedication.durationDays
                ?.takeIf { it > 0 }
                ?.let { days ->
                    val startDateMillis = originalMedication.startDateMillis
                        ?.let(::startOfDay)
                        ?: startOfDay(System.currentTimeMillis())

                    startDateMillis + days * DAY_MILLIS
                }

            medicationDao.updateMedication(
                originalMedication.copy(
                    id = medicationId,
                    name = updatedMedication.name,
                    type = updatedMedication.type,
                    strengthAmount = updatedMedication.strengthAmount,
                    strengthUnit = updatedMedication.strengthUnit,
                    endDateMillis = endDateMillis,
                    isActive = true
                )
            )

            medicationScheduleDao.deleteSchedulesForMedication(medicationId)

            val newScheduleEntities = updatedMedication.times.map { time ->
                MedicationScheduleEntity(
                    medicationId = medicationId,
                    scheduleType = updatedMedication.scheduleType,
                    timeHour = time.hour,
                    timeMinute = time.minute,
                    monday = updatedMedication.scheduleType == ScheduleType.SPECIFIC_DAYS &&
                            updatedMedication.selectedDays.contains(WeekDay.MONDAY),
                    tuesday = updatedMedication.scheduleType == ScheduleType.SPECIFIC_DAYS &&
                            updatedMedication.selectedDays.contains(WeekDay.TUESDAY),
                    wednesday = updatedMedication.scheduleType == ScheduleType.SPECIFIC_DAYS &&
                            updatedMedication.selectedDays.contains(WeekDay.WEDNESDAY),
                    thursday = updatedMedication.scheduleType == ScheduleType.SPECIFIC_DAYS &&
                            updatedMedication.selectedDays.contains(WeekDay.THURSDAY),
                    friday = updatedMedication.scheduleType == ScheduleType.SPECIFIC_DAYS &&
                            updatedMedication.selectedDays.contains(WeekDay.FRIDAY),
                    saturday = updatedMedication.scheduleType == ScheduleType.SPECIFIC_DAYS &&
                            updatedMedication.selectedDays.contains(WeekDay.SATURDAY),
                    sunday = updatedMedication.scheduleType == ScheduleType.SPECIFIC_DAYS &&
                            updatedMedication.selectedDays.contains(WeekDay.SUNDAY)
                )
            }

            medicationScheduleDao.insertSchedules(newScheduleEntities)
        }
    }

    suspend fun deleteMedication(medication: MedicationEntity) {
        medicationDao.deleteMedication(medication)
    }

    suspend fun markDoseAsTaken(dose: ScheduledDose) {
        medicationLogDao.insertLog(
            MedicationLogEntity(
                medicationId = dose.medicationId,
                scheduledDateMillis = dose.scheduledDateMillis,
                timeHour = dose.timeHour,
                timeMinute = dose.timeMinute
            )
        )
    }

    suspend fun unmarkDoseAsTaken(dose: ScheduledDose) {
        medicationLogDao.deleteLog(
            medicationId = dose.medicationId,
            scheduledDateMillis = dose.scheduledDateMillis,
            timeHour = dose.timeHour,
            timeMinute = dose.timeMinute
        )
    }

    suspend fun isDoseLogged(dose: ScheduledDose): Boolean {
        return medicationLogDao.hasLog(
            medicationId = dose.medicationId,
            scheduledDateMillis = dose.scheduledDateMillis,
            timeHour = dose.timeHour,
            timeMinute = dose.timeMinute
        )
    }

    suspend fun getUpcomingScheduledDoses(
        daysAhead: Int = 7,
        fromTimeMillis: Long = System.currentTimeMillis()
    ): List<ScheduledDose> {
        val medications = medicationDao.getAllMedicationsWithSchedulesOnce()
        val result = mutableListOf<ScheduledDose>()

        for (dayOffset in 0 until daysAhead) {
            val dayStartMillis = addDays(fromTimeMillis, dayOffset)
            val logs = medicationLogDao.getLogsForDateOnce(dayStartMillis)

            val dosesForDay = buildScheduledDosesForDate(
                selectedDateMillis = dayStartMillis,
                medications = medications,
                logs = logs
            )

            result += dosesForDay.filter { dose ->
                val triggerAtMillis = dose.scheduledDateMillis +
                        dose.timeHour * 60L * 60L * 1000L +
                        dose.timeMinute * 60L * 1000L

                !dose.isLogged && triggerAtMillis >= fromTimeMillis
            }
        }

        return result.sortedWith(
            compareBy<ScheduledDose>(
                { it.scheduledDateMillis },
                { it.timeHour },
                { it.timeMinute },
                { it.medicationName }
            )
        )
    }

    fun buildScheduledDosesForDate(
        selectedDateMillis: Long,
        medications: List<MedicationWithSchedules>,
        logs: List<MedicationLogEntity>
    ): List<ScheduledDose> {
        val selectedDayStart = startOfDay(selectedDateMillis)
        val selectedDayOfWeek = getDayOfWeek(selectedDayStart)
        val canEditLog = isWithinEditableLoggingWindow(selectedDayStart)

        val loggedDoseKeys = logs.map {
            DoseLogKey(
                medicationId = it.medicationId,
                timeHour = it.timeHour,
                timeMinute = it.timeMinute
            )
        }.toSet()

        return medications.flatMap { medicationWithSchedules ->
            val medication = medicationWithSchedules.medication
            val schedules = medicationWithSchedules.schedules

            if (!isMedicationActiveOnDay(medication, selectedDayStart)) {
                emptyList()
            } else {
                val courseEndDateMillis = medication.endDateMillis?.let { endMillis ->
                    startOfDay(endMillis - 1)
                }

                val daysRemaining = courseEndDateMillis?.let { courseEnd ->
                    if (selectedDayStart <= courseEnd) {
                        (((courseEnd - selectedDayStart) / DAY_MILLIS) + 1).toInt()
                    } else {
                        null
                    }
                }

                schedules
                    .filter { scheduleMatchesDate(it, selectedDayOfWeek) }
                    .map { schedule ->
                        val key = DoseLogKey(
                            medicationId = medication.id,
                            timeHour = schedule.timeHour,
                            timeMinute = schedule.timeMinute
                        )

                        ScheduledDose(
                            medicationId = medication.id,
                            medicationName = medication.name,
                            scheduledDateMillis = selectedDayStart,
                            timeHour = schedule.timeHour,
                            timeMinute = schedule.timeMinute,
                            isLogged = loggedDoseKeys.contains(key),
                            canEditLog = canEditLog,
                            courseEndDateMillis = courseEndDateMillis,
                            daysRemaining = daysRemaining
                        )
                    }
            }
        }.sortedWith(
            compareBy<ScheduledDose>({ it.timeHour }, { it.timeMinute }, { it.medicationName })
        )
    }

    private fun isMedicationActiveOnDay(
        medication: MedicationEntity,
        selectedDayStart: Long
    ): Boolean {
        val courseStart = medication.startDateMillis?.let(::startOfDay) ?: return false

        if (selectedDayStart < courseStart) return false

        val courseEndExclusive = medication.endDateMillis
        if (courseEndExclusive != null) {
            val lastCourseDay = startOfDay(courseEndExclusive - 1)
            if (selectedDayStart > lastCourseDay) return false
        }

        return medication.isActive
    }

    private fun scheduleMatchesDate(
        schedule: MedicationScheduleEntity,
        selectedDayOfWeek: Int
    ): Boolean {
        return when (schedule.scheduleType) {
            ScheduleType.DAILY -> true
            ScheduleType.SPECIFIC_DAYS -> {
                when (selectedDayOfWeek) {
                    java.util.Calendar.MONDAY -> schedule.monday
                    java.util.Calendar.TUESDAY -> schedule.tuesday
                    java.util.Calendar.WEDNESDAY -> schedule.wednesday
                    java.util.Calendar.THURSDAY -> schedule.thursday
                    java.util.Calendar.FRIDAY -> schedule.friday
                    java.util.Calendar.SATURDAY -> schedule.saturday
                    java.util.Calendar.SUNDAY -> schedule.sunday
                    else -> false
                }
            }
        }
    }

    private data class DoseLogKey(
        val medicationId: Long,
        val timeHour: Int,
        val timeMinute: Int
    )
}