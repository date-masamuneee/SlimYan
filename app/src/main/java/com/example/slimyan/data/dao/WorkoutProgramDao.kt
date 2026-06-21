package com.example.slimyan.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.slimyan.data.entity.WorkoutProgramItem
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutProgramDao {
    @Query("SELECT * FROM workout_program_item ORDER BY sortOrder ASC, id ASC")
    fun getAll(): Flow<List<WorkoutProgramItem>>

    @Insert
    suspend fun insert(item: WorkoutProgramItem): Long

    @Update
    suspend fun update(item: WorkoutProgramItem)

    @Delete
    suspend fun delete(item: WorkoutProgramItem)
}
