package com.example.slimyan.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.slimyan.data.entity.MealEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MealEntryDao {
    @Query("SELECT * FROM meal_entry WHERE dateEpochDay = :day ORDER BY mealSlot ASC, createdAt ASC")
    fun getForDay(day: Long): Flow<List<MealEntry>>

    @Query("SELECT COALESCE(SUM(calories), 0) FROM meal_entry WHERE dateEpochDay = :day AND isChecked = 1")
    fun checkedCaloriesForDay(day: Long): Flow<Float>

    @Insert
    suspend fun insert(entry: MealEntry): Long

    @Update
    suspend fun update(entry: MealEntry)

    @Delete
    suspend fun delete(entry: MealEntry)
}
