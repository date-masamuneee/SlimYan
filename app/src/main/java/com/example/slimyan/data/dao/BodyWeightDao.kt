package com.example.slimyan.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.slimyan.data.entity.BodyWeight
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyWeightDao {
    @Query("SELECT * FROM body_weight ORDER BY dateEpochDay DESC")
    fun getAll(): Flow<List<BodyWeight>>

    @Query("SELECT * FROM body_weight WHERE dateEpochDay >= :fromDay ORDER BY dateEpochDay ASC")
    fun getSince(fromDay: Long): Flow<List<BodyWeight>>

    @Query("SELECT * FROM body_weight WHERE dateEpochDay = :day LIMIT 1")
    suspend fun getForDay(day: Long): BodyWeight?

    @Insert
    suspend fun insert(bodyWeight: BodyWeight)

    @Delete
    suspend fun delete(bodyWeight: BodyWeight)
}
