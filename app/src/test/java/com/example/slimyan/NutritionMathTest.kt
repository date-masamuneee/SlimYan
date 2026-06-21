package com.example.slimyan

import com.example.slimyan.data.NutritionMath
import com.example.slimyan.data.entity.Food
import org.junit.Assert.assertEquals
import org.junit.Test

class NutritionMathTest {

    private fun food(calories: Float, protein: Float, fat: Float, carb: Float, serving: Float) =
        Food(name = "test", calories = calories, protein = protein, fat = fat, carb = carb, servingGrams = serving)

    @Test
    fun `一食量と同量なら一食分そのまま`() {
        val f = food(200f, 20f, 5f, 10f, serving = 100f)
        val m = NutritionMath.forGrams(f, 100f)
        assertEquals(200f, m.kcal, 0.01f)
        assertEquals(20f, m.protein, 0.01f)
        assertEquals(5f, m.fat, 0.01f)
        assertEquals(10f, m.carb, 0.01f)
    }

    @Test
    fun `一食量より多ければ比例して増える`() {
        // ご飯 茶碗1杯=150g で 252kcal → 300g なら倍
        val rice = food(252f, 3.8f, 0.5f, 55.7f, serving = 150f)
        val m = NutritionMath.forGrams(rice, 300f)
        assertEquals(504f, m.kcal, 0.01f)
        assertEquals(7.6f, m.protein, 0.01f)
    }

    @Test
    fun `一食量より少なければ比例して減る`() {
        // 卵 1個=50g で 76kcal → 25g なら半分
        val egg = food(76f, 6.2f, 5.2f, 0.2f, serving = 50f)
        val m = NutritionMath.forGrams(egg, 25f)
        assertEquals(38f, m.kcal, 0.01f)
        assertEquals(3.1f, m.protein, 0.01f)
    }

    @Test
    fun `servingGrams が0なら0を返す（ゼロ除算ガード）`() {
        val broken = food(100f, 10f, 1f, 1f, serving = 0f)
        val m = NutritionMath.forGrams(broken, 100f)
        assertEquals(0f, m.kcal, 0.01f)
    }
}
