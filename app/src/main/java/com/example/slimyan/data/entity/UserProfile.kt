package com.example.slimyan.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val heightCm: Float = 170f,
    val weightKg: Float = 65f,
    val age: Int = 30,
    val sex: String = "male",
    val activityLevel: String = "moderate",
    val goalWeightKg: Float = 60f,
    val goalDate: Long = 0L,
    val dailyCalorieTarget: Int = 0,
    val proteinTargetG: Float = 0f,
    val fatTargetG: Float = 0f,
    val carbTargetG: Float = 0f,
)
