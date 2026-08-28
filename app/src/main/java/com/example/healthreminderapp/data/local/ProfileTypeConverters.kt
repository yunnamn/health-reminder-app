package com.example.healthreminderapp.data.local

import androidx.room.TypeConverter
import com.example.healthreminderapp.data.local.entity.ProfileSex

class ProfileTypeConverters {

    @TypeConverter
    fun fromProfileSex(value: ProfileSex?): String? {
        return value?.name
    }

    @TypeConverter
    fun toProfileSex(value: String?): ProfileSex? {
        return value?.let(ProfileSex::valueOf)
    }
}