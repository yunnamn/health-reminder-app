package com.example.healthreminderapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.healthreminderapp.data.local.dao.AnalysisDao
import com.example.healthreminderapp.data.local.dao.AnalysisResultDao
import com.example.healthreminderapp.data.local.dao.DailyHealthRecordDao
import com.example.healthreminderapp.data.local.dao.MedicationDao
import com.example.healthreminderapp.data.local.dao.MedicationLogDao
import com.example.healthreminderapp.data.local.dao.MedicationScheduleDao
import com.example.healthreminderapp.data.local.dao.UserProfileDao
import com.example.healthreminderapp.data.local.entity.AnalysisEntity
import com.example.healthreminderapp.data.local.entity.AnalysisResultEntity
import com.example.healthreminderapp.data.local.entity.DailyHealthRecordEntity
import com.example.healthreminderapp.data.local.entity.MedicationEntity
import com.example.healthreminderapp.data.local.entity.MedicationLogEntity
import com.example.healthreminderapp.data.local.entity.MedicationScheduleEntity
import com.example.healthreminderapp.data.local.entity.UserProfileEntity

@Database(
    entities = [
        MedicationEntity::class,
        MedicationScheduleEntity::class,
        MedicationLogEntity::class,
        UserProfileEntity::class,
        DailyHealthRecordEntity::class,
        AnalysisEntity::class,
        AnalysisResultEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(
    MedicationTypeConverters::class,
    ProfileTypeConverters::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao
    abstract fun medicationScheduleDao(): MedicationScheduleDao
    abstract fun medicationLogDao(): MedicationLogDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun dailyHealthRecordDao(): DailyHealthRecordDao
    abstract fun analysisDao(): AnalysisDao
    abstract fun analysisResultDao(): AnalysisResultDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_reminder_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}