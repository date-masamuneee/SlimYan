package com.example.slimyan.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_entry")
data class MealEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val mealSlot: String,       // "breakfast" | "lunch" | "dinner" | "snack"
    val foodId: Long,
    val foodName: String,       // 記録時点のスナップショット
    val grams: Float,
    val calories: Float,        // 記録時点で計算してキャッシュ
    val isPlanned: Boolean = true,
    val isChecked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
