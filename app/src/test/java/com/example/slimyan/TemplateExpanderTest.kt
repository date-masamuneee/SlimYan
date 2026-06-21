package com.example.slimyan

import com.example.slimyan.data.TemplateExpander
import com.example.slimyan.data.entity.Food
import com.example.slimyan.data.entity.MealTemplateItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplateExpanderTest {

    private val rice = Food(id = 1, name = "ご飯", calories = 252f, protein = 3.8f, fat = 0.5f, carb = 55.7f, servingGrams = 150f)
    private val foods = mapOf(1L to rice)

    @Test
    fun `テンプレ品目を当日のMealEntryに変換しスナップショットする`() {
        val items = listOf(
            MealTemplateItem(dayOfWeek = 1, mealSlot = "lunch", foodId = 1, grams = 300f)
        )
        val entries = TemplateExpander.expand(items, foods, dateEpochDay = 20000L)

        assertEquals(1, entries.size)
        val e = entries.first()
        assertEquals(20000L, e.dateEpochDay)
        assertEquals("lunch", e.mealSlot)
        assertEquals(1L, e.foodId)
        assertEquals("ご飯", e.foodName)          // 名前スナップショット
        assertEquals(300f, e.grams, 0.01f)
        assertEquals(504f, e.calories, 0.01f)     // カロリースナップショット
        assertTrue(e.isPlanned)
        assertTrue(!e.isChecked)
    }

    @Test
    fun `存在しない食材IDはスキップされる`() {
        val items = listOf(
            MealTemplateItem(dayOfWeek = 1, mealSlot = "lunch", foodId = 1, grams = 150f),
            MealTemplateItem(dayOfWeek = 1, mealSlot = "dinner", foodId = 99, grams = 100f),
        )
        val entries = TemplateExpander.expand(items, foods, dateEpochDay = 20000L)
        assertEquals(1, entries.size)
        assertEquals("lunch", entries.first().mealSlot)
    }

    @Test
    fun `空なら空を返す`() {
        assertEquals(0, TemplateExpander.expand(emptyList(), foods, 20000L).size)
    }
}
