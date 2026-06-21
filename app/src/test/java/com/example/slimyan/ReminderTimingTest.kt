package com.example.slimyan

import com.example.slimyan.data.ReminderTiming
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class ReminderTimingTest {

    private val allDays = 0b1111111

    // 月曜の朝として基準時刻を作る（2026-06-22 は月曜）
    private fun monday(hour: Int, minute: Int) = LocalDateTime.of(2026, 6, 22, hour, minute)

    @Test
    fun `曜日マスク判定`() {
        // 月=bit0
        assertEquals(true, ReminderTiming.shouldFireOn(0b0000001, 1))
        assertEquals(false, ReminderTiming.shouldFireOn(0b0000001, 2))
        // 日=bit6
        assertEquals(true, ReminderTiming.shouldFireOn(0b1000000, 7))
    }

    @Test
    fun `今日のまだ来てない時刻なら今日`() {
        val now = monday(8, 0)
        val next = ReminderTiming.nextTrigger(allDays, 9, 0, now)
        assertEquals(monday(9, 0), next)
    }

    @Test
    fun `今日の時刻を過ぎていたら翌日`() {
        val now = monday(10, 0)
        val next = ReminderTiming.nextTrigger(allDays, 9, 0, now)
        assertEquals(LocalDateTime.of(2026, 6, 23, 9, 0), next) // 火曜
    }

    @Test
    fun `指定曜日だけのマスクは次の該当曜日`() {
        val wedOnly = 0b0000100 // 水=bit2
        val now = monday(10, 0)
        val next = ReminderTiming.nextTrigger(wedOnly, 19, 0, now)
        assertEquals(LocalDateTime.of(2026, 6, 24, 19, 0), next) // 水曜
    }

    @Test
    fun `マスク0なら発火しない`() {
        assertNull(ReminderTiming.nextTrigger(0, 9, 0, monday(8, 0)))
    }
}
