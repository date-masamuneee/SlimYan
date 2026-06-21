package com.example.slimyan.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 固定筋トレプログラムの1種目（汎用：ユーザーが定義）。 */
@Entity(tableName = "workout_program_item")
data class WorkoutProgramItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    val targetSets: Int,
    val repCeiling: Int,    // この回数に全セット到達したら重量UP
    val sortOrder: Int = 0,
)
