package com.example.healthreminderapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "name")
    val name: String?,

    @ColumnInfo(name = "sex")
    val sex: ProfileSex?,

    @ColumnInfo(name = "birth_date_millis")
    val birthDateMillis: Long?,

    @ColumnInfo(name = "height_cm")
    val heightCm: Int?
)