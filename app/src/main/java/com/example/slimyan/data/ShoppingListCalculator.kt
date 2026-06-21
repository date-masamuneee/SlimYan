package com.example.slimyan.data

import com.example.slimyan.data.entity.Food
import com.example.slimyan.data.entity.MealTemplateItem

/** 週間テンプレから食材ごとの必要量（個数・総グラム）を算出する純粋ロジック。 */
object ShoppingListCalculator {

    data class Line(val food: Food, val servings: Float, val totalGrams: Float)

    fun calc(
        items: List<MealTemplateItem>,
        foodsById: Map<Long, Food>,
        weeks: Int = 1,
    ): List<Line> = items
        .groupBy { it.foodId }
        .mapNotNull { (foodId, group) ->
            val food = foodsById[foodId] ?: return@mapNotNull null
            val totalGrams = group.sumOf { it.grams.toDouble() }.toFloat() * weeks
            val servings = if (food.servingGrams > 0f) totalGrams / food.servingGrams else 0f
            Line(food, servings, totalGrams)
        }
        .sortedByDescending { it.servings }
}
