package com.example.slimyan

import com.example.slimyan.data.ShoppingListCalculator
import com.example.slimyan.data.entity.Food
import com.example.slimyan.data.entity.MealTemplateItem
import org.junit.Assert.assertEquals
import org.junit.Test

class ShoppingListCalculatorTest {

    private val chicken = Food(id = 1, name = "サラダチキン", calories = 105f, protein = 26f, fat = 1.5f, carb = 1f, servingGrams = 100f)
    private val rice = Food(id = 2, name = "ご飯", calories = 252f, protein = 3.8f, fat = 0.5f, carb = 55.7f, servingGrams = 150f)
    private val foods = mapOf(1L to chicken, 2L to rice)

    private fun item(day: Int, foodId: Long, grams: Float) =
        MealTemplateItem(dayOfWeek = day, mealSlot = "lunch", foodId = foodId, grams = grams)

    @Test
    fun `曜日をまたいで同一食材を合算し個数換算する`() {
        // チキン100g×3日 = 3個、ご飯150g×2日 = 2個
        val items = listOf(
            item(1, 1, 100f), item(2, 1, 100f), item(3, 1, 100f),
            item(1, 2, 150f), item(2, 2, 150f),
        )
        val lines = ShoppingListCalculator.calc(items, foods)
        val chickenLine = lines.first { it.food.id == 1L }
        val riceLine = lines.first { it.food.id == 2L }
        assertEquals(3f, chickenLine.servings, 0.01f)
        assertEquals(300f, chickenLine.totalGrams, 0.01f)
        assertEquals(2f, riceLine.servings, 0.01f)
    }

    @Test
    fun `週倍率を掛ける`() {
        val items = listOf(item(1, 1, 100f), item(2, 1, 100f)) // 2個/週
        val lines = ShoppingListCalculator.calc(items, foods, weeks = 4)
        assertEquals(8f, lines.first().servings, 0.01f) // 4週分
    }

    @Test
    fun `存在しない食材は除外`() {
        val items = listOf(item(1, 99, 100f))
        assertEquals(0, ShoppingListCalculator.calc(items, foods).size)
    }

    @Test
    fun `個数が多い順に並ぶ`() {
        val items = listOf(
            item(1, 2, 150f),
            item(1, 1, 100f), item(2, 1, 100f), item(3, 1, 100f),
        )
        val lines = ShoppingListCalculator.calc(items, foods)
        assertEquals(1L, lines.first().food.id) // チキン3個が先頭
    }
}
