package com.example.slimyan.data

import com.example.slimyan.data.entity.Food
import com.example.slimyan.data.entity.MealEntry
import com.example.slimyan.data.entity.MealTemplateItem

/** テンプレ品目を、指定日の MealEntry（プラン）へ変換する純粋ロジック。kcal/名前はスナップショット。 */
object TemplateExpander {

    fun expand(
        items: List<MealTemplateItem>,
        foodsById: Map<Long, Food>,
        dateEpochDay: Long,
    ): List<MealEntry> = items.mapNotNull { item ->
        val food = foodsById[item.foodId] ?: return@mapNotNull null
        MealEntry(
            dateEpochDay = dateEpochDay,
            mealSlot = item.mealSlot,
            foodId = food.id,
            foodName = food.name,
            grams = item.grams,
            calories = NutritionMath.forGrams(food, item.grams).kcal,
            isPlanned = true,
            isChecked = false,
        )
    }
}
