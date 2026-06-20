package com.example.slimyan.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "set_entry")
data class SetEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    val dateEpochDay: Long,
    val weight: Float,
    val reps: Int,
    val setOrder: Int,
    val createdAt: Long = System.currentTimeMillis(),
)
