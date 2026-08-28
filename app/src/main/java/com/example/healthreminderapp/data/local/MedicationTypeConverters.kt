package com.example.healthreminderapp.data.local

import androidx.room.TypeConverter
import com.example.healthreminderapp.data.local.entity.MedicationType
import com.example.healthreminderapp.data.local.entity.ScheduleType

class MedicationTypeConverters {

    @TypeConverter
    fun fromMedicationType(type: MedicationType): String {
        return type.name
    }

    @TypeConverter
    fun toMedicationType(value: String): MedicationType {
        return MedicationType.valueOf(value)
    }

    @TypeConverter
    fun fromScheduleType(type: ScheduleType): String {
        return type.name
    }

    @TypeConverter
    fun toScheduleType(value: String): ScheduleType {
        return ScheduleType.valueOf(value)
    }
}