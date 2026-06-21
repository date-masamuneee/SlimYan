package com.example.slimyan.data.repository

import com.example.slimyan.data.dao.ReminderDao
import com.example.slimyan.data.entity.Reminder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val dao: ReminderDao,
) {
    fun getAll(): Flow<List<Reminder>> = dao.getAll()

    suspend fun getEnabled(): List<Reminder> = dao.getEnabled()

    suspend fun add(reminder: Reminder): Long = dao.insert(reminder)

    suspend fun update(reminder: Reminder) = dao.update(reminder)

    suspend fun delete(reminder: Reminder) = dao.delete(reminder)
}
