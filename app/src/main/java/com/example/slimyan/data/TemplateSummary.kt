package com.example.slimyan.data

import com.example.slimyan.data.entity.Food
import com.example.slimyan.data.entity.MealTemplateItem

/** テンプレ品目リストの栄養合計を集計する純粋ロジック。日単位でも週単位でも使える。 */
object TemplateSummary {

    data class Totals(
        val kcal: Float = 0f,
        val protein: Float = 0f,
        val fat: Float = 0f,
        val carb: Float = 0f,
    )

    fun totals(items: List<MealTemplateItem>, foodsById: Map<Long, Food>): Totals {
        var kcal = 0f; var p = 0f; var f = 0f; var c = 0f
        for (item in items) {
            val food = foodsById[item.foodId] ?: continue
            val m = NutritionMath.forGrams(food, item.grams)
            kcal += m.kcal; p += m.protein; f += m.fat; c += m.carb
        }
        return Totals(kcal, p, f, c)
    }
}
