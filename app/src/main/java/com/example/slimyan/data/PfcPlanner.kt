package com.example.slimyan.data

import kotlin.math.roundToInt

/**
 * 目標カロリー＋体重＋方針から目標PFC(g)を算出する純粋ロジック。
 * タンパク質は体重比(g/kg)、脂質は総カロリー比(%)、糖質は残りカロリーから算出。
 * P=4kcal/g, F=9kcal/g, C=4kcal/g。
 */
object PfcPlanner {

    enum class Policy(val label: String, val proteinPerKg: Float, val fatPct: Float) {
        STANDARD("スタンダード", 1.8f, 0.25f),
        HIGH_PROTEIN("タンパク質多め", 2.2f, 0.25f),
        LOW_CARB("糖質控えめ", 2.0f, 0.35f),
        LOW_FAT("脂質控えめ", 2.0f, 0.15f),
    }

    data class Pfc(val protein: Int, val fat: Int, val carb: Int)

    fun suggest(targetKcal: Int, weightKg: Float, policy: Policy): Pfc {
        val proteinG = weightKg * policy.proteinPerKg
        val fatG = targetKcal * policy.fatPct / 9f
        val carbG = ((targetKcal - proteinG * 4f - fatG * 9f) / 4f).coerceAtLeast(0f)
        return Pfc(proteinG.roundToInt(), fatG.roundToInt(), carbG.roundToInt())
    }
}
