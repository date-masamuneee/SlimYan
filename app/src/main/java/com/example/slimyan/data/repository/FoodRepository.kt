package com.example.slimyan.data.repository

import com.example.slimyan.data.dao.FoodDao
import com.example.slimyan.data.entity.Food
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepository @Inject constructor(
    private val dao: FoodDao,
) {
    fun getAll(): Flow<List<Food>> = dao.getAll()

    fun search(query: String): Flow<List<Food>> = dao.search(query)

    suspend fun getById(id: Long): Food? = dao.getById(id)

    suspend fun save(food: Food): Long = dao.insert(food)

    suspend fun update(food: Food) = dao.update(food)

    suspend fun delete(food: Food) = dao.delete(food)
}
