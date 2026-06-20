package com.example.slimyan.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.slimyan.data.entity.SetEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface SetEntryDao {
    @Query("SELECT * FROM set_entry WHERE dateEpochDay = :day ORDER BY exerciseId ASC, setOrder ASC")
    fun getForDay(day: Long): Flow<List<SetEntry>>

    @Query("SELECT * FROM set_entry WHERE exerciseId = :exerciseId ORDER BY dateEpochDay DESC, setOrder ASC LIMIT 20")
    fun getRecentSetsForExercise(exerciseId: Long): Flow<List<SetEntry>>

    @Insert
    suspend fun insert(entry: SetEntry): Long

    @Delete
    suspend fun delete(entry: SetEntry)
}
