package com.example.slimyan.data.repository

import com.example.slimyan.data.dao.BodyWeightDao
import com.example.slimyan.data.entity.BodyWeight
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyWeightRepository @Inject constructor(
    private val dao: BodyWeightDao,
) {
    fun getAll(): Flow<List<BodyWeight>> = dao.getAll()

    fun getSince(fromEpochDay: Long): Flow<List<BodyWeight>> = dao.getSince(fromEpochDay)

    suspend fun getForDay(epochDay: Long): BodyWeight? = dao.getForDay(epochDay)

    suspend fun save(bodyWeight: BodyWeight) = dao.insert(bodyWeight)

    suspend fun delete(bodyWeight: BodyWeight) = dao.delete(bodyWeight)
}
