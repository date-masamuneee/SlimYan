package com.example.slimyan.data.repository

import com.example.slimyan.data.dao.MealTemplateDao
import com.example.slimyan.data.entity.MealTemplateItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealTemplateRepository @Inject constructor(
    private val dao: MealTemplateDao,
) {
    fun getAll(): Flow<List<MealTemplateItem>> = dao.getAll()

    fun getForDay(dayOfWeek: Int): Flow<List<MealTemplateItem>> = dao.getForDay(dayOfWeek)

    suspend fun getForDayOnce(dayOfWeek: Int): List<MealTemplateItem> = dao.getForDayOnce(dayOfWeek)

    suspend fun add(item: MealTemplateItem): Long = dao.insert(item)

    suspend fun update(item: MealTemplateItem) = dao.update(item)

    suspend fun delete(item: MealTemplateItem) = dao.delete(item)

    /** テンプレ全体を置き換える（AI監査の修正案適用）。 */
    suspend fun replaceAll(items: List<MealTemplateItem>) {
        dao.deleteAll()
        dao.insertAll(items)
    }
}
