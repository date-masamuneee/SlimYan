package com.example.slimyan

import com.example.slimyan.data.TemplateSummary
import com.example.slimyan.data.entity.Food
import com.example.slimyan.data.entity.MealTemplateItem
import org.junit.Assert.assertEquals
import org.junit.Test

class TemplateSummaryTest {

    private val rice = Food(id = 1, name = "ご飯", calories = 252f, protein = 3.8f, fat = 0.5f, carb = 55.7f, servingGrams = 150f)
    private val chicken = Food(id = 2, name = "サラダチキン", calories = 105f, protein = 26f, fat = 1.5f, carb = 1f, servingGrams = 100f)
    private val foods = mapOf(1L to rice, 2L to chicken)

    private fun item(foodId: Long, grams: Float) =
        MealTemplateItem(dayOfWeek = 1, mealSlot = "breakfast", foodId = foodId, grams = grams)

    @Test
    fun `複数品目の合計を集計する`() {
        val items = listOf(item(1, 150f), item(2, 100f)) // ご飯1杯 + チキン1個
        val t = TemplateSummary.totals(items, foods)
        assertEquals(357f, t.kcal, 0.01f)       // 252 + 105
        assertEquals(29.8f, t.protein, 0.01f)   // 3.8 + 26
    }

    @Test
    fun `グラム数に応じてスケールする`() {
        val items = listOf(item(1, 300f)) // ご飯2杯分
        val t = TemplateSummary.totals(items, foods)
        assertEquals(504f, t.kcal, 0.01f)
    }

    @Test
    fun `存在しない食材IDは無視される`() {
        val items = listOf(item(1, 150f), item(99, 100f))
        val t = TemplateSummary.totals(items, foods)
        assertEquals(252f, t.kcal, 0.01f)
    }

    @Test
    fun `空なら全部ゼロ`() {
        val t = TemplateSummary.totals(emptyList(), foods)
        assertEquals(0f, t.kcal, 0.01f)
        assertEquals(0f, t.protein, 0.01f)
    }
}
