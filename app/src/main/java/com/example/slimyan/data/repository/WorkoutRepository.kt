package com.example.slimyan.data.repository

import com.example.slimyan.data.dao.ExerciseDao
import com.example.slimyan.data.dao.SetEntryDao
import com.example.slimyan.data.entity.Exercise
import com.example.slimyan.data.entity.SetEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val setEntryDao: SetEntryDao,
) {
    fun getAllExercises(): Flow<List<Exercise>> = exerciseDao.getAll()

    suspend fun addExercise(exercise: Exercise): Long = exerciseDao.insert(exercise)

    suspend fun updateExercise(exercise: Exercise) = exerciseDao.update(exercise)

    suspend fun deleteExercise(exercise: Exercise) = exerciseDao.delete(exercise)

    fun getSetsForDay(epochDay: Long): Flow<List<SetEntry>> = setEntryDao.getForDay(epochDay)

    fun getRecentSetsForExercise(exerciseId: Long): Flow<List<SetEntry>> =
        setEntryDao.getRecentSetsForExercise(exerciseId)

    suspend fun addSet(entry: SetEntry): Long = setEntryDao.insert(entry)

    suspend fun deleteSet(entry: SetEntry) = setEntryDao.delete(entry)
}
