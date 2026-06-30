package com.example.slimyan

import com.example.slimyan.data.PfcPlanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PfcPlannerTest {

    @Test
    fun `スタンダードはタンパク質1_8g_kg・脂質25％`() {
        val pfc = PfcPlanner.suggest(targetKcal = 2000, weightKg = 80f, policy = PfcPlanner.Policy.STANDARD)
        assertEquals(144, pfc.protein) // 80 * 1.8
        assertEquals(56, pfc.fat)      // 2000*0.25/9
    }

    @Test
    fun `タンパク質多めはg_kgが上がる`() {
        val pfc = PfcPlanner.suggest(2000, 80f, PfcPlanner.Policy.HIGH_PROTEIN)
        assertEquals(176, pfc.protein) // 80 * 2.2
    }

    @Test
    fun `脂質控えめは脂質が少ない`() {
        val pfc = PfcPlanner.suggest(2000, 80f, PfcPlanner.Policy.LOW_FAT)
        assertEquals(33, pfc.fat) // 2000*0.15/9
    }

    @Test
    fun `糖質控えめはスタンダードより糖質が少ない`() {
        val std = PfcPlanner.suggest(2000, 80f, PfcPlanner.Policy.STANDARD)
        val low = PfcPlanner.suggest(2000, 80f, PfcPlanner.Policy.LOW_CARB)
        assertTrue(low.carb < std.carb)
    }

    @Test
    fun `カロリーが少なすぎても糖質は負にならない`() {
        val pfc = PfcPlanner.suggest(targetKcal = 800, weightKg = 100f, policy = PfcPlanner.Policy.HIGH_PROTEIN)
        assertTrue(pfc.carb >= 0)
    }
}
