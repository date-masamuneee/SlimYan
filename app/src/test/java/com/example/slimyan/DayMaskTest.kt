package com.example.slimyan

import com.example.slimyan.data.DayMask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DayMaskTest {

    @Test
    fun `トグルでビットが立つ・消える`() {
        var mask = 0
        mask = DayMask.toggle(mask, 0) // 月
        assertTrue(DayMask.isSet(mask, 0))
        mask = DayMask.toggle(mask, 0)
        assertFalse(DayMask.isSet(mask, 0))
    }

    @Test
    fun `集合とマスクが往復一致する`() {
        val days = setOf(0, 2, 4) // 月水金
        val mask = DayMask.fromDays(days)
        assertEquals(days, DayMask.toDays(mask))
    }

    @Test
    fun `全曜日`() {
        assertEquals(setOf(0, 1, 2, 3, 4, 5, 6), DayMask.toDays(DayMask.ALL))
    }

    @Test
    fun `空集合は0`() {
        assertEquals(0, DayMask.fromDays(emptySet()))
        assertEquals(emptySet<Int>(), DayMask.toDays(0))
    }
}
