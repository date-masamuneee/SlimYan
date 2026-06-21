package com.example.slimyan.data

import com.example.slimyan.data.entity.Food

/** 食品の一食分の値を、任意グラム数に比例換算する純粋ロジック。 */
object NutritionMath {

    data class Macros(val kcal: Float, val protein: Float, val fat: Float, val carb: Float)

    fun forGrams(food: Food, grams: Float): Macros {
        if (food.servingGrams <= 0f) return Macros(0f, 0f, 0f, 0f)
        val ratio = grams / food.servingGrams
        return Macros(
            kcal = food.calories * ratio,
            protein = food.protein * ratio,
            fat = food.fat * ratio,
            carb = food.carb * ratio,
        )
    }
}
