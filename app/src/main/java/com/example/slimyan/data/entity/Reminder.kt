package com.example.slimyan.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 食事・筋トレ共通のリマインド。 */
@Entity(tableName = "reminder")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,            // "meal" | "workout"
    val mealSlot: String?,       // type=meal のとき枠。workout は null
    val label: String,           // 通知文言
    val hour: Int,               // 0-23
    val minute: Int,             // 0-59
    val daysOfWeekMask: Int,     // bit0=月 .. bit6=日
    val enabled: Boolean = true,
)
