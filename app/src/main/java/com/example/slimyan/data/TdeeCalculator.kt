package com.example.slimyan.data

import java.time.LocalDate

object TdeeCalculator {
    fun bmr(weightKg: Float, heightCm: Float, age: Int, sex: String): Float =
        if (sex == "male")
            88.362f + (13.397f * weightKg) + (4.799f * heightCm) - (5.677f * age)
        else
            447.593f + (9.247f * weightKg) + (3.098f * heightCm) - (4.330f * age)

    fun tdee(bmr: Float, activityLevel: String): Float {
        val multiplier = when (activityLevel) {
            "sedentary" -> 1.2f
            "light" -> 1.375f
            "active" -> 1.725f
            "very_active" -> 1.9f
            else -> 1.55f
        }
        return bmr * multiplier
    }

    fun dailyTarget(
        tdee: Float,
        currentWeightKg: Float,
        goalWeightKg: Float,
        goalEpochDay: Long,
    ): Int {
        if (goalEpochDay <= 0L) return tdee.toInt()
        val daysLeft = goalEpochDay - LocalDate.now().toEpochDay()
        if (daysLeft <= 0) return tdee.toInt()
        val totalKcalDeficit = (currentWeightKg - goalWeightKg) * 7200f
        val dailyDeficit = totalKcalDeficit / daysLeft
        return (tdee - dailyDeficit).coerceAtLeast(1200f).toInt()
    }
}
