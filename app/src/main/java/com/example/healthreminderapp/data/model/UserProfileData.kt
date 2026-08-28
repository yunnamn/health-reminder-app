package com.example.healthreminderapp.data.model

import com.example.healthreminderapp.data.local.entity.ProfileSex

data class UserProfileData(
    val name: String?,
    val sex: ProfileSex?,
    val birthDateMillis: Long?,
    val heightCm: Int?
)