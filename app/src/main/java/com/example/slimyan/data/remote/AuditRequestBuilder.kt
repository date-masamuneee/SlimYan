package com.example.slimyan.data.remote

import com.example.slimyan.data.TdeeCalculator
import com.example.slimyan.data.entity.Food
import com.example.slimyan.data.entity.MealTemplateItem
import com.example.slimyan.data.entity.UserProfile

/** 週間テンプレ監査の Claude 向けプロンプト（ユーザーメッセージ）を組み立てる純粋ロジック。 */
object AuditRequestBuilder {

    private val DAY_LABELS = listOf("月", "火", "水", "木", "金", "土", "日")

    fun build(
        items: List<MealTemplateItem>,
        foodsById: Map<Long, Food>,
        profile: UserProfile,
        refinement: String? = null,
    ): String {
        val tdee = TdeeCalculator.tdee(
            TdeeCalculator.bmr(profile.weightKg, profile.heightCm, profile.age, profile.sex),
            profile.activityLevel
        ).toInt()

        val foodLines = foodsById.values
            .sortedBy { it.id }
            .joinToString("\n") { f ->
                "${f.id}: ${f.name} / 一食${f.servingGrams.toInt()}g あたり ${f.calories.toInt()}kcal P${f.protein} F${f.fat} C${f.carb}"
            }

        val templateLines = items
            .sortedWith(compareBy({ it.dayOfWeek }, { it.mealSlot }))
            .joinToString("\n") { item ->
                val day = DAY_LABELS.getOrElse(item.dayOfWeek - 1) { item.dayOfWeek.toString() }
                "$day ${item.mealSlot} foodId=${item.foodId} ${item.grams.toInt()}g"
            }

        return buildString {
            appendLine("# ユーザー目標")
            appendLine("- 目標カロリー: ${profile.dailyCalorieTarget} kcal/日")
            appendLine("- 目標PFC: P${profile.proteinTargetG}g F${profile.fatTargetG}g C${profile.carbTargetG}g")
            appendLine("- TDEE(推定): $tdee kcal/日")
            appendLine("- 現体重 ${profile.weightKg}kg → 目標 ${profile.goalWeightKg}kg")
            appendLine()
            appendLine("# 利用可能な食品（この foodId のみ使用可。新しい食品は作らない）")
            appendLine(foodLines)
            appendLine()
            appendLine("# 現在の週間テンプレ（曜日 slot foodId grams）")
            appendLine(templateLines.ifBlank { "(空)" })
            appendLine()
            if (!refinement.isNullOrBlank()) {
                appendLine("# 追加の要望")
                appendLine(refinement)
                appendLine()
            }
            appendLine("# 指示")
            appendLine("この週間テンプレを減量の観点で監査せよ。観点：カロリー赤字が週を通して安定して出るか／タンパク質は足りてるか・盛りすぎてないか／塩分・食物繊維・コストの盲点。")
            appendLine("修正案を出す場合は、上記 foodId のみ使い、grams を調整した『完全な週間テンプレ』を revised に入れること（修正不要なら revised は空配列）。")
            appendLine("必ず以下のJSONのみで回答（説明文・コードブロック不要）：")
            appendLine("""{"diagnosis":"総評","issues":["問題点1","問題点2"],"revised":[{"day_of_week":1,"meal_slot":"breakfast","food_id":1,"grams":100}]}""")
        }.trim()
    }
}
