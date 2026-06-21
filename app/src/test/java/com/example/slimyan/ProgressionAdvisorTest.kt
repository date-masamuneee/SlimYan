package com.example.slimyan

import com.example.slimyan.data.ProgressionAdvisor
import com.example.slimyan.data.entity.SetEntry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionAdvisorTest {

    private fun set(reps: Int) = SetEntry(exerciseId = 1, dateEpochDay = 100, weight = 20f, reps = reps, setOrder = 0)

    @Test
    fun `全セットが上限到達なら重量UP`() {
        val sets = listOf(set(12), set(12), set(12))
        assertTrue(ProgressionAdvisor.shouldIncreaseWeight(sets, targetSets = 3, repCeiling = 12))
    }

    @Test
    fun `1セットでも未達ならまだ`() {
        val sets = listOf(set(12), set(12), set(10))
        assertFalse(ProgressionAdvisor.shouldIncreaseWeight(sets, targetSets = 3, repCeiling = 12))
    }

    @Test
    fun `セット数が足りなければまだ`() {
        val sets = listOf(set(12), set(12))
        assertFalse(ProgressionAdvisor.shouldIncreaseWeight(sets, targetSets = 3, repCeiling = 12))
    }

    @Test
    fun `目標より多くこなして全部上限なら重量UP`() {
        val sets = listOf(set(12), set(12), set(12), set(12))
        assertTrue(ProgressionAdvisor.shouldIncreaseWeight(sets, targetSets = 3, repCeiling = 12))
    }

    @Test
    fun `記録なしならまだ`() {
        assertFalse(ProgressionAdvisor.shouldIncreaseWeight(emptyList(), targetSets = 3, repCeiling = 12))
    }
}
