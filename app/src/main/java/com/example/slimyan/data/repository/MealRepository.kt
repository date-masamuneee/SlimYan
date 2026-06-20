package com.example.slimyan.data.repository

import com.example.slimyan.data.dao.MealEntryDao
import com.example.slimyan.data.entity.MealEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealRepository @Inject constructor(
    private val dao: MealEntryDao,
) {
    fun getForDay(epochDay: Long): Flow<List<MealEntry>> = dao.getForDay(epochDay)

    fun checkedCaloriesForDay(epochDay: Long): Flow<Float> = dao.checkedCaloriesForDay(epochDay)

    suspend fun add(entry: MealEntry): Long = dao.insert(entry)

    suspend fun update(entry: MealEntry) = dao.update(entry)

    suspend fun delete(entry: MealEntry) = dao.delete(entry)
}
