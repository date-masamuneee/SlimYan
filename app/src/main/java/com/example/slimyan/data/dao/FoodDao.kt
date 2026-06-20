package com.example.slimyan.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.slimyan.data.entity.Food
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM food ORDER BY isFavorite DESC, name ASC")
    fun getAll(): Flow<List<Food>>

    @Query("SELECT * FROM food WHERE name LIKE '%' || :query || '%' ORDER BY isFavorite DESC, name ASC")
    fun search(query: String): Flow<List<Food>>

    @Query("SELECT * FROM food WHERE id = :id")
    suspend fun getById(id: Long): Food?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(food: Food): Long

    @Update
    suspend fun update(food: Food)

    @Delete
    suspend fun delete(food: Food)
}
