package com.example.slimyan.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food")
data class Food(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    // すべて一食分（servingGrams 相当量）あたりの値
    val calories: Float,
    val protein: Float,
    val fat: Float,
    val carb: Float,
    val servingGrams: Float = 100f,
    val isFavorite: Boolean = false,
)
