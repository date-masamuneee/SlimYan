package com.example.slimyan

import com.example.slimyan.data.remote.AuditRequestBuilder
import com.example.slimyan.data.entity.Food
import com.example.slimyan.data.entity.MealTemplateItem
import com.example.slimyan.data.entity.UserProfile
import org.junit.Assert.assertTrue
import org.junit.Test

class AuditRequestBuilderTest {

    private val foods = listOf(
        Food(id = 1, name = "サラダチキン", calories = 105f, protein = 26f, fat = 1.5f, carb = 1f, servingGrams = 100f),
        Food(id = 2, name = "ご飯", calories = 252f, protein = 3.8f, fat = 0.5f, carb = 55.7f, servingGrams = 150f),
    )
    private val profile = UserProfile(
        heightCm = 175f, weightKg = 96f, age = 28, sex = "male",
        activityLevel = "moderate", dailyCalorieTarget = 2100,
        proteinTargetG = 150f, fatTargetG = 60f, carbTargetG = 200f,
    )
    private val items = listOf(
        MealTemplateItem(dayOfWeek = 1, mealSlot = "breakfast", foodId = 1, grams = 100f),
        MealTemplateItem(dayOfWeek = 1, mealSlot = "lunch", foodId = 2, grams = 150f),
    )

    @Test
    fun `目標カロリーとPFCを含む`() {
        val msg = AuditRequestBuilder.build(items, foods.associateBy { it.id }, profile)
        assertTrue(msg.contains("2100"))
        assertTrue(msg.contains("150")) // protein target
    }

    @Test
    fun `食品マスタをidつきで列挙する`() {
        val msg = AuditRequestBuilder.build(items, foods.associateBy { it.id }, profile)
        assertTrue(msg.contains("サラダチキン"))
        assertTrue(msg.contains("ご飯"))
        // foodId が参照可能な形で出ている
        assertTrue(msg.contains("1") && msg.contains("2"))
    }

    @Test
    fun `テンプレ品目を曜日とslotで含む`() {
        val msg = AuditRequestBuilder.build(items, foods.associateBy { it.id }, profile)
        assertTrue(msg.contains("breakfast"))
        assertTrue(msg.contains("lunch"))
    }

    @Test
    fun `JSON形式の指示を含む`() {
        val msg = AuditRequestBuilder.build(items, foods.associateBy { it.id }, profile)
        assertTrue(msg.contains("diagnosis"))
        assertTrue(msg.contains("revised"))
    }
}
