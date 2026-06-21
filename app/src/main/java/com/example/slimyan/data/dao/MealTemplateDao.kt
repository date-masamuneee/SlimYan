package com.example.slimyan.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.slimyan.data.entity.MealTemplateItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MealTemplateDao {
    @Query("SELECT * FROM meal_template_item ORDER BY dayOfWeek ASC, mealSlot ASC, sortOrder ASC")
    fun getAll(): Flow<List<MealTemplateItem>>

    @Query("SELECT * FROM meal_template_item WHERE dayOfWeek = :dayOfWeek ORDER BY mealSlot ASC, sortOrder ASC")
    fun getForDay(dayOfWeek: Int): Flow<List<MealTemplateItem>>

    @Query("SELECT * FROM meal_template_item WHERE dayOfWeek = :dayOfWeek")
    suspend fun getForDayOnce(dayOfWeek: Int): List<MealTemplateItem>

    @Insert
    suspend fun insert(item: MealTemplateItem): Long

    @Update
    suspend fun update(item: MealTemplateItem)

    @Delete
    suspend fun delete(item: MealTemplateItem)

    @Query("DELETE FROM meal_template_item")
    suspend fun deleteAll()

    @Insert
    suspend fun insertAll(items: List<MealTemplateItem>)
}
