package com.example.slimyan

import com.example.slimyan.data.TdeeCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class TdeeCalculatorTest {

    @Test
    fun `男性のBMR（Harris-Benedict）`() {
        // 88.362 + 13.397*96 + 4.799*175 - 5.677*28
        val bmr = TdeeCalculator.bmr(weightKg = 96f, heightCm = 175f, age = 28, sex = "male")
        assertEquals(2055.34f, bmr, 0.5f)
    }

    @Test
    fun `女性のBMR（Harris-Benedict）`() {
        // 447.593 + 9.247*60 + 3.098*160 - 4.330*30
        val bmr = TdeeCalculator.bmr(weightKg = 60f, heightCm = 160f, age = 30, sex = "female")
        assertEquals(1368.19f, bmr, 0.5f)
    }

    @Test
    fun `活動量の係数が反映される`() {
        val bmr = 2000f
        assertEquals(2400f, TdeeCalculator.tdee(bmr, "sedentary"), 0.1f)    // 1.2
        assertEquals(2750f, TdeeCalculator.tdee(bmr, "light"), 0.1f)        // 1.375
        assertEquals(3100f, TdeeCalculator.tdee(bmr, "moderate"), 0.1f)     // 1.55 (else)
        assertEquals(3450f, TdeeCalculator.tdee(bmr, "active"), 0.1f)       // 1.725
        assertEquals(3800f, TdeeCalculator.tdee(bmr, "very_active"), 0.1f)  // 1.9
    }

    @Test
    fun `期日未設定なら摂取目標はTDEEそのまま`() {
        assertEquals(3000, TdeeCalculator.dailyTarget(3000f, 96f, 74f, goalEpochDay = 0L))
    }

    @Test
    fun `摂取目標は下限1200を下回らない`() {
        // 極端な目標（巨大な赤字）でも 1200 で下げ止まる
        val target = TdeeCalculator.dailyTarget(
            tdee = 2000f, currentWeightKg = 96f, goalWeightKg = 74f,
            goalEpochDay = java.time.LocalDate.now().plusDays(10).toEpochDay()
        )
        assertEquals(1200, target)
    }
}
